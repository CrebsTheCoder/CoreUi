package org.crebs.coreui.paper;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.crebs.coreui.platform.*;
import java.util.List;

public final class PaperPlatform implements MenuPlatform {
    private final Plugin plugin;

    public PaperPlatform(Plugin plugin) { this.plugin = plugin; }

    public void show(MenuViewer viewer, RenderedMenu menu) {
        viewer.unwrap(Player.class).showDialog(Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(menu.title()).externalTitle(menu.externalTitle())
                .pause(false).canCloseWithEscape(true).afterAction(DialogBase.DialogAfterAction.NONE)
                .body(List.of(DialogBody.plainMessage(menu.content(), 511))).build())
            .type(DialogType.dialogList(RegistrySet.valueSet(RegistryKey.DIALOG, List.of()), null, 1, 1))));
    }

    public void close(MenuViewer viewer) { viewer.unwrap(Player.class).closeDialog(); }
    public long tick(MenuViewer viewer) { return viewer.unwrap(Player.class).getWorld().getGameTime(); }
    public void schedule(MenuViewer viewer, Runnable action, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, action, delayTicks);
    }
    public void requireThread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("CoreUi requires the server thread");
    }
}
