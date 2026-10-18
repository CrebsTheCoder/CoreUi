package org.crebs.coreui.menu.element;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiConsumer;

public record DropdownElement(ElementProperties properties, List<Component> options, int initialValue, BiConsumer<Player, Integer> onChange) implements MenuElement {

    public DropdownElement {
        Objects.requireNonNull(properties);
        options = List.copyOf(options);
        Objects.requireNonNull(onChange, "onChange");
        if (options.isEmpty() || options.size() > 8 || initialValue < 0 || initialValue >= options.size()) {
            throw new IllegalArgumentException("Dropdown needs 1..8 options and a valid selected index");
        }
        if (new HashSet<>(options).size() != options.size()) {
            throw new IllegalArgumentException("Dropdown options must be unique");
        }
        Bounds b = properties.bounds();
        if (b.height() != 33 || b.width() < 190 || b.width() > 470) {
            throw new IllegalArgumentException("Dropdown rows are 33 tall and 190..470 wide");
        }
    }
}
