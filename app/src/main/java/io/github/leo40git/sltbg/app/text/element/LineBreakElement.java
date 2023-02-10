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
