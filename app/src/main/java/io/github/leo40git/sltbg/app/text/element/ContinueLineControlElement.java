package io.github.leo40git.sltbg.app.text.element;

public final class ContinueLineControlElement extends Element {
	public ContinueLineControlElement(int sourcePosition) {
		super(sourcePosition, 2);
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
