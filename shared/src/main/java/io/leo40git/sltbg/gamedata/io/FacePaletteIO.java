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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.gamedata.NamedFacePalette;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePaletteIO {
    private FacePaletteIO() {
        throw new UnsupportedOperationException("FacePaletteIO only contains static declarations.");
    }

    @Contract("_, _ -> new")
    public static @NotNull NamedFacePalette read(@NotNull JsonReader reader, boolean sort) throws IOException {
        String name = null;
        List<String> description = null, credits = null;
        HashSet<String> categoryNames = null;
        ArrayList<FaceCategory> categories = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME -> name = reader.nextString();
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readArray(reader, JsonReader::nextString);
                case FaceFields.CREDITS -> credits = JsonReadUtils.readArray(reader, JsonReader::nextString);
                case FaceFields.CATEGORIES -> {
                    if (categories == null) {
                        categories = new ArrayList<>();
                        categoryNames = new HashSet<>();
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String categoryName = reader.nextName();
                        if (!categoryNames.add(categoryName)) {
                            throw new MalformedJsonException(reader, "Category with name \"" + categoryName + "\" defined twice");
                        }
                        categories.add(FaceCategoryIO.read(reader, categoryName, false));
                    }
                    reader.endObject();
                }
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || categories == null) {
            var missingFields = new ArrayList<String>();
            if (name == null) {
                missingFields.add(FaceFields.NAME);
            }
            if (categories == null) {
                missingFields.add(FaceFields.CATEGORIES);
            }
            throw new MissingFieldsException(reader, "Face palette", missingFields);
        }

        var palette = new NamedFacePalette(name);
        if (description != null) {
            palette.getDescription().addAll(description);
        }
        if (credits != null) {
            palette.getCredits().addAll(credits);
        }

        for (var category : categories) {
            palette.add(category);
        }
        if (sort) {
            palette.sortIfNeeded();
        }
        return palette;
    }

    @Contract("_ -> new")
    public static @NotNull NamedFacePalette read(@NotNull JsonReader reader) throws IOException {
        return read(reader, true);
    }

    public static void write(@NotNull NamedFacePalette palette, @NotNull JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name(FaceFields.NAME);
        writer.value(palette.getName());

        if (palette.hasDescription()) {
            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, palette.getDescription());
        }

        if (palette.hasCredits()) {
            JsonWriteUtils.writeStringArray(writer, palette.getCredits());
        }

        writer.name(FaceFields.CATEGORIES);
        JsonWriteUtils.writeObject(writer, FaceCategoryIO::write, palette.getCategories());

        writer.endObject();
    }
}
