/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.parse;

import java.util.HashMap;
import java.util.IdentityHashMap;

import io.github.leo40git.sltbg.app.text.element.ColorControlElement;
import io.github.leo40git.sltbg.app.text.element.Element;
import io.github.leo40git.sltbg.app.text.element.FormattingControlElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ControlElementRegistry {
	private ControlElementRegistry() {
		throw new UnsupportedOperationException("ControlElementRegistry only contains static declarations.");
	}

	private static final HashMap<Integer, IdentityHashMap<char[], ControlElementParser>> BACKING_MAP;
	private static final int MAXIMUM_NAME_LENGTH;
	private static final ThreadLocal<char[]> TL_BUFFER;

	static {
		BACKING_MAP = new HashMap<>();
		int maximumNameLength = 0;

		maximumNameLength = register("C", new ColorControlElement.Parser(), maximumNameLength);
		// TODO size up/down
		maximumNameLength = register("B",  new FormattingControlElement.Parser(FormattingControlElement.Type.BOLD, 2), maximumNameLength);
		maximumNameLength = register("I",  new FormattingControlElement.Parser(FormattingControlElement.Type.ITALIC, 2), maximumNameLength);
		maximumNameLength = register("U",  new FormattingControlElement.Parser(FormattingControlElement.Type.UNDERLINE, 2), maximumNameLength);
		maximumNameLength = register("S",  new FormattingControlElement.Parser(FormattingControlElement.Type.STRIKETHROUGH, 2), maximumNameLength);
		maximumNameLength = register("RF", new FormattingControlElement.Parser(FormattingControlElement.Type.RESET, 3), maximumNameLength);
		// TODO style? (subscript, superscript)
		// TODO animated??? (ani, rainbow)

		MAXIMUM_NAME_LENGTH = maximumNameLength;
		TL_BUFFER = ThreadLocal.withInitial(() -> new char[MAXIMUM_NAME_LENGTH]);
	}

	private static int register(@NotNull String name, @NotNull ControlElementParser parser, int maximumNameLength) {
		char[] chars = name.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			chars[i] = Character.toUpperCase(chars[i]);
		}
		BACKING_MAP.computeIfAbsent(chars.length, ignored -> new IdentityHashMap<>()).put(chars, parser);
		return Math.max(chars.length, maximumNameLength);
	}

	public static void init() { /* clinit */ }

	public static @Nullable Element parse(@NotNull TextScanner scn, int sourceStart) {
		char[] buf = TL_BUFFER.get();
		int maxNameLength = scn.peek(buf, 0, MAXIMUM_NAME_LENGTH);
		if (maxNameLength == 0) {
			return null;
		}

		for (int i = 0; i < maxNameLength; i++) {
			buf[i] = Character.toUpperCase(buf[i]);
		}

		for (int nameLength = maxNameLength; nameLength > 0; nameLength--) {
			for (var entry : BACKING_MAP.get(nameLength).entrySet()) {
				char[] nameChars = entry.getKey();
				boolean match = true;
				for (int i = 0; i < nameLength; i++) {
					if (nameChars[i] != buf[i]) {
						match = false;
						break;
					}
				}

				if (match) {
					scn.skip(nameLength);
					return entry.getValue().parse(scn, sourceStart);
				}
			}
		}

		return null;
	}
}
