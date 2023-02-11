/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

public final class LineBreakElement extends Element {
	public LineBreakElement(int sourcePosition) {
		super(sourcePosition, 1);
	}

	@Override
	public boolean isControlElement() {
		return true;
	}

	@Override
	public String toString() {
		return "LineBreak{" +
				"sourcePosition=" + sourceStart +
				'}';
	}
}
