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
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class Face implements Cloneable {
    public static final String PATH_DELIMITER = "/";

    private @NotNull String name;
    private @NotNull Path imagePath;
    private @Nullable String after;
    private @Nullable String characterName;
    private boolean characterNameSet;
    private boolean icon;
    private @Nullable List<String> description;
    private @Nullable FaceGroup group;
    private @Nullable FacePalette palette, sourcePalette;

    public Face(@NotNull String name, @NotNull Path imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        if (group != null) {
            group.rename(this, name);
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

    public @Nullable String getAfter() {
        return after;
    }

    public void setAfter(@Nullable String after) {
        this.after = after;
    }

    public @NotNull String getCharacterName() {
        if (characterNameSet) {
            assert characterName != null;
            return characterName;
        }

        if (group != null && group.getCharacterName() != null) {
            return group.getCharacterName();
        }

        if (characterName == null) {
            int commaIndex = name.indexOf(',');
            if (commaIndex < 0) {
                characterName = name;
            } else {
                characterName = name.substring(0, commaIndex);
            }
        }

        return characterName;
    }

    public void setCharacterName(@Nullable String characterName) {
        this.characterName = characterName;
        characterNameSet = characterName != null;
    }

    public boolean isIcon() {
        return icon;
    }

    public void setIcon(boolean icon) {
        this.icon = icon;
    }

    public @NotNull @UnmodifiableView List<String> getDescription() {
        if (description == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(description);
        }
    }

    public void setDescription(@Nullable Collection<String> description) {
        if (description != null && !description.isEmpty()) {
            this.description = new ArrayList<>(description);
        } else {
            this.description = null;
        }
    }

    public @Nullable FaceGroup getGroup() {
        return group;
    }

    public @Nullable FacePalette getPalette() {
        return palette;
    }

    void setGroup(@Nullable FaceGroup group) {
        this.group = group;

        if (group != null) {
            palette = group.getPalette();
        } else {
            palette = null;
        }
    }

    public void remove() {
        if (group != null) {
            group.remove(this);
        }
    }

    public @Nullable FacePalette getSourcePalette() {
        return sourcePalette;
    }

    void setSourcePalette(@Nullable FacePalette sourcePalette) {
        this.sourcePalette = sourcePalette;
    }

    @Override
    public @NotNull Face clone() {
        Face clone;
        try {
            clone = (Face) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Object.clone threw CloneNotSupportedException?!", e);
        }

        clone.setGroup(null);
        clone.setDescription(description);
        return clone;
    }

    @Override
    public String toString() {
        if (group != null) {
            return group.getName() + PATH_DELIMITER + name;
        } else {
            return name;
        }
    }
}
