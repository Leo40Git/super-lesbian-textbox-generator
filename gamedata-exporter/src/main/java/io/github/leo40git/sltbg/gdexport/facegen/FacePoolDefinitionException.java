/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FacePoolDefinitionException extends IOException {
    public static final int LINE_EOF = 0;

    public static @NotNull String getLineNumberSuffix(int lineNumber) {
        if (lineNumber == LINE_EOF) {
            return "";
        } else {
            return " at line " + lineNumber;
        }
    }

    public static @NotNull FacePoolDefinitionException atLine(@NotNull String message, int lineNumber) {
        if (lineNumber < LINE_EOF) {
            throw new IllegalArgumentException("lineNumber must be >= " + LINE_EOF);
        }

        return new FacePoolDefinitionException(message + getLineNumberSuffix(lineNumber), lineNumber);
    }

    public static @NotNull FacePoolDefinitionException atLine(@NotNull String message, int lineNumber,
                                                              @Nullable String messageCont) {
        if (lineNumber < LINE_EOF) {
            throw new IllegalArgumentException("lineNumber must be >= " + LINE_EOF);
        }

        if (messageCont == null) {
            return new FacePoolDefinitionException(message + getLineNumberSuffix(lineNumber), lineNumber);
        } else {
            return new FacePoolDefinitionException(message + getLineNumberSuffix(lineNumber) + "\n" + messageCont, lineNumber);
        }
    }

    public static @NotNull FacePoolDefinitionException atEOF(@NotNull String message) {
        return atLine(message, LINE_EOF);
    }

    public static @NotNull FacePoolDefinitionException atEOF(@NotNull String message, @Nullable String messageCont) {
        return atLine(message, LINE_EOF, messageCont);
    }

    private final int lineNumber;

    private FacePoolDefinitionException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
