package io.github.leo40git.sltbg.assext.face;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public sealed interface FaceListEntry {
	default int indexAdvance() {
		return 1;
	}

	record Add(@NotNull String category, @NotNull String name, @NotNull String path, @NotNull String @NotNull [] tags) implements FaceListEntry { }

	record Skip(@Range(from = 1, to = Integer.MAX_VALUE) int indexAdvance) implements FaceListEntry { }
}
