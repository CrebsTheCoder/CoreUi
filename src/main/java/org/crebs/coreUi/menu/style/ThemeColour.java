package org.crebs.coreUi.menu.enums;

public enum ThemeColour {
    LIGHT(0xC6C6C6, 0xFFFFFF),
    DARK(0x303431, 0xFFFFFF);

    public final int panel;
    public final int text;

    ThemeColour(int panel, int text) {
        this.panel = panel;
        this.text = text;
    }
}
