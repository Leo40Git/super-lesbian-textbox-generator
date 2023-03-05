/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

public final class StringUtils {
    private StringUtils() {
        throw new UnsupportedOperationException("StringUtils only contains static declarations.");
    }

    private static final AtomicBoolean lineSeparatorPatternInitialized = new AtomicBoolean();
    private static Pattern lineSeparatorPattern;

    public static @NotNull String simplifyLineSeparators(@NotNull String s) {
        if (!lineSeparatorPatternInitialized.compareAndExchange(false, true)) {
            String lineSeparator = System.lineSeparator();
            if (!"\n".equals(lineSeparator)) {
                lineSeparatorPattern = Pattern.compile(lineSeparator, Pattern.LITERAL);
            }
        }

        if (lineSeparatorPattern == null) {
            return s;
        } else {
            return lineSeparatorPattern.matcher(s).replaceAll("\n");
        }
    }
}
