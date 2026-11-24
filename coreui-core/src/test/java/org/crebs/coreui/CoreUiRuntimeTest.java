package org.crebs.coreui;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.crebs.coreui.menu.CoreMenu;
import org.crebs.coreui.menu.element.ButtonElement;
import org.crebs.coreui.menu.element.ToggleElement;
import org.crebs.coreui.menu.style.ThemeColour;
import org.crebs.coreui.platform.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

class CoreUiRuntimeTest {
    private final FakePlatform platform = new FakePlatform();
    private final CoreUiRuntime runtime = new CoreUiRuntime(platform, Logger.getAnonymousLogger());
    private final Viewer viewer = new Viewer(UUID.randomUUID());

    @Test
    void rendersAndDispatchesClicksWithoutBukkit() {
        AtomicInteger calls = new AtomicInteger();
        CoreMenu menu = CoreMenu.builder("test").title("Test").theme(ThemeColour.LIGHT)
            .button("button").position(10, 10).onClick(v -> {
                assertEquals(viewer.id(), v.id());
                calls.incrementAndGet();
            }).add().build();
        runtime.open(viewer, menu);
        assertNotNull(platform.frame);
        var session = runtime.getSession(viewer).orElseThrow();
        ButtonElement button = (ButtonElement) menu.getElements().getFirst();
        Key key = session.registerClick(button, button.onClick());
        runtime.handleClick(viewer, key);
        assertEquals(0, calls.get());
        platform.flush();
        assertEquals(1, calls.get());
        runtime.handleClick(viewer, key);
        platform.flush();
        assertEquals(1, calls.get());
    }

    @Test
    void delayedClickCannotAffectReplacementMenu() {
        AtomicInteger calls = new AtomicInteger();
        CoreMenu menu = toggleMenu();
        runtime.open(viewer, menu);
        var session = runtime.getSession(viewer).orElseThrow();
        Key key = session.registerClick(menu.getElements().getFirst(), v -> calls.incrementAndGet());
        runtime.handleClick(viewer, key);
        runtime.open(viewer, menu);
        platform.flush();
        assertEquals(0, calls.get());
    }

    @Test
    void remembersValuesAcrossReconnectAndKeepsViewersIsolated() {
        CoreMenu menu = toggleMenu();
        ToggleElement toggle = (ToggleElement) menu.getElements().getFirst();
        runtime.open(viewer, menu);
        runtime.getSession(viewer).orElseThrow().toggle(toggle);
        runtime.close(viewer);
        runtime.open(viewer, menu);
        assertTrue(runtime.getSession(viewer).orElseThrow().value(toggle));
        Viewer other = new Viewer(UUID.randomUUID());
        runtime.open(other, menu);
        assertFalse(runtime.getSession(other).orElseThrow().value(toggle));
        runtime.disconnect(viewer);
        runtime.open(viewer, menu);
        assertTrue(runtime.getSession(viewer).orElseThrow().value(toggle));
    }

    private CoreMenu toggleMenu() {
        return CoreMenu.builder("test").title("Test").theme(ThemeColour.LIGHT).rememberValues()
            .toggle("toggle").position(10, 10).add().build();
    }

    private record Viewer(UUID id) implements MenuViewer {
        public boolean online() { return true; }
        public Audience audience() { return Audience.empty(); }
        public <T> T unwrap(Class<T> type) { throw new UnsupportedOperationException(); }
    }

    private static final class FakePlatform implements MenuPlatform {
        private RenderedMenu frame;
        private final List<Runnable> pending = new ArrayList<>();
        public void show(MenuViewer viewer, RenderedMenu menu) { frame = menu; }
        public void close(MenuViewer viewer) { frame = null; }
        public long tick(MenuViewer viewer) { return 0; }
        public void schedule(MenuViewer viewer, Runnable action, long delay) { pending.add(action); }
        public void requireThread() {}
        void flush() {
            var tasks = List.copyOf(pending);
            pending.clear();
            tasks.forEach(Runnable::run);
        }
    }
}
