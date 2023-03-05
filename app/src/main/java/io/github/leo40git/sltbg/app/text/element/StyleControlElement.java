/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import io.github.leo40git.sltbg.app.text.TextStyle;
import io.github.leo40git.sltbg.app.text.parse.ControlElementParser;
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public abstract sealed class StyleControlElement extends Element {
    public StyleControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart,
                               @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength) {
        super(sourceStart, sourceLength);
    }

    @Override
    public boolean isControlElement() {
        return true;
    }

    public static final class Toggle extends StyleControlElement {
        private final @NotNull TextStyle target;

        public Toggle(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
                      @NotNull TextStyle target) {
            super(sourceStart, sourceLength);
            this.target = target;
        }

        public @NotNull TextStyle getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return "Control.Style.Toggle{" +
                    "sourceStart=" + sourceStart +
                    ", sourceLength=" + sourceLength +
                    ", target=" + target +
                    '}';
        }
    }

    public static final class ToggleParser implements ControlElementParser {
        private final @Range(from = 1, to = Integer.MAX_VALUE) int length;
        private final @NotNull TextStyle target;

        public ToggleParser(@Range(from = 1, to = Integer.MAX_VALUE) int length, @NotNull TextStyle target) {
            this.length = length;
            this.target = target;
        }

        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            return new Toggle(sourceStart, 1 + length, target);
        }
    }

    public static final class Reset extends StyleControlElement {
        public Reset(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength) {
            super(sourceStart, sourceLength);
        }

        @Override
        public String toString() {
            return "Control.Style.Reset{" +
                    "sourceStart=" + sourceStart +
                    ", sourceLength=" + sourceLength +
                    '}';
        }
    }

    public static final class ResetParser implements ControlElementParser {
        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            return new Reset(sourceStart, 2);
        }
    }
}
