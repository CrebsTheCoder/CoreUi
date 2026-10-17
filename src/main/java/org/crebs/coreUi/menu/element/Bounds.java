package org.crebs.coreUi.menu.element;

public record Bounds(int x, int y, int width, int height) {

    public Bounds {
        if (x < 0 || y < 0 || width < 1 || height < 1) {
            throw new IllegalArgumentException("Bounds require a non-negative position and positive size");
        }
    }

    public boolean overlaps(Bounds other) {
        return x < other.x + other.width && x + width > other.x && y < other.y + other.height && y + height > other.y;
    }
}
