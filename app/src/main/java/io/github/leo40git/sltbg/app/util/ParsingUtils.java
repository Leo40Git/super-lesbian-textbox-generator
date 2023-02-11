package io.github.leo40git.sltbg.app.util;

import org.jetbrains.annotations.NotNull;

public final class ParsingUtils {
	private ParsingUtils() {
		throw new UnsupportedOperationException("ParsingUtils only contains static declarations.");
	}

	public static int parseDecInt(@NotNull String s) {
		s = s.trim();
		if (s.charAt(0) == '+') {
			throw new NumberFormatException("Illegal leading plus sign on decimal string %s.".formatted(s));
		}
		return Integer.parseUnsignedInt(s, 10);
	}

	public static int parseHexInt(@NotNull String s) {
		s = s.trim();
		if (s.charAt(0) == '+') {
			throw new NumberFormatException("Illegal leading plus sign on hexadecimal string %s.".formatted(s));
		}
		return Integer.parseUnsignedInt(s, 16);
	}
}
