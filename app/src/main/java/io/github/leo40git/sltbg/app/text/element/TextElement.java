package io.github.leo40git.sltbg.app.text.element;

import org.jetbrains.annotations.NotNull;

public sealed class TextElement extends Element permits EscapedTextElement {
	protected final @NotNull String contents;

	public TextElement(int sourceStart, int sourceLength, @NotNull String contents) {
		super(sourceStart, sourceLength);
		this.contents = contents;
	}

	@Override
	public boolean isControlElement() {
		return false;
	}

	public @NotNull String getContents() {
		return contents;
	}

	@Override
	public String toString() {
		return "Text{" +
				"sourceStart=" + sourceStart +
				", sourceLength=" + sourceLength +
				", contents='" + contents + '\'' +
				'}';
	}
}
