package org.crebs.coreUi.menu.element;

import org.bukkit.entity.Player;
import org.crebs.coreUi.menu.Metrics;
import java.util.Objects;
import java.util.function.BiConsumer;

public record SliderElement(ElementProperties properties, int min, int max, int step, int initialValue, BiConsumer<Player, Integer> onChange, boolean enabled) implements MenuElement {
    public SliderElement(ElementProperties properties, int min, int max, int step, int initialValue, BiConsumer<Player, Integer> onChange) {
        this(properties, min, max, step, initialValue, onChange, true);
    }

    public SliderElement {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(onChange, "onChange");
        Bounds b = properties.bounds();
        int travel = Metrics.travel(b.width());
        long range = (long) max - min;
        if (b.height() != 33 || b.width() < 190 || b.width() > 470) {
            throw new IllegalArgumentException("Slider rows are 33 tall and 190..470 wide");
        }
        if (range < 1 || range > 10000 || step < 1 || range % step != 0 || range / step > travel) {
            throw new IllegalArgumentException("Slider requires an evenly stepped range with 1.." + travel + " intervals");
        }
        if (initialValue < min || initialValue > max || ((long) initialValue - min) % step != 0) {
            throw new IllegalArgumentException("Initial slider value must be in range and on a step");
        }
    }

    public int snap(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Slider value must be finite");
        }
        long snapped = (long) min + Math.round((Math.clamp(value, min, max) - min) / step) * step;
        return (int) Math.clamp(snapped, min, max);
    }
}
