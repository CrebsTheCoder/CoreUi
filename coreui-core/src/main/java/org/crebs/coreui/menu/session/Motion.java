package org.crebs.coreui.menu.session;

public record Motion(int dx, int dy, long tick) {
    static final int MAX_DISTANCE = 127;
    private static final long CLOCK_TICKS = 24000;

    public Motion {
        if (dx != 0 && dy != 0) {
            throw new IllegalArgumentException("A motion runs along one axis");
        }
        if (Math.abs(dx) > MAX_DISTANCE || Math.abs(dy) > MAX_DISTANCE) {
            throw new IllegalArgumentException("A motion covers at most " + MAX_DISTANCE + " pixels");
        }
        if (tick < -1) {
            throw new IllegalArgumentException("A motion's tick is unstamped or a world tick");
        }
    }

    public boolean fresh(long now) {
        return Math.floorMod(now - tick, CLOCK_TICKS) <= 8;
    }
}
