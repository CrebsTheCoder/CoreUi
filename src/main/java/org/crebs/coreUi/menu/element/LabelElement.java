package org.crebs.coreUi.menu.element;

import java.util.Objects;

public record LabelElement(ElementProperties properties) implements MenuElement {

    public LabelElement {
        Objects.requireNonNull(properties);
    }
}
