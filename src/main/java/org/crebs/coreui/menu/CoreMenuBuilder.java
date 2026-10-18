package org.crebs.coreui.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.crebs.coreui.menu.element.*;
import org.crebs.coreui.menu.style.ThemeColour;
import org.crebs.coreui.menu.style.ButtonStyle;
import java.util.*;
import java.util.function.*;

public final class CoreMenuBuilder {
    private final String id;
    private Component title;
    private boolean headerEnabled;
    private Component header;
    private String defaultPage = "main";
    private ThemeColour theme;
    private int width = 300;
    private int height = 240;
    private boolean rememberValues;
    private final List<MenuElement> elements = new ArrayList<>();
    private final Set<String> elementIds = new HashSet<>();

    public CoreMenuBuilder(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Menu id isn't set");
        }
        this.id = id;
    }

    public CoreMenuBuilder title(Component value) {
        title = Objects.requireNonNull(value);
        return this;
    }

    public CoreMenuBuilder theme(ThemeColour value) {
        theme = Objects.requireNonNull(value);
        return this;
    }

    public CoreMenuBuilder header() {
        headerEnabled = true;
        return this;
    }

    public CoreMenuBuilder header(Component value) {
        headerEnabled = true;
        header = Objects.requireNonNull(value);
        return this;
    }

    public CoreMenuBuilder rememberValues() {
        rememberValues = true;
        return this;
    }

    public CoreMenuBuilder defaultPage(String value) {
        if (value == null || value.isBlank() || value.equals("*")) {
            throw new IllegalArgumentException("Invalid default page");
        }
        defaultPage = value;
        return this;
    }

    public CoreMenuBuilder size(int width, int height) {
        if (width < 140 || width > 480 || height < 90 || height > 320) {
            throw new IllegalArgumentException("Menu size must be within 140x90 and 480x320");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    public CoreMenuBuilder addElement(MenuElement element) {
        Objects.requireNonNull(element);
        if (!elementIds.add(element.id())) {
            throw new IllegalArgumentException("Duplicate element id: " + element.id());
        }
        elements.add(element);
        return this;
    }

    public LabelBuilder label(String id, Component text) {
        return new LabelBuilder(id).label(text);
    }

    public ButtonBuilder button(String id) {
        return new ButtonBuilder(id);
    }

    public ToggleBuilder toggle(String id) {
        return new ToggleBuilder(id);
    }

    public SliderBuilder slider(String id) {
        return new SliderBuilder(id);
    }

    public DropdownBuilder dropdown(String id) {
        return new DropdownBuilder(id);
    }

    public PanelBuilder panel(String id) {
        return new PanelBuilder(id);
    }

    public ButtonBuilder tab(String id, String targetPage) {
        return button(id).compact().style(ButtonStyle.TAB).targetPage(targetPage);
    }

    public ToggleBuilder checkbox(String id) { return toggle(id).checkbox(); }
    public DisplayBuilder badge(String id) { return new DisplayBuilder(id, DisplayElement.Kind.BADGE).size(90, 18); }
    public DisplayBuilder progress(String id) { return new DisplayBuilder(id, DisplayElement.Kind.PROGRESS).size(200, 30); }
    public DisplayBuilder divider(String id) { return new DisplayBuilder(id, DisplayElement.Kind.DIVIDER).size(width - 20, 1); }

    public CoreMenu build() {
        if (title == null) {
            throw new IllegalStateException("Menu title isn't set");
        }
        if (theme == null) {
            throw new IllegalStateException("Theme isn't set");
        }
        if (elements.stream().noneMatch(e -> e.page().equals(defaultPage))) {
            throw new IllegalStateException("Default page has no elements");
        }
        for (MenuElement element : elements) {
            Bounds b = element.bounds();
            if ((long) b.x() + b.width() > width || (long) b.y() + b.height() > height) {
                throw new IllegalStateException("Element is outside the menu: " + element.id());
            }
            if (element instanceof DropdownElement dropdown && b.y() + 36 + dropdown.options().size() * 24 > height) {
                throw new IllegalStateException("Dropdown options extend outside the menu: " + element.id());
            }
            if (element instanceof ButtonElement button && button.targetPage() != null
                    && elements.stream().noneMatch(e -> e.page().equals(button.targetPage()))) {
                throw new IllegalStateException("Unknown target page: " + button.targetPage());
            }
            if (element instanceof ButtonElement button) {
                for (String target : button.resetValues()) {
                    MenuElement control = elements.stream().filter(e -> e.id().equals(target)).findFirst()
                            .orElseThrow(() -> new IllegalStateException("Unknown reset control: " + target));
                    if (!(control instanceof ToggleElement || control instanceof SliderElement || control instanceof DropdownElement)) {
                        throw new IllegalStateException("Reset target has no value: " + target);
                    }
                }
            }
        }
        Component visibleHeader = headerEnabled ? Objects.requireNonNullElse(header, title) : null;
        return new CoreMenu(id, title, visibleHeader,
                defaultPage, theme, width, height, rememberValues, elements);
    }

    public abstract class ElementBuilder<B extends ElementBuilder<B>> {
        protected final String id;
        protected int x;
        protected int y;
        protected int width = -1;
        protected int height = 33;
        protected String page = defaultPage;
        protected Component label = Component.empty();
        protected Component description = Component.empty();
        private boolean added;

        protected ElementBuilder(String id) {
            this.id = id;
        }

        protected abstract B self();

        protected abstract MenuElement create();

        public B position(int x, int y) {
            this.x = x;
            this.y = y;
            return self();
        }

        public B size(int width, int height) {
            this.width = width;
            this.height = height;
            return self();
        }

        public B page(String page) {
            this.page = page;
            return self();
        }

        public B label(Component value) {
            label = Objects.requireNonNull(value);
            return self();
        }

        public B description(Component value) {
            description = Objects.requireNonNull(value);
            return self();
        }

        protected int resolvedWidth() {
            if (width >= 0) {
                return width;
            }
            return Math.max(60, CoreMenuBuilder.this.width - 2 * x - 2);
        }

        protected int resolvedHeight() {
            return height;
        }

        protected ElementProperties properties() {
            return new ElementProperties(id, new Bounds(x, y, resolvedWidth(), resolvedHeight()), page, label, description);
        }

        public CoreMenuBuilder add() {
            if (added) {
                throw new IllegalStateException("Element already added: " + id);
            }
            addElement(create());
            added = true;
            return CoreMenuBuilder.this;
        }
    }

    public final class LabelBuilder extends ElementBuilder<LabelBuilder> {
        private LabelBuilder(String id) {
            super(id);
            height = 9;
        }

        protected LabelBuilder self() {
            return this;
        }

        protected MenuElement create() {
            return new LabelElement(properties());
        }
    }

    public final class PanelBuilder extends ElementBuilder<PanelBuilder> {
        private PanelBuilder(String id) {
            super(id);
            height = -1;
        }

        protected PanelBuilder self() {
            return this;
        }

        @Override
        protected int resolvedWidth() {
            if (width >= 0) {
                return width;
            }
            return Math.max(8, CoreMenuBuilder.this.width - 2 * x);
        }

        @Override
        protected int resolvedHeight() {
            if (height >= 0) {
                return height;
            }
            return Math.max(8, CoreMenuBuilder.this.height - 2 * y);
        }

        protected MenuElement create() {
            return new PanelElement(properties());
        }
    }

    public final class ButtonBuilder extends ElementBuilder<ButtonBuilder> {
        private ButtonStyle style = ButtonStyle.DEFAULT;
        private boolean selected;
        public ButtonBuilder selected(boolean value) { selected = value; return this; }
        private boolean enabled = true;
        public ButtonBuilder style(ButtonStyle value) { style = Objects.requireNonNull(value); return this; }
        public ButtonBuilder disabled() { enabled = false; return this; }
        private String targetPage;
        private List<String> resetValues = List.of();
        private Consumer<Player> onClick = player -> {
        };

        private ButtonBuilder(String id) {
            super(id);
        }

        protected ButtonBuilder self() {
            return this;
        }

        public ButtonBuilder compact() {
            return size(110, 24);
        }

        public ButtonBuilder targetPage(String value) {
            targetPage = Objects.requireNonNull(value);
            return this;
        }

        public ButtonBuilder onClick(Consumer<Player> value) {
            onClick = Objects.requireNonNull(value);
            return this;
        }

        public ButtonBuilder resetValues(String... ids) {
            resetValues = List.of(ids);
            return this;
        }

        protected MenuElement create() {
            return new ButtonElement(properties(), targetPage, resetValues, onClick, style, enabled, selected);
        }
    }

    public final class ToggleBuilder extends ElementBuilder<ToggleBuilder> {
        private boolean checkbox;
        private Component indicatorLabel = Component.empty();
        public ToggleBuilder indicatorLabel(Component value) { indicatorLabel = Objects.requireNonNull(value); return this; }
        private boolean enabled = true;
        public ToggleBuilder checkbox() { checkbox = true; return this; }
        public ToggleBuilder disabled() { enabled = false; return this; }
        private boolean initialValue;
        private BiConsumer<Player, Boolean> onChange = (player, value) -> {
        };

        private ToggleBuilder(String id) {
            super(id);
        }

        protected ToggleBuilder self() {
            return this;
        }

        public ToggleBuilder initialValue(boolean value) {
            initialValue = value;
            return this;
        }

        public ToggleBuilder onChange(BiConsumer<Player, Boolean> value) {
            onChange = Objects.requireNonNull(value);
            return this;
        }

        protected MenuElement create() {
            return new ToggleElement(properties(), initialValue, onChange, checkbox, enabled, indicatorLabel);
        }
    }

    public final class DisplayBuilder extends ElementBuilder<DisplayBuilder> {
        private final DisplayElement.Kind kind;
        private double progress;
        private DisplayBuilder(String id, DisplayElement.Kind kind) { super(id); this.kind = kind; }
        public DisplayBuilder value(double value) { progress = value; return this; }
        protected DisplayBuilder self() { return this; }
        protected MenuElement create() { return new DisplayElement(properties(), kind, progress); }
    }

    public final class SliderBuilder extends ElementBuilder<SliderBuilder> {
        private boolean enabled = true;
        public SliderBuilder disabled() { enabled = false; return this; }
        private int min = 0;
        private int max = 100;
        private int step = 5;
        private int initialValue = 0;
        private BiConsumer<Player, Integer> onChange = (player, value) -> {
        };

        private SliderBuilder(String id) {
            super(id);
        }

        protected SliderBuilder self() {
            return this;
        }

        public SliderBuilder range(int min, int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public SliderBuilder step(int value) {
            step = value;
            return this;
        }

        public SliderBuilder initialValue(int value) {
            initialValue = value;
            return this;
        }

        public SliderBuilder onChange(BiConsumer<Player, Integer> value) {
            onChange = Objects.requireNonNull(value);
            return this;
        }

        protected MenuElement create() {
            return new SliderElement(properties(), min, max, step, initialValue, onChange, enabled);
        }
    }

    public final class DropdownBuilder extends ElementBuilder<DropdownBuilder> {
        private List<Component> options = List.of();
        private int initialValue;
        private BiConsumer<Player, Integer> onChange = (player, value) -> {
        };

        private DropdownBuilder(String id) {
            super(id);
        }

        protected DropdownBuilder self() {
            return this;
        }

        public DropdownBuilder options(Component... values) {
            options = List.of(values);
            return this;
        }

        public DropdownBuilder initialValue(int value) {
            initialValue = value;
            return this;
        }

        public DropdownBuilder onChange(BiConsumer<Player, Integer> value) {
            onChange = Objects.requireNonNull(value);
            return this;
        }

        protected MenuElement create() {
            return new DropdownElement(properties(), options, initialValue, onChange);
        }
    }
}
