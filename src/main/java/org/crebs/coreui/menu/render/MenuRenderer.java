package org.crebs.coreui.menu.render;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.crebs.coreui.menu.Metrics;
import org.crebs.coreui.menu.element.*;
import org.crebs.coreui.menu.style.ThemeColour;
import org.crebs.coreui.menu.style.ButtonStyle;
import org.crebs.coreui.menu.session.MenuSession;
import org.crebs.coreui.menu.session.Motion;
import org.crebs.coreui.menu.session.PopupAnimation;
import org.jspecify.annotations.NonNull;

import java.util.*;
import static org.crebs.coreui.menu.render.MenuSkin.*;

public final class MenuRenderer {
    public static final Key CLOSE = Key.key("coreui", "close");

    private static final int QUAD_ADVANCE = 9;
    private static final int HEADER_HEIGHT = 24;
    private static final Component NATIVE_TITLE = Component.text("\uE100".repeat(8192))
            .font(Key.key("coreui", "space")).shadowColor(ShadowColor.none());
    private static final Component LINE_BOUNDS = Component.text("\uE20E")
            .font(Key.key("coreui", "bounds")).shadowColor(ShadowColor.none());
    private static final char ARROW_DOWN = '\uE246';
    private static final char ARROW_UP = '\uE247';
    private static final char CLOSE_DARK = '\uE25D';
    private static final char CLOSE_LIGHT = '\uE25E';
    private static final char TOGGLE_ON_INDICATOR = '\uE268';
    private static final char TOGGLE_OFF_INDICATOR = '\uE269';
    private static final char TOGGLE_ON_INDICATOR_DISABLED = '\uE26A';
    private static final char TOGGLE_OFF_INDICATOR_DISABLED = '\uE26B';
    private static final char ROW_ARROW = '\uE26C';
    private static final char CHECKBOX_CHECKED = '\uE257';
    private static final char CHECKBOX_UNCHECKED = '\uE258';
    private static final char CHECKBOX_CHECKED_PRESSED = '\uE259';
    private static final char CHECKBOX_UNCHECKED_PRESSED = '\uE25A';
    private static final char CHECKBOX_CHECKED_DISABLED = '\uE25B';
    private static final char CHECKBOX_UNCHECKED_DISABLED = '\uE25C';
    private static final int CHECKBOX_ADVANCE = 17;

    private final Map<Integer, Component> rows = new TreeMap<>();
    private final List<HitBox> hitBoxes = new ArrayList<>();
    private final Map<String, Bounds> focusBounds = new HashMap<>();
    private MenuSession session;
    private String font;
    private boolean dark;
    private int foreground;
    private int secondary;
    private Bounds popupArea;
    private PopupAnimation popupMotion;
    private boolean popupPainting;
    private boolean paintingHeader;
    private boolean overlay;
    private Component overlayContent = Component.empty();

    private int contentTop() { return session.getMenu().hasHeader() && !paintingHeader ? HEADER_HEIGHT : 0; }
    private int canvasHeight() { return session.getMenu().getHeight() + (session.getMenu().hasHeader() ? HEADER_HEIGHT : 0); }
    private int canvasRows() { return (canvasHeight() + 8) / 9; }
    private int overlayLine() { return canvasRows() - 1; }

