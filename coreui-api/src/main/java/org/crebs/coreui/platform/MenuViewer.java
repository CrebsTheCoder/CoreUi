package org.crebs.coreui.platform;

import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import java.util.UUID;

public interface MenuViewer extends ForwardingAudience.Single {
    UUID id();
    boolean online();
    <T> T unwrap(Class<T> type);

    default void sendMessage(String message) {
        audience().sendMessage(Component.text(message));
    }
}
