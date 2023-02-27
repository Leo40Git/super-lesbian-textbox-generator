/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.util;

import org.jetbrains.annotations.Contract;

public final class ArrayUtils {
	private ArrayUtils() {
		throw new UnsupportedOperationException("ArrayUtils only contains static declarations.");
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	@Contract("null -> null; !null -> !null")
	public static String[] clone(String[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_STRING_ARRAY;
		} else {
			return array.clone();
		}
	}

	@Contract("null -> null; !null -> !null")
	public static <T> T[] clone(T[] array) {
		if (array == null) {
			return null;
		} else {
			return array.clone();
		}
	}
}
