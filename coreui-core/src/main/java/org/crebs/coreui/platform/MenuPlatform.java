package org.crebs.coreui.platform;

public interface MenuPlatform {
    void show(MenuViewer viewer, RenderedMenu menu);
    void close(MenuViewer viewer);
    void schedule(MenuViewer viewer, Runnable action, long delayTicks);
    long tick(MenuViewer viewer);
    void requireThread();
}
