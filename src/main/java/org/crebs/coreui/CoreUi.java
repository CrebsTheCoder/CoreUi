package org.crebs.coreui;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.crebs.coreui.menu.CoreMenu;
import org.crebs.coreui.menu.style.ThemeColour;
import org.crebs.coreui.menu.element.DropdownElement;
import org.crebs.coreui.menu.listener.MenuListener;
import org.crebs.coreui.menu.render.MenuRenderer;
import org.crebs.coreui.menu.session.MenuSession;
import org.crebs.coreui.menu.session.MenuValues;
import java.util.*;
import org.crebs.coreui.glyph.GlyphManager;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Path;

public final class CoreUi extends JavaPlugin {
    private final Map<UUID, MenuSession> sessions = new HashMap<>();
    private final Map<UUID, ThemeColour> themes = new HashMap<>();
    private final Map<UUID, Map<String, MenuValues>> rememberedValues = new HashMap<>();
    private final MenuRenderer renderer = new MenuRenderer();
    private GlyphManager glyphs;

    public GlyphManager getGlyphs() {
        return Objects.requireNonNull(glyphs, "CoreUi is not enabled");
    }

    public CompletableFuture<Path> reloadPack() {
        return getGlyphs().reload();
    }

    @Override
    public void onEnable() {
        glyphs = new GlyphManager(getDataFolder().toPath(), getFile().toPath());
        reloadPack().whenComplete((path, error) -> {
            if (error == null) getLogger().info("Resource pack generated: " + path);
            else getLogger().log(java.util.logging.Level.SEVERE, "Resource pack generation failed", error);
        });
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getLogger().info("CoreUI menus online");
        Objects.requireNonNull(getCommand("coreui")).setExecutor((sender, command, label, args) -> {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("coreui.reload")) {
                    sender.sendMessage("You do not have permission to reload CoreUi");
                    return true;
                }
                sender.sendMessage("Rebuilding CoreUi resource pack...");
                reloadPack().whenComplete((path, error) -> {
                    if (!isEnabled()) return;
                    getServer().getScheduler().runTask(this, () -> {
                        if (error == null) sender.sendMessage("Loaded " + glyphs.all().size() + " glyphs. Pack: " + path);
                        else {
                            sender.sendMessage("Pack rebuild failed. Check the console for details");
                            getLogger().log(java.util.logging.Level.SEVERE, "Resource pack generation failed", error);
                        }
                    });
                });
                return true;
            }
            sender.sendMessage("Usage: /coreui reload");
            return true;
        });
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (UUID id : new ArrayList<>(sessions.keySet())) {
                MenuSession session = sessions.get(id);
                if (session != null && session.expired()) {
                    Player player = Bukkit.getPlayer(id);
                    if (player != null) {
                        close(player);
                    } else {
                        remember(id);
                        sessions.remove(id);
                    }
                }
            }
        }, 200L, 200L);
    }

    public void open(Player player, CoreMenu menu) {
        requireMainThread();
        remember(player.getUniqueId());
        MenuValues saved = menu.remembersValues() ? rememberedValues.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(menu.getId(), MenuValues.EMPTY) : MenuValues.EMPTY;
        MenuSession session = new MenuSession(menu, saved);
        sessions.put(player.getUniqueId(), session);
        draw(player, session);
    }

    public Optional<MenuSession> getSession(Player player) {
        requireMainThread();
        return Optional.ofNullable(sessions.get(player.getUniqueId()));
    }

    public void refresh(Player player) {
        requireMainThread();
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.touch();
        draw(player, session);
    }

    private void draw(Player player, MenuSession session) {
        player.showDialog(renderer.render(session, player.getWorld().getGameTime()));
    }

    public void navigate(Player player, String page) {
        requireMainThread();
        MenuSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.navigate(page);
            refresh(player);
        }
    }

    public void focus(Player player, String elementId) {
        requireMainThread();
        MenuSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.focus(elementId);
            refresh(player);
        }
    }

    public ThemeColour getTheme(Player player) {
        return themes.getOrDefault(player.getUniqueId(), ThemeColour.LIGHT);
    }

    public void setTheme(Player player, ThemeColour theme) {
        requireMainThread();
        themes.put(player.getUniqueId(), Objects.requireNonNull(theme));
        MenuSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.setTheme(theme);
        }
    }

    public void close(Player player) {
        requireMainThread();
        removeSession(player);
        player.closeDialog();
    }

    public void removeSession(Player player) {
        requireMainThread();
        remember(player.getUniqueId());
        sessions.remove(player.getUniqueId());
    }

    private void remember(UUID playerId) {
        MenuSession session = sessions.get(playerId);
        if (session != null && session.getMenu().remembersValues()) {
            rememberedValues.computeIfAbsent(playerId, ignored -> new HashMap<>())
                    .put(session.getMenu().getId(), session.snapshot());
        }
    }

    public void handleClick(Player player, Key key) {
        requireMainThread();
        MenuSession session = sessions.get(player.getUniqueId());
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
        session.renderTick(player.getWorld().getGameTime());
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
        getServer().getScheduler().runTaskLater(this, () -> release(player, session, interaction), 2L);
    }

    private void release(Player player, MenuSession session, MenuSession.Interaction interaction) {
        if (sessions.get(player.getUniqueId()) != session || !player.isOnline()) {
            return;
        }
        if (!session.getPressedId().equals(interaction.elementId())) {
            return;
        }
        session.release();
        try {
            session.renderTick(player.getWorld().getGameTime());
            interaction.callback().accept(player);
            if (sessions.get(player.getUniqueId()) == session && player.isOnline()) {
                refresh(player);
            }
        } catch (RuntimeException exception) {
            getLogger().log(java.util.logging.Level.SEVERE, "Menu click failed: " + interaction.elementId(), exception);
            if (sessions.get(player.getUniqueId()) == session) {
                close(player);
            }
        }
    }

    private void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("CoreUI requires the server thread");
        }
    }

    @Override
    public void onDisable() {
        for (UUID id : new ArrayList<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                close(player);
            }
        }
        sessions.clear();
        rememberedValues.clear();
        themes.clear();
    }
}
