package org.crebs.coreUi.menu.element;

import org.bukkit.entity.Player;
import java.util.Objects;
import java.util.List;
import java.util.HashSet;
import java.util.function.Consumer;

public record ButtonElement(ElementProperties properties, String targetPage, List<String> resetValues, Consumer<Player> onClick,
                            org.crebs.coreUi.menu.style.ButtonStyle style, boolean enabled, boolean selected) implements MenuElement {

    public ButtonElement(ElementProperties properties, String targetPage, List<String> resetValues, Consumer<Player> onClick) {
        this(properties, targetPage, resetValues, onClick, org.crebs.coreUi.menu.style.ButtonStyle.DEFAULT, true, false);
    }

    public ButtonElement {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(onClick, "onClick");
        Objects.requireNonNull(style, "style");
        if (style == org.crebs.coreUi.menu.style.ButtonStyle.TAB && targetPage == null) {
            throw new IllegalArgumentException("Tabs require a target page");
        }

        resetValues = List.copyOf(resetValues);
        if (resetValues.stream().anyMatch(String::isBlank) || new HashSet<>(resetValues).size() != resetValues.size()) {
            throw new IllegalArgumentException("Reset control IDs must be non-blank and unique");
        }
        if (targetPage != null && (targetPage.isBlank() || targetPage.equals("*"))) {
            throw new IllegalArgumentException("Invalid target page");
        }

        Bounds b = properties.bounds();
        boolean row = b.height() == 33 && b.width() >= 190 && b.width() <= 470;
        boolean compact = b.height() == 24 && b.width() >= 60 && b.width() <= 470;
        if (!(row || compact)) {
            throw new IllegalArgumentException("Buttons are 33-tall rows (190..470 wide) or 24-tall compact buttons (60..470 wide)");
        }
    }
}
