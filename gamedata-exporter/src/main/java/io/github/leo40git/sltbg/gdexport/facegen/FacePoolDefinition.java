/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.leo40git.sltbg.gamedata.FacePool;
import io.leo40git.sltbg.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("ClassCanBeRecord")
public final class FacePoolDefinition {
	private final @NotNull @Unmodifiable Map<String, FaceCategoryDefinition> categories;
	private final @NotNull @Unmodifiable List<FaceSheet> sheets;
	private final @NotNull @Unmodifiable List<String> description, credits;

	public FacePoolDefinition(@NotNull Map<String, FaceCategoryDefinition> categories, @NotNull List<FaceSheet> sheets,
			@Nullable List<String> description, @Nullable List<String> credits) {
		this.categories = CollectionUtils.copyOf(categories);
		this.sheets = CollectionUtils.copyOf(sheets);
		this.description = CollectionUtils.copyOrEmpty(description);
		this.credits = CollectionUtils.copyOrEmpty(credits);
	}

	public @NotNull @Unmodifiable Map<String, FaceCategoryDefinition> getCategories() {
		return categories;
	}

	public @NotNull @Unmodifiable List<FaceSheet> getSheets() {
		return sheets;
	}

	public @NotNull @Unmodifiable List<String> getDescription() {
		return description;
	}

	public @NotNull @Unmodifiable List<String> getCredits() {
		return credits;
	}

	public @NotNull FacePool build(@NotNull String name, @NotNull Path inputDir) throws IOException {
		var pool = new FacePool(name);
		for (var sheet : sheets) {
			var pairs = sheet.split(inputDir);
			for (var pair : pairs) {
				if (!pool.contains(pair.left())) {
					var category = categories.get(pair.left());
					assert category != null : "Undefined category \"" + pair.left() + "\" referenced (should've been caught at parse time!)";
					// TODO
				}
			}
		}
		return pool;
	}
}
