/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Face implements Comparable<Face> {
    public static final String PATH_DELIMITER = "/";

    private @Nullable FacePaletteFile sourceFile;
    private @Nullable FacePalette palette;
    private @Nullable FaceCategory category;
    @SuppressWarnings("FieldMayBeFinal")
    private @NotNull String name;
    private @NotNull String imagePath;
    private long order;
    private @Nullable String characterName;
    private boolean characterNameSet;
    private boolean icon;
    private @Nullable List<String> description;

    Face(@NotNull String name, @NotNull String imagePath, long order) {
        this.name = name;
        this.imagePath = imagePath;
        this.order = order;
    }

    public @Nullable FacePaletteFile getSourceFile() {
        return sourceFile;
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
        if (palette instanceof FacePaletteFile namedPalette) {
            this.sourceFile = namedPalette;
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
        if (sourceFile == palette) {
            sourceFile = null;
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
            category.markDirty();
        }

        this.name = name;
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

    public @NotNull Path resolveImagePath() {
        if (sourceFile == null) {
            throw new IllegalStateException("No source palette!");
        }
        return sourceFile.getRootDirectory().resolve(imagePath);
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        if (this.order != order) {
            this.order = order;
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

    public boolean isIcon() {
        return icon;
    }

    public void setIcon(boolean icon) {
        this.icon = icon;
        if (category != null) {
            category.markDirty();
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

    public void clearDescription() {
        description = null;
    }

    @Contract(" -> new")
    public @NotNull Face copy() {
        var clone = new Face(name, imagePath, order);

        clone.sourceFile = sourceFile;
        if (characterNameSet) {
            clone.characterName = characterName;
            clone.characterNameSet = true;
        }
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
