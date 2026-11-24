package org.crebs.coreui.menu.session;

import net.kyori.adventure.text.Component;
import java.util.Map;

public record MenuValues(Map<String, Boolean> toggles, Map<String, Integer> sliders,
                         Map<String, Component> dropdowns) {
    public static final MenuValues EMPTY = new MenuValues(Map.of(), Map.of(), Map.of());
    public MenuValues {
        toggles = Map.copyOf(toggles);
        sliders = Map.copyOf(sliders);
        dropdowns = Map.copyOf(dropdowns);
    }
}
