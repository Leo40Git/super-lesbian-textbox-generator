package io.github.leo40git.sltbg.app.json;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonWriter;

@FunctionalInterface
public interface JsonWriteDelegate<T> {
	void write(@NotNull JsonWriter writer, @Nullable T value) throws IOException;
}
