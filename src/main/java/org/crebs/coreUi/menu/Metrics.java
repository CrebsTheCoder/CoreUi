package org.crebs.coreUi.menu;

public final class Metrics {
    public static final int ROW_HEIGHT = 33;
    public static final int CONTROL_LEFT = 157;
    public static final int CONTROL_MARGIN = 10;
    public static final int THUMB_WIDTH = 8;
    public static final int TOGGLE_WIDTH = 32;
    public static final int TOGGLE_KNOB = 12;
    public static final int TOGGLE_TRAVEL = TOGGLE_WIDTH - 2 - TOGGLE_KNOB;

    public static int trackWidth(int rowWidth) {
        return rowWidth - CONTROL_LEFT - 1 - CONTROL_MARGIN;
    }

    public static int travel(int rowWidth) {
        return trackWidth(rowWidth) - THUMB_WIDTH;
    }

    public static int controlWidth(int rowWidth) {
        return rowWidth - CONTROL_LEFT - 1 - CONTROL_MARGIN;
    }

    private Metrics() {
    }
}
