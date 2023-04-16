/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.window;

/**
 * Represents the version of RPG Maker the window skin file was created for.
 */
public enum WindowVersion {
    VX_VXA(1, 2, 12),
    MV(1.5, 4, 18),
    MZ(1.5, 4, 12);

    private final double scale;
    private final int margin, padding;

    WindowVersion(double scale, int margin, int padding) {
        this.scale = scale;
        this.margin = margin;
        this.padding = padding;
    }

    public double getScale() {
        return scale;
    }

    public int getMargin() {
        return margin;
    }

    public int getPadding() {
        return padding;
    }

    public int scale(int v) {
        if (scale == 1) {
            return v;
        } else {
            return (int) Math.floor(v * scale);
        }
    }
}
