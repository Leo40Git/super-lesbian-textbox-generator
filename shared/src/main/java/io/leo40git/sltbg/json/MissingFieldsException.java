/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.json;

import org.jetbrains.annotations.NotNull;

public class MissingFieldsException extends MalformedJsonException {
    public MissingFieldsException(@NotNull String messageStart, @NotNull Iterable<String> missingFields) {
        super(messageStart + " is missing the following fields: " + String.join(", ", missingFields));
    }

    public MissingFieldsException(@NotNull String messageStart, String... missingFields) {
        super(messageStart + " is missing the following fields: " + String.join(", ", missingFields));
    }
}
