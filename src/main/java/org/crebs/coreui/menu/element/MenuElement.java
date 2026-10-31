package org.crebs.coreui.menu.element;

public sealed interface MenuElement permits LabelElement, ButtonElement, ToggleElement, SliderElement, DropdownElement, PanelElement, DisplayElement, GlyphElement {

    ElementProperties properties();

    default String id() {
        return properties().id();
    }

    default Bounds bounds() {
        return properties().bounds();
    }

    default String page() {
        return properties().page();
    }

    default boolean visibleOn(String page) {
        return page().equals("*") || page().equals(page);
    }
}
