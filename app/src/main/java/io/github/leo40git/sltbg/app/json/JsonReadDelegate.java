package io.github.leo40git.sltbg.app.json;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;

@FunctionalInterface
public interface JsonReadDelegate<T> {
	@Nullable T read(@NotNull JsonReader reader) throws IOException;
}