    public Dialog render(MenuSession session, long tick) {
        Component canvas = renderContent(session, tick);
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(NATIVE_TITLE).externalTitle(session.getMenu().getTitle())
                        .pause(false).canCloseWithEscape(true).afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(canvas, 511))).build())
                .type(DialogType.dialogList(RegistrySet.valueSet(RegistryKey.DIALOG, List.of()), null, 1, 1)));
    }

    public Component renderContent(MenuSession session, long tick) {
        MenuRenderer frame = new MenuRenderer();
        frame.session = Objects.requireNonNull(session);
        session.renderTick(tick);
        frame.dark = session.getTheme() == ThemeColour.DARK;
        frame.font = "glyphs_" + (frame.dark ? "dark_" : "light_");
        frame.foreground = frame.dark ? 0xF0F0F0 : 0x252826;
        frame.secondary = frame.dark ? 0xB5B9B6 : 0x575D59;
        return frame.draw();
    }

    private Component draw() {
        session.clearClicks();
        if (session.getMenu().hasHeader()) header();
        quad(0, 0, session.getMenu().getWidth(), session.getMenu().getHeight(), dark ? PANEL_DARK : PANEL_LIGHT);
        List<MenuElement> visible = session.getMenu().getElements().stream()
                .filter(e -> e.visibleOn(session.getActivePage())).toList();
        DropdownElement popup = visible.stream().filter(DropdownElement.class::isInstance)
                .map(DropdownElement.class::cast).filter(e -> session.isExpanded(e) || session.popupAnimation(e) != null)
                .findFirst().orElse(null);
        if (popup != null) {
            popupMotion = session.popupAnimation(popup);
            if (popupMotion == null) popupMotion = new PopupAnimation(1, true,
                    Math.floorMod(session.getRenderTick() - PopupAnimation.DURATION_TICKS, PopupAnimation.CLOCK_TICKS));
        }
        popupArea = popup == null || !session.isExpanded(popup) ? null : popupBounds(popup);
        for (MenuElement element : visible) {
            if (element instanceof PanelElement panel) {
                Bounds b = panel.bounds();
                quad(b.x(), b.y(), b.width(), b.height(), dark ? PANEL_DARK : PANEL_LIGHT);
            }
        }
        for (MenuElement element : visible) {
            if (element instanceof PanelElement) {
                continue;
            }
            switch (element) {
                case LabelElement label -> label(label.bounds().x(), label.bounds().y(), label.properties().label(), secondary, null);
                case ButtonElement button -> button(button);
                case ToggleElement toggle -> toggle(toggle);
                case SliderElement slider -> slider(slider);
                case DropdownElement dropdown -> dropdown(dropdown);
                case DisplayElement display -> display(display);
                default -> throw new IllegalStateException("Unexpected value: " + element);
            }
        }
        Bounds focus = focusBounds.get(session.getFocusedId());
        overlay = true;
        if (focus != null) {
            outline(focus);
        }
        if (popup != null) {
            popupPainting = true;
            options(popup);
            popupPainting = false;
        }
        overlay = false;
        renderHitBoxes();
        Component body = Component.empty().shadowColor(ShadowColor.none());
        int count = canvasRows();
        for (int row = 0; row < count; row++) {
            body = body.append(LINE_BOUNDS).append(space(-481))
                    .append(rows.getOrDefault(row, Component.empty()));
            if (row == count - 1) {
                body = body.append(LINE_BOUNDS.font(Key.key("coreui", "bounds_overlay")))
                        .append(space(-481)).append(overlayContent);
            }
            body = body.append(space(480));
            if (row < count - 1) {
                body = body.append(Component.newline());
            }
        }
        return body;
    }

    private void header() {
        paintingHeader = true;
        int width = session.getMenu().getWidth();
        quad(0, 0, width, 24, dark ? HEADER_DARK : HEADER_LIGHT);
        Component title = session.getMenu().getHeader();
        label(Math.max(8, (width - textWidth(title)) / 2), 8, title, foreground, null);
        place(width - 22, 8, Component.text(String.valueOf(dark ? CLOSE_DARK : CLOSE_LIGHT))
                .color(TextColor.color(0xFFFFFF)).shadowColor(ShadowColor.none()), 9, null);
        hitBox(new Bounds(width - 28, 2, 24, 20), CLOSE);
        paintingHeader = false;
    }

    private void button(ButtonElement element) {
        Bounds b = element.bounds();
        Key click = element.enabled() ? session.registerClick(element, player -> {
            for (MenuElement reset : session.resetValues(element.resetValues().toArray(String[]::new))) {
                switch (reset) {
                    case ToggleElement toggle -> toggle.onChange().accept(player, session.value(toggle));
                    case SliderElement slider -> slider.onChange().accept(player, session.value(slider));
                    case DropdownElement dropdown -> dropdown.onChange().accept(player, session.value(dropdown));
                    default -> { }
                }
            }
            if (element.targetPage() != null) {
                session.navigate(element.targetPage());
            }
            element.onClick().accept(player);
        }) : null;
        if (b.height() == 24) {
            boolean selected = element.selected() || element.style() == ButtonStyle.TAB && element.targetPage().equals(session.getActivePage());
            boolean pressed = element.enabled() && held(element);
            MenuSkin skin = getSkin(element, pressed, selected);
            quad(b.x(), b.y(), b.width(), b.height(), skin);
            if (selected) quad(b.x() + 1, b.y() + b.height() - 3, b.width() - 2, 2, SELECTION_STRIPE);
            Component text = element.properties().label();
            int ink = !element.enabled() ? secondary : element.style() == ButtonStyle.DEFAULT ? 0x222623
                    : element.style() == ButtonStyle.PRIMARY ? 0xFFFFFF : foreground;
            label(b.x() + Math.max(4, (b.width() - textWidth(text)) / 2), b.y() + 8 + (pressed ? 1 : 0), text, ink, click);
        } else {
            row(element, element.properties().description(), click);
            if (element.enabled()) place(b.x() + b.width() - 19, b.y() + 13,
                    Component.text(String.valueOf(ROW_ARROW)).color(TextColor.color(secondary)), 9, click);
        }
        hitBox(b, click);
        if (element.enabled()) focusBounds.put(element.id(), b);
    }

    private @NonNull MenuSkin getSkin(ButtonElement element, boolean pressed, boolean selected) {
        MenuSkin skin = switch (element.style()) {
            case DEFAULT -> pressed || selected ? BUTTON_PRESSED : BUTTON;
            case PRIMARY -> pressed || selected ? PRIMARY_PRESSED : dark ? PRIMARY_DARK : PRIMARY_LIGHT;
            case TAB -> pressed ? (dark ? TAB_PRESSED_DARK : TAB_PRESSED_LIGHT)
                    : selected ? (dark ? TAB_SELECTED_DARK : TAB_SELECTED_LIGHT) : dark ? TAB_DARK : TAB_LIGHT;
        };
        if (!element.enabled()) skin = dark ? BUTTON_DISABLED_DARK : BUTTON_DISABLED_LIGHT;
        return skin;
    }

    private void toggle(ToggleElement element) {
        Bounds b = element.bounds();
        Key click = element.enabled() ? session.registerClick(element, player -> element.onChange().accept(player, session.toggle(element))) : null;
        row(element, element.properties().description(), click);
        boolean on = session.value(element);
        if (element.checkbox()) {
            int x = b.x() + b.width() - 26, y = b.y() + 8;
            char glyph = !element.enabled()
                    ? (on ? CHECKBOX_CHECKED_DISABLED : CHECKBOX_UNCHECKED_DISABLED)
                    : held(element) ? (on ? CHECKBOX_CHECKED_PRESSED : CHECKBOX_UNCHECKED_PRESSED)
                    : (on ? CHECKBOX_CHECKED : CHECKBOX_UNCHECKED);
            place(x, y, Component.text(String.valueOf(glyph)).color(TextColor.color(0xFFFFFF))
                    .shadowColor(ShadowColor.none()), CHECKBOX_ADVANCE, null);
            label(x - 8 - textWidth(element.indicatorLabel()), y + 4, element.indicatorLabel(), secondary, click);
            hitBox(b, click);
            if (element.enabled() && !held(element)) focusBounds.put(element.id(), new Bounds(x - 2, y - 2, 20, 20));
            return;
        }
        int trackX = b.x() + b.width() - Metrics.TOGGLE_WIDTH - Metrics.CONTROL_MARGIN;
        int trackY = b.y() + 8;
        int trackLine = (trackY - 2 + contentTop()) / 9 - contentTop() / 9;
        quad(trackX, trackY, Metrics.TOGGLE_WIDTH, 16,
                !element.enabled() ? TOGGLE_DISABLED : on ? TOGGLE_ON : TOGGLE_OFF, trackLine);
        char indicator = !element.enabled() ? (on ? TOGGLE_ON_INDICATOR_DISABLED : TOGGLE_OFF_INDICATOR_DISABLED)
                : on ? TOGGLE_ON_INDICATOR : TOGGLE_OFF_INDICATOR;
        place(trackX + (on ? 5 : 23), trackY + 4, Component.text(String.valueOf(indicator))
                .color(TextColor.color(0xFFFFFF)).shadowColor(ShadowColor.none()), 9, null);
        int knobX = trackX + 1 + (on ? Metrics.TOGGLE_TRAVEL : 0);
        Motion motion = session.motion(element.id());
        controlKnob(element, knobX - 1, trackY - 2, false, element.enabled(), motion);
        hitBox(b, click);
    }

    private void display(DisplayElement element) {
        Bounds b = element.bounds();
        switch (element.kind()) {
            case DIVIDER -> quad(b.x(), b.y(), b.width(), b.height(), DIVIDER);
            case BADGE -> {
                quad(b.x(), b.y(), b.width(), b.height(), BADGE);
                label(b.x() + 6, b.y() + (b.height() - 8) / 2, element.properties().label(), 0xFFFFFF, null);
            }
            case PROGRESS -> {
                label(b.x(), b.y(), element.properties().label(), foreground, null);
                String percent = Math.round(element.progress() * 100) + "%";
                label(b.x() + b.width() - textWidth(Component.text(percent)), b.y(), Component.text(percent), secondary, null);
                quad(b.x(), b.y() + 15, b.width(), 10, PROGRESS_TRACK);
                int fill = (int) Math.round((b.width() - 4) * element.progress());
                if (fill > 0) quad(b.x() + 2, b.y() + 17, fill, 6, PROGRESS_FILL);
            }
        }
    }

    private void slider(SliderElement element) {
        Bounds b = element.bounds();
        Key click = element.enabled() ? session.registerClick(element, player -> {}) : null;
        row(element, Component.text(session.value(element) + suffix(element)), click);
        int trackLine = (b.y() + 7 + contentTop()) / 9 - contentTop() / 9;
        int trackX = b.x() + Metrics.CONTROL_LEFT + 1;
        int trackWidth = Metrics.trackWidth(b.width());
        int travel = trackWidth - Metrics.THUMB_WIDTH;
        quad(trackX, b.y() + 14, trackWidth, 3, element.enabled() ? SLIDER_TRACK : SLIDER_TRACK_DISABLED, trackLine);
        double progress = (session.value(element) - element.min()) / (double) (element.max() - element.min());
        int filled = (int) Math.round(Math.clamp(progress, 0, 1) * travel);
        if (filled > 0 && element.enabled()) {
            quad(trackX, b.y() + 14, filled, 3, dark ? SLIDER_FILL_DARK : SLIDER_FILL_LIGHT, trackLine);
        }
        int thumbX = trackX + filled;
        Motion motion = session.motion(element.id());
        controlKnob(element, thumbX - 1, b.y() + 7, true, element.enabled(), motion);
        if (!element.enabled()) return;
        int intervals = (element.max() - element.min()) / element.step();
        for (int index = 0; index <= intervals; index++) {
            int target = element.min() + index * element.step();
            Key stepClick = session.registerClick(element, player ->
                    element.onChange().accept(player, session.setValue(element, target)));
            int left = index == 0 ? 0 : (int) Math.round(4 + (index - 0.5) * travel / intervals);
            int right = index == intervals ? b.width() - Metrics.CONTROL_LEFT
                    : (int) Math.round(4 + (index + 0.5) * travel / intervals);
            hitBox(new Bounds(b.x() + Metrics.CONTROL_LEFT + left, b.y(), right - left, b.height()), stepClick);
        }
        hitBox(new Bounds(b.x(), b.y(), Metrics.CONTROL_LEFT, b.height()), click);
    }

    private void controlKnob(MenuElement element, int x, int y, boolean slider, boolean enabled, Motion motion) {
        boolean focused = session.getFocusedId().equals(element.id());
        int palette = !enabled ? (slider ? 5 : 2) : held(element) ? (slider ? 4 : 1)
                : focused ? (slider ? 7 : 6) : (slider ? 3 : 0);
        long start = Math.floorMod(motion == null ? session.getRenderTick() - 4 : motion.tick(), 24000L);
        int distance = motion == null ? 0 : motion.dx();
        int argb = (17 + palette) << 24 | (int) (start & 255) << 16 | (int) (start >> 8) << 8 | (distance + 128);
        place(x, y, Component.text(String.valueOf((char) ('\uE260' + palette)))
                .shadowColor(ShadowColor.shadowColor(argb)), QUAD_ADVANCE, null);
    }

    private static String suffix(SliderElement element) {
        return element.min() == 0 && element.max() == 100 ? "%" : "";
    }

    private void dropdown(DropdownElement element) {
        Bounds b = element.bounds();
        Key click = session.registerClick(element, player -> session.toggleDropdown(element));
        row(element, element.properties().description(), click);
        int boxX = b.x() + Metrics.CONTROL_LEFT + 1;
        int boxWidth = Metrics.controlWidth(b.width());
        quad(boxX, b.y() + 5, boxWidth, 22, held(element) ? DROPDOWN_PRESSED : DROPDOWN);
        label(boxX + 7, b.y() + 11, element.options().get(session.value(element)), 0x101010, click);
        place(boxX + boxWidth - 16, b.y() + 13,
                Component.text(String.valueOf(session.isExpanded(element) ? ARROW_UP : ARROW_DOWN)).color(TextColor.color(0x101010)), 9, click);
        hitBox(b, click);
        if (!held(element)) focusBounds.put(element.id(), new Bounds(boxX - 2, b.y() + 3, boxWidth + 4, 26));
    }

    private Bounds popupBounds(DropdownElement element) {
        Bounds b = element.bounds();
        return new Bounds(b.x() + Metrics.CONTROL_LEFT + 1, b.y() + 36,
                Metrics.controlWidth(b.width()), element.options().size() * 24);
    }

    private void options(DropdownElement element) {
        Bounds b = element.bounds();
        int boxX = b.x() + Metrics.CONTROL_LEFT + 1;
        int boxWidth = Metrics.controlWidth(b.width());
        for (int index = 0; index < element.options().size(); index++) {
            int selected = index;
            Key click = null;
            if (session.isExpanded(element)) {
                click = session.registerClick(element, "option-" + index, player -> {
                    session.select(element, selected);
                    element.onChange().accept(player, selected);
                });
            }
            int y = b.y() + 36 + index * 24;
            boolean chosen = session.value(element) == index;
            boolean pressed = held(element) && session.getPressedPart().equals("option-" + index);
            MenuSkin skin = pressed ? (dark ? DROPDOWN_OPTION_PRESSED : DROPDOWN_OPTION_PRESSED_LIGHT)
                    : chosen ? (dark ? DROPDOWN_OPTION_SELECTED : DROPDOWN_OPTION_SELECTED_LIGHT)
                    : dark ? DROPDOWN_OPTION : DROPDOWN_OPTION_LIGHT;
            quad(boxX, y, boxWidth, 24, skin);
            if (chosen) quad(boxX + 1, y + 1, 2, 22, SELECTION_STRIPE);
            label(boxX + 7, y + 8 + (pressed ? 1 : 0), element.options().get(index), 0x000000, click);
            if (click != null) {
                hitBox(new Bounds(boxX, y, boxWidth, 24), click);
            }
        }
    }

    private void row(MenuElement element, Component description, Key click) {
        Bounds b = element.bounds();
        boolean chosen = element instanceof ButtonElement button && button.selected();
        boolean rowPressed = held(element) && !(element instanceof SliderElement)
                && !(element instanceof ToggleElement toggle && !toggle.checkbox());
        boolean enabled = !(element instanceof ButtonElement button && !button.enabled())
                && !(element instanceof ToggleElement toggle && !toggle.enabled())
                && !(element instanceof SliderElement slider && !slider.enabled());
        MenuSkin skin = !enabled ? (dark ? ROW_DISABLED_DARK : ROW_DISABLED_LIGHT)
                : rowPressed || chosen ? (dark ? ROW_PRESSED_DARK : ROW_PRESSED_LIGHT)
                : dark ? ROW_DARK : ROW_LIGHT;
        quad(b.x(), b.y(), b.width(), Metrics.ROW_HEIGHT, skin);
        if (chosen) quad(b.x() + 1, b.y() + 1, 2, b.height() - 2, SELECTION_STRIPE);
        label(b.x() + 10, b.y() + 7, element.properties().label(), enabled ? foreground : secondary, click);
        label(b.x() + 10, b.y() + 18, description, secondary, click);
    }

    private boolean held(MenuElement element) {
        return session.getPressedId().equals(element.id());
    }

    private void outline(Bounds b) {
        quad(b.x(), b.y(), b.width(), b.height(), FOCUS);
    }

    private void quad(int x, int y, int width, int height, MenuSkin skin) {
        quad(x, y, width, height, skin, y / 9);
    }

    private void quad(int x, int y, int width, int height, MenuSkin skin, int line) {
        if (width <= 0 || height <= 0) {
            return;
        }
        y += contentTop();
        line += contentTop() / 9;
        if (popupPainting) {
            int paint = skin.popupVariant();
            int up = overlayLine() * 9 - y;
            char glyph = (char) ('\uE800' + paint * 336 + up);
            overlayPlace(x, Component.text(String.valueOf(glyph))
                    .shadowColor(ShadowColor.shadowColor(popupMotion.shadow(width, 26 + paint * 32))), QUAD_ADVANCE);
            return;
        }
        int yOffset = Math.clamp(y - line * 9, 0, 15);
        int argb = 1 << 24 | (width & 0xFF) << 16
                | ((width >> 8) | (height & 0x7F) << 1) << 8 | (height >> 7) | (yOffset << 4);
        char glyph = overlay ? (char) ('\uE400' + overlayLine() - line) : skin.glyph();
        Component content = Component.text(String.valueOf(glyph)).shadowColor(ShadowColor.shadowColor(argb));
        if (overlay) {
            overlayPlace(x, content, QUAD_ADVANCE);
        } else {
            placeAtLine(x, line, content, QUAD_ADVANCE, null);
        }
    }

    private record HitBox(Bounds bounds, Key click, boolean popup, boolean header) {
    }

    private void hitBox(Bounds bounds, Key click) {
        if (click == null) return;
        hitBoxes.add(new HitBox(bounds, click, overlay, paintingHeader));
    }

    private void renderHitBoxes() {
        for (HitBox hit : hitBoxes) {
            paintingHeader = hit.header();
            for (Bounds b : hit.header() || hit.popup() || popupArea == null ? List.of(hit.bounds()) : subtract(hit.bounds(), popupArea)) {
            for (int y = 0; y < b.height(); y += 9) {
                int height = Math.min(9, b.height() - y);
                Component content = Component.empty();
                int remaining = b.width();
                for (int bit = 7; bit >= 0; bit--) {
                    int span = 1 << bit;
                    while (remaining >= span) {
                        int code = height == 9 ? 0xE280 + bit : 0xE500 + (height - 1) * 8 + bit;
                        content = content.append(Component.text(String.valueOf((char) code))).append(space(-1));
                        remaining -= span;
                    }
                }
                int baselineAdjustment = height == 9 ? 0 : 8 - height;
                place(b.x(), b.y() + y - baselineAdjustment, content, b.width(), hit.click());
            }
            }
        }
        paintingHeader = false;
    }

    public static List<Bounds> subtract(Bounds source, Bounds cover) {
        if (!source.overlaps(cover)) return List.of(source);
        int left = Math.max(source.x(), cover.x()), top = Math.max(source.y(), cover.y());
        int right = Math.min(source.x() + source.width(), cover.x() + cover.width());
        int bottom = Math.min(source.y() + source.height(), cover.y() + cover.height());
        List<Bounds> pieces = new ArrayList<>();
        if (top > source.y()) pieces.add(new Bounds(source.x(), source.y(), source.width(), top - source.y()));
        if (bottom < source.y() + source.height()) pieces.add(new Bounds(source.x(), bottom, source.width(), source.y() + source.height() - bottom));
        if (left > source.x()) pieces.add(new Bounds(source.x(), top, left - source.x(), bottom - top));
        if (right < source.x() + source.width()) pieces.add(new Bounds(right, top, source.x() + source.width() - right, bottom - top));
        return pieces;
    }

    private void label(int x, int y, Component text, int color, Key click) {
        if (overlay) {
            int up = overlayLine() * 9 - y - contentTop();
            int argb = popupPainting ? popupMotion.shadow(up, 58)
                    : 25 << 24 | (up & 255) << 16 | (up >> 8) << 8 | (dark ? 6 : 5);
            overlayPlace(x, Component.text(PlainTextComponentSerializer.plainText().serialize(text))
                    .shadowColor(ShadowColor.shadowColor(argb)), textWidth(text));
            return;
        }
        if (popupArea != null && new Bounds(x, y, Math.max(1, textWidth(text)), 9).overlaps(popupArea)) click = null;
        place(x, y, text.colorIfAbsent(TextColor.color(color)), textWidth(text), click);
    }

    private void overlayPlace(int x, Component content, int advance) {
        int left = (480 - session.getMenu().getWidth()) / 2 + x;
        overlayContent = overlayContent.append(space(left)).append(content.font(Key.key("coreui", "overlay")))
                .append(space(-left - advance));
    }

    private void place(int x, int y, Component content, int fontAdvance, Key click) {
        y += contentTop();
        content = content.font(Key.key("coreui", font + y % 9));
        int left = (480 - session.getMenu().getWidth()) / 2 + x;
        rows.merge(y / 9, space(left).append(withClick(content, click)).append(space(-left - fontAdvance)), Component::append);
    }

    private void placeAtLine(int x, int line, Component content, int fontAdvance, Key click) {
        content = content.font(Key.key("coreui", font + "0"));
        int left = (480 - session.getMenu().getWidth()) / 2 + x;
        rows.merge(line, space(left).append(withClick(content, click)).append(space(-left - fontAdvance)), Component::append);
    }

    private static Component withClick(Component content, Key click) {
        return click == null ? content : content.clickEvent(ClickEvent.custom(click, null));
    }

    private static Component space(int pixels) {
        return Component.text(String.valueOf(pixels < 0 ? '\uE101' : '\uE100').repeat(Math.abs(pixels)))
                .font(Key.key("coreui", "space"));
    }

    private static int textWidth(Component component) {
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        int width = 0;
        for (char character : text.toCharArray()) {
            width += switch (character) {
                case ' ' -> 4;
                case '!', '.', ',', ':', ';', 'i', '|', '\'' -> 2;
                case '`', 'l' -> 3;
                case '"', '*', 'I', '[', ']', 't' -> 4;
                case 'f', 'k', '<', '>', '(', ')', '{', '}' -> 5;
                case '@', '~' -> 7;
                default -> 6;
            };
        }
        return width;
    }
}
