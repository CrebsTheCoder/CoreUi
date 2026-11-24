package org.crebs.coreui;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.crebs.coreui.menu.CoreMenu;
import org.crebs.coreui.menu.style.ThemeColour;
import org.crebs.coreui.menu.listener.MenuListener;
import org.crebs.coreui.menu.session.MenuSession;
import java.util.*;
import org.crebs.coreui.glyph.GlyphManager;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Path;

import org.crebs.coreui.paper.PaperPlatform;
import org.crebs.coreui.paper.PaperViewer;

public final class CoreUi extends JavaPlugin {
    private CoreUiRuntime runtime;
    private GlyphManager glyphs;

    public CoreUiRuntime getRuntime() { return Objects.requireNonNull(runtime, "CoreUi is not enabled"); }
    public GlyphManager getGlyphs() { return Objects.requireNonNull(glyphs, "CoreUi is not enabled"); }
    public CompletableFuture<Path> reloadPack() { return getGlyphs().reload(); }
    public CompletableFuture<Path> reloadPack(Path outputDirectory) { return getGlyphs().reload(outputDirectory); }

    private void configurePackOutput() {
        String configured = getConfig().getString("pack.output-directory", "Pack");
        if (configured == null || configured.isBlank()) throw new IllegalArgumentException("output-directory cannot be empty");
        Path path = Path.of(configured);
        if (!path.isAbsolute()) path = getDataFolder().toPath().resolve(path);
        getGlyphs().setOutputDirectory(path);
    }

    public void open(Player player, CoreMenu menu) { getRuntime().open(new PaperViewer(player), menu); }
    public Optional<MenuSession> getSession(Player player) { return getRuntime().getSession(new PaperViewer(player)); }
    public void refresh(Player player) { getRuntime().refresh(new PaperViewer(player)); }
    public void navigate(Player player, String page) { getRuntime().navigate(new PaperViewer(player), page); }
    public void focus(Player player, String elementId) { getRuntime().focus(new PaperViewer(player), elementId); }
    public ThemeColour getTheme(Player player) { return getRuntime().getTheme(new PaperViewer(player)); }
    public void setTheme(Player player, ThemeColour theme) { getRuntime().setTheme(new PaperViewer(player), theme); }
    public void close(Player player) { getRuntime().close(new PaperViewer(player)); }
    public void removeSession(Player player) { getRuntime().removeSession(new PaperViewer(player)); }
    public void handleClick(Player player, Key key) { getRuntime().handleClick(new PaperViewer(player), key); }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runtime = new CoreUiRuntime(new PaperPlatform(this), getLogger());
        glyphs = new GlyphManager(getDataFolder().toPath(), getFile().toPath());
        configurePackOutput();
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
                reloadConfig();
                try {
                    configurePackOutput();
                } catch (IllegalArgumentException exception) {
                    sender.sendMessage("Invalid pack output: " + exception.getMessage());
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
        getServer().getScheduler().runTaskTimer(this, runtime::expireSessions, 200L, 200L);
    }

    @Override
    public void onDisable() {
        if (runtime != null) runtime.close();
    }
}
