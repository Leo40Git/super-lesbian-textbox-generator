/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import static io.leo40git.sltbg.gamedata.Face.IMAGE_SIZE;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.imageio.ImageIO;

import io.leo40git.sltbg.gamedata.Face;
import io.leo40git.sltbg.util.ArrayUtils;
import io.leo40git.sltbg.util.CollectionUtils;
import io.leo40git.sltbg.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("ClassCanBeRecord")
public final class FaceSheet {
	private final @NotNull String sheetPath;
	private final int offset;
	private final @NotNull @Unmodifiable List<Entry> entries;

	public FaceSheet(@NotNull String sheetPath, int offset, @NotNull List<Entry> entries) {
		this.sheetPath = sheetPath;
		this.offset = offset;
		this.entries = CollectionUtils.copyOf(entries);
	}

	public @NotNull String getSheetPath() {
		return sheetPath;
	}

	public int getOffset() {
		return offset;
	}

	public @NotNull @Unmodifiable List<Entry> getEntries() {
		return entries;
	}

	public @NotNull List<Pair<String, Face>> split(@NotNull Path inputDir) throws IOException {
		var inputPath = inputDir.resolve(sheetPath);
		BufferedImage sheet;
		try (var is = Files.newInputStream(inputPath)) {
			sheet = ImageIO.read(is);
		}

		if (sheet.getWidth() % IMAGE_SIZE != 0 || sheet.getHeight() % IMAGE_SIZE != 0) {
			throw new IOException("sheet's dimensions (%d x %d) are not divisible by face image size (%d)"
					.formatted(sheet.getWidth(), sheet.getHeight(), IMAGE_SIZE));
		}

		var faces = new ArrayList<Pair<String, Face>>(entries.size());
		int facesPerRow = sheet.getWidth() / IMAGE_SIZE;
		int index = offset;
		for (var entry : entries) {
			int x = (index % facesPerRow) * IMAGE_SIZE;
			int y = (index / facesPerRow) * IMAGE_SIZE;
			var image = sheet.getSubimage(x, y, IMAGE_SIZE, IMAGE_SIZE);
			var face = new Face(entry.getImagePath(), entry.getName());
			face.setImage(image);
			face.setOrder(entry.getOrder());
			if (entry.getCharacterName() != null) {
				face.setCharacterName(entry.getCharacterName());
			}
			if (!entry.getDescription().isEmpty()) {
				face.setDescription(entry.getDescription().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
			}
			faces.add(new Pair<>(entry.getCategory(), face));

			index += entry.getAdvance();
		}
		return faces;
	}

	public @NotNull CompletableFuture<List<Pair<String, Face>>> splitAsync(@NotNull Executor executor, @NotNull Path inputDir) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return split(inputDir);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, executor);
	}

	@SuppressWarnings("ClassCanBeRecord")
	public static final class Entry {
		private final @NotNull String imagePath, category, name;
		private final long order;
		private final @Nullable String characterName;
		private final @NotNull @Unmodifiable List<String> description;
		private final @Range(from = 1, to = Integer.MAX_VALUE) int advance;

		public Entry(@NotNull String imagePath, @NotNull String category, @NotNull String name, long order, @Nullable String characterName,
				@Nullable List<String> description, @Range(from = 1, to = Integer.MAX_VALUE) int advance) {
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

		public @Range(from = 1, to = Integer.MAX_VALUE) int getAdvance() {
			return advance;
		}
	}
}
