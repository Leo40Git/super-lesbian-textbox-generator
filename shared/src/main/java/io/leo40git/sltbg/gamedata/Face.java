/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.JsonWriter;

public final class Face implements Comparable<Face> {
    public static final String PATH_DELIMITER = "/";

    private @Nullable NamedFacePalette sourcePalette;
    private @Nullable FacePalette palette;
    private @Nullable FaceCategory category;
    @SuppressWarnings("FieldMayBeFinal")
    private @NotNull String name;
    private @NotNull String imagePath;
    private long order;
    private boolean orderSet;
    private @Nullable String characterName;
    private boolean characterNameSet;
    private @Nullable ArrayList<String> description;

    public Face(@NotNull String name, @NotNull String imagePath) {
        this.name = name;
        this.imagePath = imagePath;

        sourcePalette = null;
        palette = null;
        category = null;
        order = 0;
        orderSet = false;
        characterName = null;
        characterNameSet = false;
        description = null;
    }

    @Contract("_, _ -> new")
    public static @NotNull Face read(@NotNull JsonReader reader, @NotNull String name) throws IOException {
        String imagePath = null;
        boolean orderSet = false;
        long order = 0;
        String characterName = null;
        List<String> description = null;

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
                    case FaceFields.CHARACTER_NAME -> characterName = reader.nextString();
                    case FaceFields.DESCRIPTION -> description = JsonReadUtils.readArray(reader, JsonReader::nextString);
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
        if (characterName != null) {
            face.setCharacterName(characterName);
        }
        if (description != null) {
            face.getDescription().addAll(description);
        }
        return face;
    }

    public void write(@NotNull JsonWriter writer) throws IOException {
        writer.name(name);
        if (!orderSet && !characterNameSet && (description == null || description.isEmpty())) {
            writer.value(imagePath);
        } else {
            writer.beginObject();
            writer.name(FaceFields.IMAGE_PATH);
            writer.value(imagePath);
            if (orderSet) {
                writer.name(FaceFields.ORDER);
                writer.value(order);
            }
            if (characterNameSet) {
                writer.name(FaceFields.CHARACTER_NAME);
                writer.value(characterName);
            }
            if (description != null && !description.isEmpty()) {
                writer.name(FaceFields.DESCRIPTION);
                JsonWriteUtils.writeStringArray(writer, description);
            }
            writer.endObject();
        }
    }

    public @Nullable NamedFacePalette getSourcePalette() {
        return sourcePalette;
    }

    public @Nullable FacePalette getPalette() {
        return palette;
    }

    public @Nullable FaceCategory getCategory() {
        return category;
    }

    void onCategoryRenamed() {
    }

    void onAddedToPalette(@NotNull FacePalette palette) {
        if (palette instanceof NamedFacePalette namedPalette) {
            this.sourcePalette = namedPalette;
        }
        this.palette = palette;
    }

    void onAddedToCategory(@NotNull FaceCategory category) {
        if (category.getPalette() != null) {
            onAddedToPalette(category.getPalette());
        }
        this.category = category;
        onCategoryRenamed();
    }

    void onRemovedFromPalette() {
        if (sourcePalette == palette) {
            sourcePalette = null;
        }
        palette = null;
    }

    void onRemovedFromCategory() {
        onRemovedFromPalette();
        category = null;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        if (category != null) {
            category.rename(this, name);
        }

        if (!characterNameSet) {
            characterName = null;
        }
    }

    public @NotNull String getImagePath() {
        return imagePath;
    }

    public void setImagePath(@NotNull String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isOrderSet() {
        return orderSet;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        if (!orderSet || this.order != order) {
            this.order = order;
            orderSet = true;
            if (category != null) {
                category.markDirty();
            }
        }
    }

    public boolean isCharacterNameSet() {
        return characterNameSet;
    }

    public @NotNull String getCharacterName() {
        if (characterNameSet) {
            assert characterName != null;
            return characterName;
        } else if (category != null && category.getCharacterName() != null) {
            return category.getCharacterName();
        } else {
            int commaIndex = name.indexOf(',');
            if (commaIndex < 0) {
                characterName = name;
            } else {
                characterName = name.substring(0, commaIndex);
            }
            return characterName;
        }
    }

    public void setCharacterName(@NotNull String characterName) {
        this.characterName = characterName;
        characterNameSet = true;
    }

    public void clearCharacterName() {
        characterName = null;
        characterNameSet = false;
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

    @Contract(" -> new")
    public @NotNull Face copy() {
        var clone = new Face(name, imagePath);
        clone.sourcePalette = sourcePalette;
        clone.order = order;
        clone.orderSet = orderSet;
        clone.characterName = characterName;
        clone.characterNameSet = characterNameSet;
        if (description != null) {
            clone.description = new ArrayList<>(description);
        }
        return clone;
    }

    @Override
    public int compareTo(@NotNull Face o) {
        if (order != o.order) {
            return Long.compare(order, o.getOrder());
        } else {
            return name.compareTo(o.getName());
        }
    }
}
