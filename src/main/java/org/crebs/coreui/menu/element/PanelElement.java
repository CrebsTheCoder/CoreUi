package org.crebs.coreui.menu.element;

import java.util.Objects;

public record PanelElement(ElementProperties properties) implements MenuElement {

    public PanelElement {
        Objects.requireNonNull(properties);
    }
}
