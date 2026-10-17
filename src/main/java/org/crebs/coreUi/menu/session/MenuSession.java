package org.crebs.coreUi.menu.session;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.crebs.coreUi.menu.CoreMenu;
import org.crebs.coreUi.menu.Metrics;
import org.crebs.coreUi.menu.element.*;
import org.crebs.coreUi.menu.enums.ThemeColour;
import java.util.*;
import java.util.function.Consumer;

public final class MenuSession {
    private final CoreMenu menu;
    private String activePage;
    private ThemeColour theme;
    private final Map<String, Object> values = new HashMap<>();
    private PopupAnimation dropdownAnimation;
    private final Map<String, Motion> motions = new HashMap<>();
    private final Map<Key, Interaction> clicks = new HashMap<>();
    private String focusedId = "";
    private String pressedId = "";
    private String pressedPart = "";
    private String dropdownId = "";
    private boolean dropdownExpanded;
    private long lastInteraction = System.currentTimeMillis();
    private long renderTick;

    public record Interaction(String elementId, Consumer<Player> callback, String part) {
    }

    public MenuSession(CoreMenu menu) {
        this(menu, MenuValues.EMPTY);
    }

    public MenuSession(CoreMenu menu, MenuValues saved) {
        this.menu = Objects.requireNonNull(menu);
        Objects.requireNonNull(saved);
        activePage = menu.getDefaultPage();
        theme = menu.getTheme();
        for (MenuElement element : menu.getElements()) {
            switch (element) {
                case ToggleElement toggle -> values.put(element.id(), saved.toggles().getOrDefault(element.id(), toggle.initialValue()));
                case SliderElement slider -> {
                    int value = saved.sliders().getOrDefault(element.id(), slider.initialValue());
                    values.put(element.id(), slider.snap(value) == value ? value : slider.initialValue());
                }
                case DropdownElement dropdown -> {
                    var option = saved.dropdowns().get(element.id());
                    int selected = option == null ? -1 : dropdown.options().indexOf(option);
                    values.put(element.id(), selected < 0 ? dropdown.initialValue() : selected);
                }
                default -> { }
            }
        }
    }

    public MenuValues snapshot() {
        Map<String, Boolean> toggles = new HashMap<>();
        Map<String, Integer> sliders = new HashMap<>();
        Map<String, net.kyori.adventure.text.Component> dropdowns = new HashMap<>();
        for (MenuElement element : menu.getElements()) {
            switch (element) {
                case ToggleElement toggle -> toggles.put(element.id(), value(toggle));
                case SliderElement slider -> sliders.put(element.id(), value(slider));
                case DropdownElement dropdown -> dropdowns.put(element.id(), dropdown.options().get(value(dropdown)));
                default -> { }
            }
        }
        return new MenuValues(toggles, sliders, dropdowns);
    }

