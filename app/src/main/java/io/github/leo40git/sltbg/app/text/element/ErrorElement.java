package io.github.leo40git.sltbg.app.text.element;

import org.jetbrains.annotations.NotNull;

public final class ErrorElement extends Element {
	private final boolean isControlElement;
	private final @NotNull String message;

	public ErrorElement(int sourceStart, int sourceLength, boolean isControlElement, @NotNull String message) {
		super(sourceStart, sourceLength);
		this.isControlElement = isControlElement;
		this.message = message;
	}

	@Override
	public boolean isControlElement() {
		return isControlElement;
	}

	public @NotNull String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "Error{" +
				"sourceStart=" + sourceStart +
				", sourceLength=" + sourceLength +
				", isControlElement=" + isControlElement +
				", message='" + message + '\'' +
				'}';
	}
}
