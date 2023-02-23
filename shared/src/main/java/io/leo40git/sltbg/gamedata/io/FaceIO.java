/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import io.leo40git.sltbg.gamedata.Face;
import io.leo40git.sltbg.json.MissingFieldsException;
import io.leo40git.sltbg.util.ImageUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.JsonWriter;

public final class FaceIO {
	private FaceIO() {
		throw new UnsupportedOperationException("FaceIO only contains static declarations.");
	}

	@Contract("_, _ -> new")
	public static @NotNull Face read(@NotNull JsonReader reader, @NotNull String name) throws IOException {
		String imagePath = null;
		boolean orderSet = false;
		long order = 0;
		String charName = null;

		if (reader.peek() == JsonToken.STRING) {
			imagePath = reader.nextString();
		} else {
			reader.beginObject();
			while (reader.hasNext()) {
				String field = reader.nextName();
				switch (field) {
					case FaceFields.IMAGE_PATH -> imagePath = reader.nextString();
					case FaceFields.ORDER -> {
						order = reader.nextLong();
						orderSet = true;
					}
					case FaceFields.CHARACTER_NAME -> charName = reader.nextName();
					default -> reader.skipValue();
				}
			}
			reader.endObject();

			if (imagePath == null) {
				throw new MissingFieldsException(reader, "Face", FaceFields.IMAGE_PATH);
			}
		}

		var face = new Face(name, imagePath);
		if (orderSet) {
			face.setOrder(order);
		}
		if (charName != null) {
			face.setCharacterName(charName);
		}
		return face;
	}

	public static void readImage(@NotNull Face face, @NotNull Path rootDir) throws FaceIOException {
		face.clearImage();

		final var imagePath = rootDir.resolve(face.getImagePath());
		BufferedImage image;
		try (var is = Files.newInputStream(imagePath)) {
			image = ImageIO.read(is);
		} catch (IOException e) {
			throw new FaceIOException(face, "Failed to read face image from \"" + imagePath + "\"", e);
		}

		face.setImage(image);
	}

	public static void write(@NotNull Face face, @NotNull JsonWriter writer) throws IOException {
		writer.name(face.getName());
		if (!face.isOrderSet() && !face.isCharacterNameSet()) {
			writer.value(face.getImagePath());
		} else {
			writer.beginObject();
			writer.name(FaceFields.IMAGE_PATH);
			writer.value(face.getImagePath());
			if (face.isOrderSet()) {
				writer.name(FaceFields.ORDER);
				writer.value(face.getOrder());
			}
			if (face.isCharacterNameSet()) {
				writer.name(FaceFields.CHARACTER_NAME);
				writer.value(face.getCharacterName());
			}
			writer.endObject();
		}
	}

	public static void writeImage(@NotNull Face face, @NotNull Path rootDir) throws FaceIOException {
		if (!face.hasImage()) {
			throw new FaceIOException(face, "Face doesn't have an image to write");
		}

		final var imagePath = rootDir.resolve(face.getImagePath());
		try {
			ImageUtils.writeImage(face.getImage(), imagePath);
		} catch (IOException e) {
			throw new FaceIOException(face, "Failed to write face image to \"" + imagePath + "\"", e);
		}
	}
}
