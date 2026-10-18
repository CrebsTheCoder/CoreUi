package org.crebs.coreui.menu.session;

public record PopupAnimation(double from, boolean opening, long tick) {
    public static final int DURATION_TICKS = 4;
    public static final int CLOCK_TICKS = 24000;

    public PopupAnimation {
        if (!Double.isFinite(from) || from < 0 || from > 1 || tick < -1) {
            throw new IllegalArgumentException("Invalid popup transition");
        }
        from = Math.round(from * 15) / 15.0;
    }

    public PopupAnimation stamp(long now) {
        return tick < 0 ? new PopupAnimation(from, opening, now) : this;
    }

    private long elapsed(long now) {
        long elapsed = Math.floorMod(now - tick, CLOCK_TICKS);
        return elapsed > CLOCK_TICKS / 2 ? 0 : elapsed;
    }

    public double value(long now) {
        if (tick < 0) return from;
        double progress = Math.clamp((double) elapsed(now) / DURATION_TICKS, 0, 1);
        double eased = progress * progress * (3 - 2 * progress);
        return from + ((opening ? 1 : 0) - from) * eased;
    }

    public boolean active(long now) {
        return tick < 0 || elapsed(now) < DURATION_TICKS;
    }

    public int shadow(int position, int alphaBase) {
        if (position < 0 || position > 511 || tick < 0) {
            throw new IllegalArgumentException("Popup encoding requires a position and stamped clock");
        }
        int clock = (int) Math.floorMod(tick, CLOCK_TICKS);
        int mode = (opening ? 0 : 16) | (int) Math.round(from * 15);
        return (alphaBase + mode) << 24 | (position & 255) << 16
                | ((position >> 8) | ((clock & 127) << 1)) << 8 | (clock >> 7);
    }
}
