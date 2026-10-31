package org.crebs.coreui.menu.element;

import org.crebs.coreui.glyph.Glyph;
import java.util.Objects;

public record GlyphElement(ElementProperties properties, Glyph glyph) implements MenuElement {
    public GlyphElement {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(glyph);
    }
}
