package org.crebs.coreui.menu.render;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;

public final class TextPosition {
    public static final int MARKER_COLOR = 0xFCFDEF;
    public static final int ALPHA_BASE = 128;

    private TextPosition() {
    }

    public static Component encode(Component component, int offset) {
        if (offset < 0 || offset > 8) throw new IllegalArgumentException("Offset must be between 0 and 8");
        return encode(component, offset, TextColor.color(0xFFFFFF));
    }

    private static Component encode(Component component, int offset, TextColor inherited) {
        TextColor color = component.color() == null ? inherited : component.color();
        return component.color(TextColor.color(MARKER_COLOR))
                .shadowColor(ShadowColor.shadowColor((ALPHA_BASE + offset) << 24 | color.value()))
                .children(component.children().stream().map(child -> encode(child, offset, color)).toList());
    }
}
