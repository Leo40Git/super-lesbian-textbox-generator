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
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class SizeControlElement extends Element {
    private final int delta;

    public SizeControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
                              int delta) {
        super(sourceStart, sourceLength);
        this.delta = delta;
    }

    @Override
    public boolean isControlElement() {
        return true;
    }

    public int getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        return "Control.Size{" +
                "sourceStart=" + sourceStart +
                ", sourceLength=" + sourceLength +
                ", delta=" + delta +
                '}';
    }

    public static final class Parser implements ControlElementParser {
        private final char ch;
        private final int sign;

        public Parser(char ch, int sign) {
            this.ch = ch;
            this.sign = sign;
        }

        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            int count = 1;
            while (scn.peek() == ch) {
                count++;
                // TODO limit!
                scn.skip();
            }
            return new SizeControlElement(sourceStart, 1 + count, sign * count);
        }
    }

    public static final class ResetParser implements ControlElementParser {
        @Override
        public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
            return new SizeControlElement(sourceStart, 2, 0);
        }
    }
}
