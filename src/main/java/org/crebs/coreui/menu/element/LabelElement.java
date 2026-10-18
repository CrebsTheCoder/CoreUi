package org.crebs.coreui.menu.element;

import java.util.Objects;

public record LabelElement(ElementProperties properties) implements MenuElement {

    public LabelElement {
        Objects.requireNonNull(properties);
    }
}
