package org.crebs.coreui.glyph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;

public record Glyph(String id, int width, int height, int ascent, int advance) {
    public Key font() {
        return Key.key("coreui", "custom/" + id);
    }

    public Component component() {
        return Component.text("\uE000").font(font())
                .color(TextColor.color(0xFFFFFF)).shadowColor(ShadowColor.none());
    }

    public Component component(int verticalOffset) {
        if (verticalOffset < 0 || verticalOffset > 8) {
            throw new IllegalArgumentException("Vertical offset must be between 0 and 8");
        }
        return Component.text(String.valueOf((char) ('\uE000' + verticalOffset))).font(font())
                .color(TextColor.color(0xFFFFFF)).shadowColor(ShadowColor.none());
    }
}
