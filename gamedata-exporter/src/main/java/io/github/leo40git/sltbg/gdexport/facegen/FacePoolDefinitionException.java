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

public final class FacePoolDefinitionException extends IOException {
    private final int lineNumber;

    public FacePoolDefinitionException(String message, int lineNumber) {
        super(message + " at line " + lineNumber);
        this.lineNumber = lineNumber;
    }

    public FacePoolDefinitionException(String message, String messageCont, int lineNumber) {
        super(message + " at line " + lineNumber + (messageCont != null ? "\n" + messageCont : ""));
        this.lineNumber = lineNumber;
    }

    public FacePoolDefinitionException(String message, int lineNumber, Throwable cause) {
        super(message + " at line " + lineNumber, cause);
        this.lineNumber = lineNumber;
    }

    public FacePoolDefinitionException(String message, String messageCont, int lineNumber, Throwable cause) {
        super(message + " at line " + lineNumber + (messageCont != null ? "\n" + messageCont : ""), cause);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
