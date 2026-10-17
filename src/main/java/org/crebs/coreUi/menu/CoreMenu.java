package org.crebs.coreUi.menu;

import net.kyori.adventure.text.Component;
import org.crebs.coreUi.menu.enums.ThemeColour;
import org.crebs.coreUi.menu.element.MenuElement;
import java.util.List;

public final class CoreMenu {
    private final String id;
    private final Component title;
    private final Component header;
    private final String defaultPage;
    private final ThemeColour theme;
    private final int width;
    private final int height;
    private final boolean rememberValues;
    private final List<MenuElement> elements;

    CoreMenu(String id, Component title, Component header, String defaultPage, ThemeColour theme,
             int width, int height, boolean rememberValues, List<MenuElement> elements) {
        this.id = id;
        this.title = title;
        this.header = header;
        this.defaultPage = defaultPage;
        this.theme = theme;
        this.width = width;
        this.height = height;
        this.rememberValues = rememberValues;
        this.elements = List.copyOf(elements);
    }

    public static CoreMenuBuilder builder(String id) {
        return new CoreMenuBuilder(id);
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public Component getHeader() { return header; }

    public boolean hasHeader() { return header != null; }

    public String getDefaultPage() {
        return defaultPage;
    }

    public ThemeColour getTheme() {
        return theme;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean remembersValues() {
        return rememberValues;
    }

    public List<MenuElement> getElements() {
        return elements;
    }
}
