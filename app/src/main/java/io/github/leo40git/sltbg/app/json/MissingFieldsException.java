package io.github.leo40git.sltbg.app.json;

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
