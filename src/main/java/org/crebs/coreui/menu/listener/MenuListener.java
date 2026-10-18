package org.crebs.coreui.menu.listener;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.crebs.coreui.CoreUi;

public final class MenuListener implements Listener {
    private final CoreUi plugin;

    public MenuListener(CoreUi plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerCustomClickEvent event) {
        if (!event.getIdentifier().namespace().equals("coreui")) {
            return;
        }
        if (!(event.getCommonConnection() instanceof PlayerGameConnection connection)) {
            return;
        }
        if (event.isAsynchronous()) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> plugin.handleClick(connection.getPlayer(), event.getIdentifier()));
        } else {
            plugin.handleClick(connection.getPlayer(), event.getIdentifier());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer());
    }
}
