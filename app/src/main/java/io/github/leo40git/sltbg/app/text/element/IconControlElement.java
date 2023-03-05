/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.parse.ControlElementParser;
import io.github.leo40git.sltbg.app.text.parse.ParsingUtils;
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class IconControlElement extends Element {
    private final @Range(from = 0, to = Integer.MAX_VALUE) int index;

    public IconControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
                              @Range(from = 0, to = Integer.MAX_VALUE) int index) {
        super(sourceStart, sourceLength);
        this.index = index;
    }

    @Override
    public boolean isControlElement() {
        return true;
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "IconControlElement{" +
                "sourceStart=" + sourceStart +
                ", sourceLength=" + sourceLength +
                ", index=" + index +
                '}';
    }

    public static final class Parser implements ControlElementParser {
        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            String arg = ParsingUtils.getArgument(scn);
            if (arg == null) {
                return new ErrorElement(sourceStart, 2, true,
                        "\\I: missing required argument");
            }

            int index;
            try {
                index = ParsingUtils.parseDecInt(arg);
            } catch (NumberFormatException e) {
                return new ErrorElement(sourceStart, 2 + 2 + arg.length(), true,
                        "\\I: invalid argument: %s".formatted(e.getLocalizedMessage()));
            }

            if (index > GameAssets.getMaximumIconIndex()) {
                return new ErrorElement(sourceStart, 2 + 2 + arg.length(), true,
                        "\\I: index %d too high, must be lower than %d".formatted(index, GameAssets.getMaximumIconIndex()));
            }

            return new IconControlElement(sourceStart, 2 + 2 + arg.length(), index);
        }
    }
}
