/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.window;

import java.util.Objects;

import org.jetbrains.annotations.Range;

/**
 * Represents a tone. Each channel of said tone can be additive or subtractive.
 * <p>
 * There is also an <em>intensity</em> channel that the 3 color channels are scaled by.
 */
public final class WindowTone {
    private final @Range(from = -255, to = 255) int red, green, blue;
    private final @Range(from = 0, to = 255) int intensity;

    private final @Range(from = -255, to = 255) int redScaled, greenScaled, blueScaled;

    public WindowTone(
            @Range(from = -255, to = 255) int red,
            @Range(from = -255, to = 255) int green,
            @Range(from = -255, to = 255) int blue,
            @Range(from = 0, to = 255) int intensity) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.intensity = intensity;

        double intensityScale = intensity / 255.0;
        redScaled = (int) Math.floor(red * intensityScale);
        greenScaled = (int) Math.floor(green * intensityScale);
        blueScaled = (int) Math.floor(blue * intensityScale);
    }

    public @Range(from = -255, to = 255) int red() {
        return red;
    }

    public @Range(from = -255, to = 255) int green() {
        return green;
    }

    public @Range(from = -255, to = 255) int blue() {
        return blue;
    }

    public @Range(from = 0, to = 255) int intensity() {
        return intensity;
    }

    public @Range(from = -255, to = 255) int redScaled() {
        return redScaled;
    }

    public @Range(from = -255, to = 255) int greenScaled() {
        return greenScaled;
    }

    public @Range(from = -255, to = 255) int blueScaled() {
        return blueScaled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WindowTone) obj;
        return this.red == that.red &&
                this.green == that.green &&
                this.blue == that.blue &&
                this.intensity == that.intensity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, intensity);
    }

    @Override
    public String toString() {
        return "WindowTone[" +
                "red=" + red + ", " +
                "green=" + green + ", " +
                "blue=" + blue + ", " +
                "intensity=" + intensity + ']';
    }

}
