package org.crebs.coreUi.menu.element;

import java.util.Objects;

public record DisplayElement(ElementProperties properties, Kind kind, double progress) implements MenuElement {
    public enum Kind { BADGE, PROGRESS, DIVIDER }
    public DisplayElement {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(kind);
        if (!Double.isFinite(progress) || progress < 0 || progress > 1) {
            throw new IllegalArgumentException("Progress must be between 0 and 1");
        }
        if (kind == Kind.PROGRESS && (properties.bounds().height() < 28 || properties.bounds().width() < 60)) {
            throw new IllegalArgumentException("Progress controls need at least 60x28 pixels");
        }
        if (kind == Kind.BADGE && properties.bounds().height() < 16) {
            throw new IllegalArgumentException("Badges need at least 16 pixels of height");
        }
    }
}
