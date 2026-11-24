package org.crebs.coreui;

import net.kyori.adventure.key.Key;
import org.crebs.coreui.menu.CoreMenu;
import org.crebs.coreui.menu.element.DropdownElement;
import org.crebs.coreui.menu.render.MenuRenderer;
import org.crebs.coreui.menu.session.MenuSession;
import org.crebs.coreui.menu.session.MenuValues;
import org.crebs.coreui.menu.style.ThemeColour;
import org.crebs.coreui.platform.MenuPlatform;
import org.crebs.coreui.platform.MenuViewer;
import java.util.*;
import java.util.logging.Logger;

public final class CoreUiRuntime implements AutoCloseable {
    private final Map<UUID, MenuSession> sessions = new HashMap<>();
    private final Map<UUID, MenuViewer> viewers = new HashMap<>();
    private final Map<UUID, ThemeColour> themes = new HashMap<>();
    private final Map<UUID, Map<String, MenuValues>> rememberedValues = new HashMap<>();
    private final MenuRenderer renderer = new MenuRenderer();
    private final MenuPlatform platform;
    private final Logger logger;

    public CoreUiRuntime(MenuPlatform platform, Logger logger) {
        this.platform = Objects.requireNonNull(platform);
        this.logger = Objects.requireNonNull(logger);
    }

    public void open(MenuViewer player, CoreMenu menu) {
        platform.requireThread();
        remember(player.id());
        MenuValues saved = menu.remembersValues() ? rememberedValues.getOrDefault(player.id(), Map.of()).getOrDefault(menu.getId(), MenuValues.EMPTY) : MenuValues.EMPTY;
        MenuSession session = new MenuSession(menu, saved);
        sessions.put(player.id(), session);
        viewers.put(player.id(), player);
        draw(player, session);
    }

    public Optional<MenuSession> getSession(MenuViewer player) {
        platform.requireThread();
        return Optional.ofNullable(sessions.get(player.id()));
    }

    public void refresh(MenuViewer player) {
        platform.requireThread();
        MenuSession session = sessions.get(player.id());
        if (session == null) {
            return;
        }
        session.touch();
        draw(player, session);
    }

    private void draw(MenuViewer player, MenuSession session) {
        platform.show(player, renderer.render(session, platform.tick(player)));
    }

    public void navigate(MenuViewer player, String page) {
        platform.requireThread();
        MenuSession session = sessions.get(player.id());
        if (session != null) {
            session.navigate(page);
            refresh(player);
        }
    }

    public void focus(MenuViewer player, String elementId) {
        platform.requireThread();
        MenuSession session = sessions.get(player.id());
        if (session != null) {
            session.focus(elementId);
            refresh(player);
        }
    }

    public ThemeColour getTheme(MenuViewer player) {
        platform.requireThread();
        return themes.getOrDefault(player.id(), ThemeColour.LIGHT);
    }

    public void setTheme(MenuViewer player, ThemeColour theme) {
        platform.requireThread();
        themes.put(player.id(), Objects.requireNonNull(theme));
        MenuSession session = sessions.get(player.id());
        if (session != null) {
            session.setTheme(theme);
        }
    }

    public void close(MenuViewer player) {
        platform.requireThread();
        removeSession(player);
        platform.close(player);
    }

    public void removeSession(MenuViewer player) {
        platform.requireThread();
        remember(player.id());
        sessions.remove(player.id());
        viewers.remove(player.id());
    }

    private void remember(UUID playerId) {
        MenuSession session = sessions.get(playerId);
        if (session != null && session.getMenu().remembersValues()) {
            rememberedValues.computeIfAbsent(playerId, ignored -> new HashMap<>())
                    .put(session.getMenu().getId(), session.snapshot());
        }
    }

    public void handleClick(MenuViewer player, Key key) {
        platform.requireThread();
        MenuSession session = sessions.get(player.id());
        if (session == null) {
            return;
        }
        if (session.expired() || key.equals(MenuRenderer.CLOSE)) {
            close(player);
            return;
        }
        if (!session.getPressedId().isEmpty()) {
            return;
        }
        session.renderTick(platform.tick(player));
        MenuSession.Interaction interaction = session.consumeClick(key);
        if (interaction == null) {
            return;
        }
        session.press(interaction.elementId(), interaction.part());
        if (session.getMenu().getElements().stream().anyMatch(element ->
                element.id().equals(interaction.elementId()) && element instanceof DropdownElement)) {
            release(player, session, interaction);
            return;
        }
        refresh(player);
        platform.schedule(player, () -> release(player, session, interaction), 2L);
    }

    private void release(MenuViewer player, MenuSession session, MenuSession.Interaction interaction) {
        if (sessions.get(player.id()) != session || !player.online()) {
            return;
        }
        if (!session.getPressedId().equals(interaction.elementId())) {
            return;
        }
        session.release();
        try {
            session.renderTick(platform.tick(player));
            interaction.callback().accept(player);
            if (sessions.get(player.id()) == session && player.online()) {
                refresh(player);
            }
        } catch (RuntimeException exception) {
            logger.log(java.util.logging.Level.SEVERE, "Menu click failed: " + interaction.elementId(), exception);
            if (sessions.get(player.id()) == session) {
                close(player);
            }
        }
    }

    public void expireSessions() {
        platform.requireThread();
        for (MenuViewer viewer : List.copyOf(viewers.values())) {
            MenuSession session = sessions.get(viewer.id());
            if (!viewer.online()) disconnect(viewer);
            else if (session != null && session.expired()) close(viewer);
        }
    }

    public void disconnect(MenuViewer viewer) {
        platform.requireThread();
        removeSession(viewer);
    }

    @Override
    public void close() {
        platform.requireThread();
        for (MenuViewer viewer : List.copyOf(viewers.values())) {
            if (viewer.online()) platform.close(viewer);
        }
        sessions.clear();
        viewers.clear();
        rememberedValues.clear();
        themes.clear();
    }
}
