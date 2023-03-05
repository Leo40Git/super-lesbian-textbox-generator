/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import java.awt.Color;

import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.parse.ControlElementParser;
import io.github.leo40git.sltbg.app.text.parse.ParsingUtils;
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class ColorControlElement extends Element {
    private final @NotNull Color color;

    public ColorControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
                               @NotNull Color color) {
        super(sourceStart, sourceLength);
        this.color = color;
    }

    @Override
    public boolean isControlElement() {
        return true;
    }

    public @NotNull Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Control.Color{" +
                "sourceStart=" + sourceStart +
                ", sourceLength=" + sourceLength +
                ", color=#%02X%02X%02X".formatted(color.getRed(), color.getGreen(), color.getBlue()) +
                '}';
    }

    public static final class Parser implements ControlElementParser {
        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            String arg = ParsingUtils.getArgument(scn);
            if (arg == null) {
                return new ColorControlElement(sourceStart, 2, GameAssets.getPaletteColor(0));
            }

            Color color;
            try {
                color = parseColorArgument(arg);
            } catch (IllegalArgumentException e) {
                return new ErrorElement(sourceStart, 2 + 2 + arg.length(), true,
                        "\\C: invalid argument: %s".formatted(e.getLocalizedMessage()));
            }

            return new ColorControlElement(sourceStart, 2 + 2 + arg.length(), color);
        }

        private static @NotNull Color parseColorArgument(@NotNull String arg) throws IllegalArgumentException {
            if (arg.startsWith("#")) {
                int hexLen = arg.length();
                if (hexLen != 4 && hexLen != 7) {
                    throw new IllegalArgumentException("Invalid hex color format, should be 3 or 6 chars long (but was %d)".formatted(hexLen));
                }

                if (hexLen == 4) {
                    // expand CSS-style to standard
                    char cr = arg.charAt(1), cg = arg.charAt(2), cb = arg.charAt(3);
                    arg = "#" + cr + cr + cg + cg + cb + cb;
                }

                try {
                    return Color.decode(arg);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Failed to parse hex color", e);
                }
            } else {
                int palIdx;
                try {
                    palIdx = ParsingUtils.parseDecInt(arg);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Failed to parse palette index", e);
                }
                if (palIdx >= GameAssets.PALETTE_SIZE) {
                    throw new IllegalArgumentException("Palette index is out of bounds (must be below %d, but was %d)"
                            .formatted(palIdx, GameAssets.PALETTE_SIZE));
                }
                return GameAssets.getPaletteColor(palIdx);
            }
        }
    }
}
