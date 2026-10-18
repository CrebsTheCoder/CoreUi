package org.crebs.coreui.menu.element;

import org.bukkit.entity.Player;
import java.util.Objects;
import java.util.function.BiConsumer;

public record ToggleElement(ElementProperties properties, boolean initialValue,
                            BiConsumer<Player, Boolean> onChange, boolean checkbox, boolean enabled,
                            net.kyori.adventure.text.Component indicatorLabel) implements MenuElement {

    public ToggleElement(ElementProperties properties, boolean initialValue, BiConsumer<Player, Boolean> onChange) {
        this(properties, initialValue, onChange, false, true, net.kyori.adventure.text.Component.empty());
    }

    public ToggleElement {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(onChange, "onChange");
        Objects.requireNonNull(indicatorLabel);
        Bounds b = properties.bounds();
        if (b.height() != 33 || b.width() < 190 || b.width() > 470) {
            throw new IllegalArgumentException("Toggle rows are 33 tall and 190..470 wide");
        }
    }
}
