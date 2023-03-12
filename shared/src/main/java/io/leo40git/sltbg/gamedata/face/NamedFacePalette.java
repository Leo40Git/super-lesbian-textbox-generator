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
import java.nio.file.Path;
import java.util.ArrayList;
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
    private @NotNull Path rootDirectory;
    private @Nullable ArrayList<String> description = null, credits = null;

    public NamedFacePalette(@NotNull String name, @NotNull Path rootDirectory) {
        super();
        this.name = name;
        this.rootDirectory = rootDirectory;
    }

    public NamedFacePalette(@NotNull String name, @NotNull Path rootDirectory, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
        this.rootDirectory = rootDirectory;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull NamedFacePalette read(@NotNull JsonReader reader, @NotNull Path rootDirectory, boolean sort) throws IOException {
        var palette = new NamedFacePalette("", rootDirectory);

        boolean gotName = false, gotCategories = false;

        String startLocStr = reader.locationString();

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME -> {
                    palette.setName(reader.nextString());
                    gotName = true;
                }
                case FaceFields.DESCRIPTION -> JsonReadUtils.readArray(reader, JsonReader::nextString, palette.getDescription()::add);
                case FaceFields.CREDITS -> JsonReadUtils.readArray(reader, JsonReader::nextString, palette.getCredits()::add);
                case FaceFields.CATEGORIES -> {
                    try {
                        JsonReadUtils.readSimpleMap(reader, FaceCategory::read, palette::add);
                    } catch (IllegalArgumentException e) {
                        throw new MalformedJsonException("Duplicate category" + reader.locationString());
                    }

                    gotCategories = true;
                }
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (!gotName || !gotCategories) {
            var missingFields = new ArrayList<String>();
            if (gotName) {
                missingFields.add(FaceFields.NAME);
            }
            if (gotCategories) {
                missingFields.add(FaceFields.CATEGORIES);
            }
            throw new MissingFieldsException("Face palette" + startLocStr, missingFields);
        }

        if (sort) {
            palette.sortIfNeeded();
        }
        return palette;
    }

    @Contract("_, _ -> new")
    public static @NotNull NamedFacePalette read(@NotNull JsonReader reader, @NotNull Path rootDirectory) throws IOException {
        return read(reader, rootDirectory, true);
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

    public @NotNull Path getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(@NotNull Path rootDirectory) {
        this.rootDirectory = rootDirectory;
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
