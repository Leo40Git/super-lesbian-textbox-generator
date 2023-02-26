/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facelist;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

public final class FaceList {
	private final @NotNull HashMap<String, PendingFaceCategory> pendingCategories;
	private final @NotNull ArrayList<FaceSheetSplitter> sheetSplitters;

	public FaceList() {
		this.pendingCategories = new HashMap<>();
		this.sheetSplitters = new ArrayList<>();
	}

	public void addPendingCategory(@NotNull PendingFaceCategory category) {
		if (pendingCategories.put(category.getName(), category) != null) {
			throw new IllegalArgumentException("Category with \"" + category.getName() + "\" was already declared");
		}
	}

	public void addSheetSplitter(@NotNull FaceSheetSplitter splitter) {
		sheetSplitters.add(splitter);
	}

	// TODO

	public @NotNull CompletableFuture<Void> writePool(@NotNull Executor executor, @NotNull Path dirGameData, @NotNull Path dirOutput) {
		// TODO
		return CompletableFuture.completedFuture(null);
	}
}
