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

public sealed abstract class Element
		permits ColorControlElement, InvisibleControlElement, ErrorElement, FormattingControlElement, LineBreakElement, TextElement {
	protected final @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, sourceLength;

	public Element(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength) {
		this.sourceStart = sourceStart;
		this.sourceLength = sourceLength;
	}

	public abstract boolean isControlElement();

	public @Range(from = 0, to = Integer.MAX_VALUE) int getSourceStart() {
		return sourceStart;
	}

	public @Range(from = 1, to = Integer.MAX_VALUE) int getSourceLength() {
		return sourceLength;
	}

	public int getSourceEnd() {
		return sourceStart + sourceLength;
	}

	@Override
	public String toString() {
		return "Element{" +
				"sourceStart=" + sourceStart +
				", sourceLength=" + sourceLength +
				'}';
	}
}