    public List<MenuElement> resetValues(String... ids) {
        List<MenuElement> targets = new ArrayList<>();
        for (String id : new LinkedHashSet<>(Arrays.asList(ids))) {
            MenuElement element = menu.getElements().stream().filter(e -> e.id().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown control: " + id));
            if (!(element instanceof ToggleElement || element instanceof SliderElement || element instanceof DropdownElement)) {
                throw new IllegalArgumentException("Control has no value: " + id);
            }
            targets.add(element);
        }
        List<MenuElement> changed = new ArrayList<>();
        for (MenuElement element : targets) {
            boolean updated = switch (element) {
                case ToggleElement toggle -> {
                    boolean differs = value(toggle) != toggle.initialValue();
                    if (differs) {
                        toggle(toggle);
                    }
                    yield differs;
                }
                case SliderElement slider -> {
                    boolean differs = value(slider) != slider.initialValue();
                    if (differs) {
                        setValue(slider, slider.initialValue());
                    }
                    yield differs;
                }
                case DropdownElement dropdown -> {
                    boolean differs = value(dropdown) != dropdown.initialValue();
                    select(dropdown, dropdown.initialValue());
                    yield differs;
                }
                default -> false;
            };
            if (updated) {
                changed.add(element);
            }
        }
        return List.copyOf(changed);
    }

    public CoreMenu getMenu() {
        return menu;
    }

    public String getActivePage() {
        return activePage;
    }

    public ThemeColour getTheme() {
        return theme;
    }

    public void setTheme(ThemeColour value) {
        theme = Objects.requireNonNull(value);
    }

    public String getFocusedId() {
        return focusedId;
    }

    public void focus(String id) {
        if (menu.getElements().stream().noneMatch(e -> e.id().equals(id) && e.visibleOn(activePage))) {
            throw new IllegalArgumentException("Unknown visible element: " + id);
        }
        focusedId = id;
    }


    public boolean value(ToggleElement element) {
        requireElement(element);
        return (boolean) values.get(element.id());
    }

    public int value(SliderElement element) {
        requireElement(element);
        return (int) values.get(element.id());
    }

    public int value(DropdownElement element) {
        requireElement(element);
        return (int) values.get(element.id());
    }

    public boolean toggle(ToggleElement element) {
        boolean value = !value(element);
        values.put(element.id(), value);
        motion(element.id(), value ? -Metrics.TOGGLE_TRAVEL : Metrics.TOGGLE_TRAVEL, 0);
        return value;
    }

    public int setValue(SliderElement element, double requested) {
        int target = element.snap(requested);
        int previous = value(element);
        values.put(element.id(), target);
        if (previous != target) {
            int travel = Metrics.travel(element.bounds().width());
            int range = element.max() - element.min();
            int from = (int) Math.round((double) (previous - element.min()) / range * travel);
            int to = (int) Math.round((double) (target - element.min()) / range * travel);
            motion(element.id(), Math.clamp(from - to, -Motion.MAX_DISTANCE, Motion.MAX_DISTANCE), 0);
        }
        return target;
    }

    public void select(DropdownElement element, int selected) {
        requireElement(element);
        if (selected < 0 || selected >= element.options().size()) {
            throw new IllegalArgumentException("Invalid option");
        }
        values.put(element.id(), selected);
        closeDropdown();
    }

    public void toggleDropdown(DropdownElement element) {
        requireElement(element);
        double from = dropdownProgress(element);
        if (!dropdownId.equals(element.id())) {
            dropdownId = element.id();
            dropdownExpanded = false;
        }
        dropdownExpanded = !dropdownExpanded;
        dropdownAnimation = new PopupAnimation(from, dropdownExpanded, -1);
    }

    public void closeDropdown() {
        if (dropdownId.isEmpty() || !dropdownExpanded) {
            return;
        }
        dropdownAnimation = new PopupAnimation(dropdownAnimation == null ? 1 : dropdownAnimation.value(renderTick), false, -1);
        dropdownExpanded = false;
    }

    public boolean isExpanded(DropdownElement element) {
        return dropdownExpanded && dropdownId.equals(element.id());
    }

    public double dropdownProgress(DropdownElement element) {
        return dropdownId.equals(element.id())
                ? dropdownAnimation == null ? (dropdownExpanded ? 1 : 0) : dropdownAnimation.value(renderTick) : 0;
    }

    public PopupAnimation popupAnimation(DropdownElement element) {
        if (!dropdownId.equals(element.id()) || dropdownAnimation == null) return null;
        dropdownAnimation = dropdownAnimation.stamp(renderTick);
        return dropdownAnimation.active(renderTick) ? dropdownAnimation : null;
    }

    public void renderTick(long tick) {
        renderTick = tick;
    }

    public long getRenderTick() {
        return renderTick;
    }

    public void motion(String id, int dx, int dy) {
        motions.put(id, new Motion(dx, dy, -1));
    }

    public Motion motion(String id) {
        Motion motion = motions.get(id);
        if (motion == null) {
            return null;
        }
        if (motion.tick() < 0) {
            motion = new Motion(motion.dx(), motion.dy(), renderTick);
            motions.put(id, motion);
        }
        if (!motion.fresh(renderTick)) {
            motions.remove(id);
            return null;
        }
        return motion;
    }

    public void press(String id) {
        press(id, "");
    }

    public void press(String id, String part) {
        pressedId = id;
        pressedPart = Objects.requireNonNull(part);
    }

    public void release() {
        pressedId = "";
        pressedPart = "";
    }

    public String getPressedId() {
        return pressedId;
    }

    public String getPressedPart() { return pressedPart; }

    public void navigate(String page) {
        if (page == null || page.equals("*") || menu.getElements().stream().noneMatch(e -> e.page().equals(page))) {
            throw new IllegalArgumentException("Unknown page: " + page);
        }
        activePage = page;
        focusedId = "";
        pressedId = "";
        pressedPart = "";
        dropdownId = "";
        dropdownExpanded = false;
        dropdownAnimation = null;
        motions.clear();
        clicks.clear();
    }

    public void clearClicks() {
        clicks.clear();
    }

    public Key registerClick(MenuElement element, Consumer<Player> action) {
        return registerClick(element, "", action);
    }

    public Key registerClick(MenuElement element, String part, Consumer<Player> action) {
        requireElement(element);
        if (!element.visibleOn(activePage)) {
            throw new IllegalArgumentException("Element isn't on the active page");
        }
        Key key = Key.key("coreui", "click/" + UUID.randomUUID());
        clicks.put(key, new Interaction(element.id(), Objects.requireNonNull(action), Objects.requireNonNull(part)));
        return key;
    }

    public Interaction consumeClick(Key key) {
        Interaction interaction = clicks.remove(key);
        if (interaction == null) {
            return null;
        }
        clicks.clear();
        if (!interaction.elementId().equals(dropdownId) && dropdownExpanded) {
            closeDropdown();
        }
        focusedId = interaction.elementId();
        lastInteraction = System.currentTimeMillis();
        return interaction;
    }

    public boolean expired() {
        return System.currentTimeMillis() - lastInteraction > 600_000L;
    }

    public void touch() {
        lastInteraction = System.currentTimeMillis();
    }

    private void requireElement(MenuElement element) {
        if (menu.getElements().stream().noneMatch(candidate -> candidate == element)) {
            throw new IllegalArgumentException("Element does not belong to this menu: " + element.id());
        }
    }
}
