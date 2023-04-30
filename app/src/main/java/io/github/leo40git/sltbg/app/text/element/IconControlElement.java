/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import io.github.leo40git.sltbg.app.text.parse.ControlElementParser;
import io.github.leo40git.sltbg.app.text.parse.ParsingUtils;
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class IconControlElement extends Element {
    private final @NotNull String name;

    public IconControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
                              @NotNull String name) {
        super(sourceStart, sourceLength);
        this.name = name;
    }

    @Override
    public boolean isControlElement() {
        return true;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Control.Icon{" +
                "sourceStart=" + sourceStart +
                ", sourceLength=" + sourceLength +
                ", name='" + name + '\'' +
                '}';
    }

    public static final class Parser implements ControlElementParser {
        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            String name = ParsingUtils.getArgument(scn);
            if (name == null) {
                return new ErrorElement(sourceStart, 2, true,
                        "\\I: missing required argument");
            }

            return new IconControlElement(sourceStart, 2 + 2 + name.length(), name);
        }
    }
}
