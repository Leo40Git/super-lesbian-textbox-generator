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
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Face implements Comparable<Face> {
    public static final String PATH_DELIMITER = "/";

    private @Nullable FacePalette palette;
    private @Nullable FaceCategory category;
    private @NotNull String name;
    private @NotNull Path imagePath;
    private long order;
    private @Nullable String characterName;
    private boolean characterNameSet;
    private boolean icon;
    private @Nullable List<String> description;

    public Face(@NotNull String name, @NotNull Path imagePath, long order) {
        this.name = name;
        this.imagePath = imagePath;
        this.order = order;
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

    public @NotNull Path getImagePath() {
        return imagePath;
    }

    public void setImagePath(@NotNull Path imagePath) {
        this.imagePath = imagePath;
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

    public synchronized void setDescription(@NotNull Collection<String> description) {
        this.description = new ArrayList<>(description);
    }

    public synchronized void clearDescription() {
        description = null;
    }

    @Contract(" -> new")
    public @NotNull Face copy() {
        var clone = new Face(name, imagePath, order);

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

    @Override
    public String toString() {
        if (category != null) {
            return category.getName() + PATH_DELIMITER + name;
        } else {
            return name;
        }
    }
}
