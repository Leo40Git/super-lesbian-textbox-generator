/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import org.jetbrains.annotations.Range;

public final class InvisibleControlElement extends Element {
    public InvisibleControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength) {
        super(sourceStart, sourceLength);
    }

    @Override
    public boolean isControlElement() {
        return true;
    }

    @Override
    public String toString() {
        return "Control.ContinueLine{" +
                "sourcePosition=" + sourceStart +
                '}';
    }
}
