/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class NamedFacePalette extends FacePalette {
    private @NotNull String name;
    private @Nullable ArrayList<String> description = null, credits = null;

    public NamedFacePalette(@NotNull String name) {
        super();
        this.name = name;
    }

    public NamedFacePalette(@NotNull String name, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
    }

    private NamedFacePalette(@NotNull String name, @NotNull ArrayList<FaceCategory> categories) {
        super(categories);
        this.name = name;
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
                        categories.add(FaceCategory.read(reader, categoryName, false));
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

        var palette = new NamedFacePalette(name, categories);
        if (description != null) {
            palette.getDescription().addAll(description);
        }
        if (credits != null) {
            palette.getCredits().addAll(credits);
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

    public void write(@NotNull JsonWriter writer) throws IOException {
        sortIfNeeded();

        writer.beginObject();

        writer.name(FaceFields.NAME);
        writer.value(name);

        if (description != null && !description.isEmpty()) {
            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, description);
        }

        if (credits != null && !credits.isEmpty()) {
            JsonWriteUtils.writeStringArray(writer, credits);
        }

        writer.name(FaceFields.CATEGORIES);
        JsonWriteUtils.writeObject(writer, FaceCategory::write, getCategories());

        writer.endObject();
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    public synchronized @NotNull List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
        }
        return description;
    }

    public void clearDescription() {
        description = null;
    }

    public boolean hasCredits() {
        return credits != null && !credits.isEmpty();
    }

    public synchronized @NotNull List<String> getCredits() {
        if (credits == null) {
            credits = new ArrayList<>();
        }
        return credits;
    }

    public void clearCredits() {
        credits = null;
    }
}
