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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Face implements Cloneable {
    public static final String PATH_DELIMITER = "/";

    private @NotNull String name;
    private @NotNull Path imagePath;
    private @Nullable String characterName;
    private boolean characterNameSet;
    private boolean icon;
    private @Nullable List<String> description;
    private @Nullable FaceGroup group;

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

    public @Nullable FaceGroup getGroup() {
        return group;
    }

    void setGroup(@Nullable FaceGroup group) {
        this.group = group;
    }

    @Override
    public @NotNull Face clone() {
        Face clone;
        try {
            clone = (Face) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Object.clone threw CloneNotSupportedException?!", e);
        }

        clone.group = null;

        if (hasDescription()) {
            clone.setDescription(getDescription());
        } else {
            clone.description = null;
        }

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
