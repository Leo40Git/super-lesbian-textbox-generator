/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.json;

import java.io.IOException;

public class MalformedJsonException extends IOException {
    public MalformedJsonException(String message) {
        super(message);
    }

    public MalformedJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
