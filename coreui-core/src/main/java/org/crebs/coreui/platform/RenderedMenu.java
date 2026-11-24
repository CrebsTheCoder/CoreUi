package org.crebs.coreui.platform;

import net.kyori.adventure.text.Component;
import java.util.Objects;

public record RenderedMenu(Component title, Component externalTitle, Component content) {
    public RenderedMenu {
        Objects.requireNonNull(title);
        Objects.requireNonNull(externalTitle);
        Objects.requireNonNull(content);
    }
}
