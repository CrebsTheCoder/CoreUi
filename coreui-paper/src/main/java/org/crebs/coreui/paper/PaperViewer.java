package org.crebs.coreui.paper;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.crebs.coreui.platform.MenuViewer;
import java.util.Objects;
import java.util.UUID;

public record PaperViewer(Player player) implements MenuViewer {
    public PaperViewer { Objects.requireNonNull(player); }
    public UUID id() { return player.getUniqueId(); }
    public boolean online() { return player.isOnline(); }
    public Audience audience() { return player; }
    public <T> T unwrap(Class<T> type) { return type.cast(player); }
}
