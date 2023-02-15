/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import io.github.leo40git.sltbg.app.text.parse.ControlElementParser;
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class FontControlElement extends Element {
	public enum Type {
		BOLD,
		ITALIC,
		UNDERLINE,
		STRIKETHROUGH,
		RESET,
	}

	private final @NotNull Type type;

	public FontControlElement(@Range(from = 0, to = Integer.MAX_VALUE) int sourceStart, @Range(from = 1, to = Integer.MAX_VALUE) int sourceLength,
			@NotNull Type type) {
		super(sourceStart, sourceLength);
		this.type = type;
	}

	@Override
	public boolean isControlElement() {
		return true;
	}

	public @NotNull Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Control.Font{" +
				"sourceStart=" + sourceStart +
				", sourceLength=" + sourceLength +
				", type=" + type +
				'}';
	}

	public static final class Parser implements ControlElementParser {
		private final @NotNull Type type;

		public Parser(@NotNull Type type) {
			this.type = type;
		}

		@Override
		public @NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart) {
			return new FontControlElement(sourceStart, 3, type);
		}
	}
}
