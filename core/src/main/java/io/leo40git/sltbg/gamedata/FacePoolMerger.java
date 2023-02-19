/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FacePoolMerger {
	private @Nullable HashMap<String, FaceCategoryMerger> categories;

	public FacePoolMerger() {
		categories = new HashMap<>();
	}

	public void add(@NotNull FacePool pool) {
		if (categories == null) {
			throw new IllegalStateException("finish() was already called");
		}

		for (var category : pool.categories.values()) {
			var merger = categories.get(category.getName());
			if (merger == null) {
				categories.put(category.getName(), new FaceCategoryMerger(category));
			} else {
				merger.add(category);
			}
		}
	}

	public @NotNull FacePool finish() {
		if (categories == null) {
			throw new IllegalStateException("finish() was already called");
		}

		var pool = new FacePool();
		for (var category : categories.values()) {
			pool.add(category.finish());
		}

		categories.clear();
		categories = null;

		return pool;
	}
}
