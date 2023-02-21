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

import org.quiltmc.json5.JsonReader;

public class MissingFieldsException extends MalformedJsonException {
	public MissingFieldsException(String name, @NotNull Iterable<String> missingFields) {
		super("%s is missing the following fields: %s".formatted(name, String.join(", ", missingFields)));
	}

	public MissingFieldsException(String name, String @NotNull ... missingFields) {
		super("%s is missing the following fields: %s".formatted(name, String.join(", ", missingFields)));
	}

	public MissingFieldsException(@NotNull JsonReader reader, String name, @NotNull Iterable<String> missingFields) {
		super("%s%s is missing the following fields: %s".formatted(name, reader.locationString(),
				String.join(", ", missingFields)));
	}

	public MissingFieldsException(@NotNull JsonReader reader, String name, String @NotNull ... missingFields) {
		super("%s%s is missing the following fields: %s".formatted(name, reader.locationString(),
				String.join(", ", missingFields)));
	}
}
