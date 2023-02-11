/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.assext.face;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public sealed interface FaceListEntry {
	default int indexAdvance() {
		return 1;
	}

	record Add(@NotNull String category, @NotNull String name, @NotNull String path, int order, @NotNull String @NotNull [] tags) implements FaceListEntry { }

	record Skip(@Range(from = 1, to = Integer.MAX_VALUE) int indexAdvance) implements FaceListEntry { }
}
