package io.github.leo40git.sltbg.app.text.element;

import org.jetbrains.annotations.NotNull;

public final class EscapedTextElement extends TextElement {
	public EscapedTextElement(int sourceStart, int sourceLength, @NotNull String contents) {
		super(sourceStart, sourceLength, contents);
	}

	@Override
	public boolean isControlElement() {
		return true;
	}

	@Override
	public String toString() {
		return "EscapedText{" +
				"sourceStart=" + sourceStart +
				", sourceLength=" + sourceLength +
				", contents='" + contents + '\'' +
				'}';
	}
}
