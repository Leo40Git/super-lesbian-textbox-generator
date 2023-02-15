/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class ErrorElement extends Element {
	private final boolean isControlElement;
	private final @NotNull String message;

	public ErrorElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
			boolean isControlElement, @NotNull String message) {
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
