/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FaceCategoryMerger {
	private @Nullable FaceCategory workingCategory;

	public FaceCategoryMerger(@NotNull FaceCategory baseCategory) {
		workingCategory = baseCategory.copy();
	}

	public void add(@NotNull FaceCategory category) {
		if (workingCategory == null) {
			throw new IllegalStateException("finish() was already called");
		}

		for (var face : category.faces.values()) {
			workingCategory.add(face.copy());
		}
	}

	public @NotNull FaceCategory finish() {
		if (workingCategory == null) {
			throw new IllegalStateException("finish() was already called");
		}

		var c = workingCategory;
		workingCategory = null;
		return c;
	}
}
