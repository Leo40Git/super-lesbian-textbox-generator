/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

public sealed abstract class Element
		permits ColorControlElement, ContinueLineControlElement, ErrorElement, FormattingControlElement, LineBreakElement, TextElement {
	protected final int sourceStart, sourceLength;

	public Element(int sourceStart, int sourceLength) {
		this.sourceStart = sourceStart;
		this.sourceLength = sourceLength;
	}

	public abstract boolean isControlElement();

	public int getSourceStart() {
		return sourceStart;
	}

	public int getSourceLength() {
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
