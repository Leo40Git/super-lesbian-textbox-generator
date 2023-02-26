/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facelist;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import io.leo40git.sltbg.gamedata.Face;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FaceSheetSplitter {
	private final @NotNull String sheetPath;
	private final @NotNull ArrayList<Entry> entries;

	public FaceSheetSplitter(@NotNull String sheetPath) {
		this.sheetPath = sheetPath;
		entries = new ArrayList<>();
	}

	public void addEntry(@NotNull Entry entry) {
		entries.add(entry);
	}

	public void execute(@NotNull Path dirGameData, @NotNull Consumer<PendingFace> faceAccumulator) throws IOException {
		var sheetPath = dirGameData.resolve(this.sheetPath);

		BufferedImage sheet;
		try (var is = Files.newInputStream(sheetPath)) {
			sheet = ImageIO.read(is);
		}
		if (sheet.getWidth() % Face.IMAGE_SIZE != 0 || sheet.getHeight() % Face.IMAGE_SIZE != 0) {
			throw new IOException("sheet's dimensions (%d x %d) are not divisible by face image size (%d)"
					.formatted(sheet.getWidth(), sheet.getHeight(), Face.IMAGE_SIZE));
		}

		final int facesPerRow = sheet.getWidth() / Face.IMAGE_SIZE;
		int index = 0;

		for (var entry : entries) {
			if (entry instanceof AddEntry add) {
				int x = (index % facesPerRow) * Face.IMAGE_SIZE;
				int y = (index / facesPerRow) * Face.IMAGE_SIZE;
				faceAccumulator.accept(new PendingFace(add.getImagePath(), add.getCategory(), add.getName(), add.getOrder(), add.getCharacterName(),
						sheet.getSubimage(x, y, Face.IMAGE_SIZE, Face.IMAGE_SIZE)));
				index++;
			} else if (entry instanceof SkipEntry skip) {
				index += skip.getAdvance();
			} else {
				assert false : "unexpected entry type " + entry.getClass();
			}
		}
	}

	public sealed static class Entry {}

	public static final class AddEntry extends Entry {
		private final @NotNull String imagePath, category, name;
		private final long order;
		private final @Nullable String characterName;

		public AddEntry(@NotNull String imagePath, @NotNull String category, @NotNull String name, long order, @Nullable String characterName) {
			this.imagePath = imagePath;
			this.category = category;
			this.name = name;
			this.characterName = characterName;
			this.order = order;
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
	}

	public static final class SkipEntry extends Entry {
		private final int advance;

		public SkipEntry(int advance) {
			this.advance = advance;
		}

		public int getAdvance() {
			return advance;
		}
	}
}
