/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import java.io.IOException;
import java.nio.file.Path;

import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FaceCategoryIO {
	private FaceCategoryIO() {
		throw new UnsupportedOperationException("FaceCategoryIO only contains static declarations.");
	}

	@Contract("_, _ -> new")
	public static @NotNull FaceCategory read(@NotNull JsonReader reader, @NotNull String name) throws IOException {
		var category = new FaceCategory(name);

		boolean gotFaces = false;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case FaceFields.FACES -> {
					JsonReadUtils.readSimpleMap(reader, FaceIO::read, category::add);
					gotFaces = true;
				}
				case FaceFields.ORDER -> category.setOrder(reader.nextLong());
				case FaceFields.CHARACTER_NAME -> category.setCharacterName(reader.nextString());
				default -> reader.skipValue();
			}
		}
		reader.endObject();

		if (!gotFaces) {
			throw new MissingFieldsException("Category", FaceFields.FACES);
		}

		return category;
	}

	public static void write(@NotNull JsonWriter writer, @NotNull FaceCategory category) throws IOException {
		category.sortIfNeeded();

		writer.name(category.getName());

		writer.beginObject();

		if (category.isOrderSet()) {
			writer.name(FaceFields.ORDER);
			writer.value(category.getOrder());
		}

		if (category.getCharacterName() != null) {
			writer.name(FaceFields.CHARACTER_NAME);
			writer.value(category.getCharacterName());
		}

		writer.name(FaceFields.FACES);
		JsonWriteUtils.writeObject(writer, FaceIO::write, category.getFaces().values());

		writer.endObject();
	}

	public static void writeImages(@NotNull FaceCategory category, @NotNull Path rootDir) throws IOException {
		category.sortIfNeeded();
		for (var face : category.getFaces().values()) {
			FaceIO.writeImage(face, rootDir);
		}
	}
}
