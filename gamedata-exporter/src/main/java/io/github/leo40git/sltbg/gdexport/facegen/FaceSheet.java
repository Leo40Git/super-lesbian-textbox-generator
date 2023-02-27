/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.util.List;

import io.leo40git.sltbg.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("ClassCanBeRecord")
public final class FaceSheet {
	private final @NotNull String inputPath;
	private final int offset;
	private final @NotNull @Unmodifiable List<Entry> entries;

	public FaceSheet(@NotNull String inputPath, int offset, @NotNull List<Entry> entries) {
		this.inputPath = inputPath;
		this.offset = offset;
		this.entries = CollectionUtils.copyOf(entries);
	}

	public @NotNull String getInputPath() {
		return inputPath;
	}

	public int getOffset() {
		return offset;
	}

	public @NotNull @Unmodifiable List<Entry> getEntries() {
		return entries;
	}

	// TODO

	@SuppressWarnings("ClassCanBeRecord")
	public static final class Entry {
		private final @NotNull String imagePath, category, name;
		private final long order;
		private final @Nullable String characterName;
		private final @NotNull @Unmodifiable List<String> description;
		private final int advance;

		public Entry(@NotNull String imagePath, @NotNull String category, @NotNull String name, long order, @Nullable String characterName,
				@Nullable List<String> description, int advance) {
			this.imagePath = imagePath;
			this.category = category;
			this.name = name;
			this.order = order;
			this.characterName = characterName;
			this.description = CollectionUtils.copyOrEmpty(description);
			this.advance = advance;
		}

		public @NotNull String getImagePath() {
			return imagePath;
		}

		public @NotNull String getCategory() {
			return category;
		}

		public @NotNull String getName() {
			return name;
		}

		public long getOrder() {
			return order;
		}

		public @Nullable String getCharacterName() {
			return characterName;
		}

		public @NotNull @Unmodifiable List<String> getDescription() {
			return description;
		}

		public int getAdvance() {
			return advance;
		}
	}
}
