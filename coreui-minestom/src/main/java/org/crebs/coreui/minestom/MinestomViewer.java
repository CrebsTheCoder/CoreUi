package org.crebs.coreui.minestom;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.entity.Player;
import org.crebs.coreui.platform.MenuViewer;
import java.util.Objects;
import java.util.UUID;

public record MinestomViewer(Player player) implements MenuViewer {
    public MinestomViewer { Objects.requireNonNull(player); }
    public UUID id() { return player.getUuid(); }
    public boolean online() { return player.isOnline(); }
    public Audience audience() { return player; }
    public <T> T unwrap(Class<T> type) { return type.cast(player); }
}
