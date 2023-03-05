/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.assext.face;

import java.io.IOException;

public class FaceListException extends IOException {
    private final int lineNum;

    public FaceListException(int lineNum) {
        this.lineNum = lineNum;
    }

    public FaceListException(String message, int lineNum) {
        super(message);
        this.lineNum = lineNum;
    }

    public FaceListException(String message, Throwable cause, int lineNum) {
        super(message, cause);
        this.lineNum = lineNum;
    }

    public FaceListException(Throwable cause, int lineNum) {
        super(cause);
        this.lineNum = lineNum;
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + " at line " + lineNum;
    }
}
