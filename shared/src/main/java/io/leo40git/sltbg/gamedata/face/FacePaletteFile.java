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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePaletteFile extends FacePalette {
    private @NotNull String name;
    private @NotNull Path rootDirectory;
    private @Nullable List<String> description, credits;

    public FacePaletteFile(@NotNull String name, @NotNull Path rootDirectory) {
        this.name = name;
        this.rootDirectory = rootDirectory;
    }

    public FacePaletteFile(@NotNull String name, @NotNull Path rootDirectory, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
        this.rootDirectory = rootDirectory;
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

    public synchronized void setRootDirectory(@NotNull Path rootDirectory) {
        var oldRootDirectory = this.rootDirectory;
        this.rootDirectory = rootDirectory;

        for (var category : getCategories()) {
            for (var face : category.getFaces()) {
                var imagePath = face.getImagePath();
                imagePath = oldRootDirectory.relativize(imagePath);
                imagePath = rootDirectory.resolve(imagePath);
                face.setImagePath(imagePath);
            }
        }
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

    public synchronized void setDescription(@NotNull Collection<String> description) {
        this.description = new ArrayList<>(description);
    }

    public synchronized void clearDescription() {
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

    public synchronized void setCredits(@NotNull Collection<String> credits) {
        this.credits = new ArrayList<>(credits);
    }

    public synchronized void clearCredits() {
        credits = null;
    }

    @Contract("_, _ -> new")
    public static @NotNull FacePaletteFile read(@NotNull JsonReader reader, @NotNull Path rootDirectory) throws IOException {
        String name = null;
        Map<String, FaceCategory> categories = null;
        List<String> description = null, credits = null;

        String loc = reader.locationString();
        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME -> name = reader.nextName();
                case FaceFields.CATEGORIES -> {
                    if (categories == null) {
                        categories = new HashMap<>();
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String categoryName = reader.nextName();
                        if (categories.containsKey(categoryName)) {
                            throw new MalformedJsonException("Duplicate face category" + reader.locationString());
                        }

                        categories.put(categoryName, readCategory(reader, rootDirectory, categoryName));
                    }
                    reader.endObject();
                }
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readStringArray(reader);
                case FaceFields.CREDITS -> credits = JsonReadUtils.readStringArray(reader);
            }
        }

        if (name == null || categories == null) {
            var missingFields = new ArrayList<String>();
            if (name == null) {
                missingFields.add(FaceFields.NAME);
            }
            if (categories == null) {
                missingFields.add(FaceFields.CATEGORIES);
            }
            throw new MissingFieldsException("Face palette" + loc, missingFields);
        }

        var palette = new FacePaletteFile(name, rootDirectory, categories.size());
        for (var category : categories.values()) {
            palette.add(category);
        }

        if (description != null) {
            palette.setDescription(description);
        }
        if (credits != null) {
            palette.setCredits(credits);
        }

        palette.sortIfNeeded();
        return palette;
    }

    public static @NotNull FacePaletteFile read(@NotNull Path filePath) throws IOException {
        var rootDirectory = filePath.getParent();
        if (rootDirectory == null) {
            throw new IOException("Path \"" + filePath + "\" does not have a parent");
        }

        try (var reader = JsonReader.json5(filePath)) {
            return read(reader, rootDirectory);
        }
    }

    public void write(@NotNull JsonWriter writer) throws IOException {
        sortIfNeeded();

        writer.beginObject();

        writer.name(FaceFields.NAME).value(name);
        if (hasDescription()) {
            assert description != null;

            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, description);
        }
        if (hasCredits()) {
            assert credits != null;

            writer.name(FaceFields.CREDITS);
            JsonWriteUtils.writeStringArray(writer, credits);
        }

        writer.name(FaceFields.CATEGORIES);
        writer.beginObject();
        for (var category : getCategories()) {
            writeCategory(writer, category);
        }
        writer.endObject();

        writer.endObject();
    }

    @Contract("_, _, _ -> new")
    private static @NotNull FaceCategory readCategory(@NotNull JsonReader reader, @NotNull Path rootDirectory, @NotNull String name) throws IOException {
        Map<String, Face> faces = null;
        long order = 0;
        boolean gotOrder = false;
        String characterName = null;
        List<String> description = null;

        String loc = reader.locationString();
        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.FACES -> {
                    if (faces == null) {
                        faces = new HashMap<>();
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String faceName = reader.nextName();
                        if (faces.containsKey(faceName)) {
                            throw new MalformedJsonException("Duplicate face" + reader.locationString());
                        }

                        faces.put(faceName, readFace(reader, rootDirectory, faceName));
                    }
                    reader.endObject();
                }
                case FaceFields.ORDER -> {
                    order = reader.nextLong();
                    gotOrder = true;
                }
                case FaceFields.CHARACTER_NAME -> characterName = reader.nextString();
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readStringArray(reader);
                default -> reader.skipValue();
            }
        }

        if (faces == null || !gotOrder) {
            var missingFields = new ArrayList<String>();
            if (faces == null) {
                missingFields.add(FaceFields.FACES);
            }
            if (!gotOrder) {
                missingFields.add(FaceFields.ORDER);
            }
            throw new MissingFieldsException("Face category" + loc, missingFields);
        }

        var category = new FaceCategory(name, order, faces.size());
        for (var face : faces.values()) {
            category.add(face);
        }

        if (characterName != null) {
            category.setCharacterName(characterName);
        }
        if (description != null) {
            category.setDescription(description);
        }

        category.sortIfNeeded();
        return category;
    }

    @Contract("_, _, _ -> new")
    private static @NotNull Face readFace(@NotNull JsonReader reader, @NotNull Path rootDirectory, @NotNull String name) throws IOException {
        Path imagePath = null;
        long order = 0;
        boolean gotOrder = false;
        String characterName = null;
        List<String> description = null;
        boolean icon = false;

        String loc = reader.locationString();
        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.ORDER -> {
                    order = reader.nextLong();
                    gotOrder = true;
                }
                case FaceFields.IMAGE_PATH -> {
                    String relativePath = reader.nextString();
                    try {
                        imagePath = rootDirectory.resolve(relativePath);
                    } catch (InvalidPathException e) {
                        throw new MalformedJsonException("Image path" + reader.locationString() + " is invalid: \"" + relativePath + "\"", e);
                    }
                }
                case FaceFields.CHARACTER_NAME -> characterName = reader.nextString();
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readStringArray(reader);
                case FaceFields.ICON -> icon = reader.nextBoolean();
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (imagePath == null || !gotOrder) {
            var missingFields = new ArrayList<String>();
            if (imagePath == null) {
                missingFields.add(FaceFields.IMAGE_PATH);
            }
            if (!gotOrder) {
                missingFields.add(FaceFields.ORDER);
            }
            throw new MissingFieldsException("Face" + loc, missingFields);
        }

        var face = new Face(name, imagePath, order);

        if (characterName != null) {
            face.setCharacterName(characterName);
        }
        if (description != null) {
            face.setDescription(description);
        }
        face.setIcon(icon);

        return face;
    }

    private void writeCategory(@NotNull JsonWriter writer, @NotNull FaceCategory category) throws IOException {
        category.sortIfNeeded();

        writer.name(category.getName());
        writer.beginObject();

        writer.name(FaceFields.ORDER).value(category.getOrder());
        if (category.getCharacterName() != null) {
            writer.name(FaceFields.CHARACTER_NAME).value(category.getCharacterName());
        }
        if (category.hasDescription()) {
            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, category.getDescription());
        }

        writer.name(FaceFields.FACES);
        writer.beginObject();
        for (var face : category.getFaces()) {
            writeFace(writer, face);
        }
        writer.endObject();

        writer.endObject();
    }

    private void writeFace(@NotNull JsonWriter writer, @NotNull Face face) throws IOException {
        writer.name(face.getName());
        writer.beginObject();

        writer.name(FaceFields.ORDER).value(face.getOrder());
        writer.name(FaceFields.IMAGE_PATH).value(rootDirectory.relativize(face.getImagePath()).toString());

        if (face.isCharacterNameSet()) {
            writer.name(FaceFields.CHARACTER_NAME).value(face.getCharacterName());
        }
        if (face.hasDescription()) {
            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, face.getDescription());
        }
        if (face.isIcon()) {
            writer.name(FaceFields.ICON).value(true);
        }

        writer.endObject();
    }
}
