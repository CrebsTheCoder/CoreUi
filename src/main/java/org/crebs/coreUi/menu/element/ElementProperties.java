package org.crebs.coreUi.menu.element;

import net.kyori.adventure.text.Component;
import java.util.Objects;

public record ElementProperties(String id, Bounds bounds, String page, Component label, Component description) {

    public ElementProperties {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Element id isn't set");
        }
        if (page == null || page.isBlank()) {
            throw new IllegalArgumentException("Element page isn't set");
        }
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(description, "description");
    }
}
