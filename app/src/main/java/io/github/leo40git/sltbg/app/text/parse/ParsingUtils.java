/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class ParsingUtils {
    private ParsingUtils() {
        throw new UnsupportedOperationException("ParsingUtils only contains static declarations.");
    }

    public static @Range(from = 0, to = Integer.MAX_VALUE) int parseDecInt(@NotNull String s) {
        s = s.trim();

        if (s.isEmpty()) {
            throw new NumberFormatException("Cannot parse empty string as number.");
        }

        if (s.charAt(0) == '+') {
            throw new NumberFormatException("Illegal leading plus sign on decimal string %s.".formatted(s));
        }

        return Integer.parseUnsignedInt(s, 10);
    }

    public static @Range(from = 0, to = Integer.MAX_VALUE) int parseHexInt(@NotNull String s) {
        s = s.trim();

        if (s.isEmpty()) {
            throw new NumberFormatException("Cannot parse empty string as number.");
        }

        if (s.charAt(0) == '+') {
            throw new NumberFormatException("Illegal leading plus sign on hexadecimal string %s.".formatted(s));
        }

        return Integer.parseUnsignedInt(s, 16);
    }

    public static @Nullable String getArgument(@NotNull TextScanner scn) {
        if (scn.peek() != '[') {
            return null;
        } else {
            scn.skip();
            return scn.until(']');
        }
    }
}
