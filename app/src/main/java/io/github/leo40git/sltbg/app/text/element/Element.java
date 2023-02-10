package io.github.leo40git.sltbg.app.text.element;

public sealed abstract class Element permits ColorControlElement, ContinueLineControlElement, ErrorElement, LineBreakElement, TextElement {
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
