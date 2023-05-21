/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class FaceGroup implements Cloneable {
    private @NotNull String name;
    private @NotNull List<Face> faces;
    private @NotNull Map<String, Face> facesLookup;
    private @Nullable String after;
    private @Nullable String characterName;
    private @Nullable List<String> description;
    private @Nullable FacePalette palette;

    private @Nullable Face iconFace;
    private boolean iconFaceSet;

    public FaceGroup(@NotNull String name) {
        this.name = name;

        faces = new ArrayList<>();
        facesLookup = new HashMap<>();
    }

    public FaceGroup(@NotNull String name, int initialCapacity) {
        this.name = name;

        faces = new ArrayList<>(initialCapacity);
        facesLookup = new HashMap<>(initialCapacity);
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        if (palette != null) {
            palette.rename(this, name);
        }

        this.name = name;
    }

    public @Nullable String getAfter() {
        return after;
    }

    public void setAfter(@Nullable String after) {
        this.after = after;
    }

    public @Nullable String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(@Nullable String characterName) {
        this.characterName = characterName;
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

    public @Nullable FacePalette getPalette() {
        return palette;
    }

    void setPalette(@Nullable FacePalette palette) {
        this.palette = palette;

        for (var face : faces) {
            face.setGroup(this);
        }
    }

    public @NotNull @UnmodifiableView List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    public int size() {
        return faces.size();
    }

    public boolean containsFace(@NotNull Face face) {
        return faces.contains(face);
    }

    public boolean containsName(@NotNull String name) {
        return facesLookup.containsKey(name);
    }

    public int indexOf(@NotNull Face face) {
        return faces.indexOf(face);
    }

    public int indexOf(@NotNull String name) {
        var face = getFace(name);
        if (face != null) {
            return indexOf(face);
        } else {
            return -1;
        }
    }

    public @Nullable Face getFace(@NotNull String name) {
        return facesLookup.get(name);
    }

    public @Nullable Face getIconFace() {
        if (!iconFaceSet) {
            for (var face : faces) {
                if (face.isIcon()) {
                    iconFace = face;
                    break;
                }
            }

            iconFaceSet = true;
        }

        return iconFace;
    }

    private void markDirty() {
        iconFace = null;
        iconFaceSet = false;
    }

    public void add(@NotNull Face face) {
        if (facesLookup.containsKey(face.getName())) {
            throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
        }

        if (face.getGroup() != null) {
            face.getGroup().remove(face);
        }

        faces.add(face);
        facesLookup.put(face.getName(), face);
        face.setGroup(this);
        markDirty();
    }

    public void add(int index, @NotNull Face face) {
        if (facesLookup.containsKey(face.getName())) {
            throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
        }

        if (face.getGroup() != null) {
            face.getGroup().remove(face);
        }

        faces.add(index, face);
        facesLookup.put(face.getName(), face);
        face.setGroup(this);
        markDirty();
    }

    void rename(@NotNull Face face, @NotNull String newName) {
        if (facesLookup.containsKey(newName)) {
            throw new IllegalArgumentException("Face with name \"" + newName + "\" already exists in this category");
        }

        facesLookup.remove(face.getName());
        facesLookup.put(newName, face);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean remove(@NotNull Face face) {
        if (faces.remove(face)) {
            facesLookup.remove(face.getName());
            face.setGroup(null);
            markDirty();
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public @Nullable Face remove(@NotNull String name) {
        final Face face;

        face = facesLookup.remove(name);
        if (face == null) {
            return null;
        }
        faces.remove(face);
        face.setGroup(null);
        markDirty();

        return face;
    }

    @SuppressWarnings("UnusedReturnValue")
    public @NotNull Face remove(int index) {
        final Face face;

        face = faces.remove(index);
        facesLookup.remove(face.getName());
        face.setGroup(null);
        markDirty();

        return face;
    }

    public void clear() {
        for (var face : faces) {
            face.setGroup(null);
        }
        faces.clear();
        facesLookup.clear();
        markDirty();
    }

    @Override
    public @NotNull FaceGroup clone() {
        FaceGroup clone;
        try {
            clone = (FaceGroup) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Object.clone threw CloneNotSupportedException?!", e);
        }

        clone.palette = null;
        clone.markDirty();

        clone.faces = new ArrayList<>(faces.size());
        clone.facesLookup = new HashMap<>(faces.size());
        for (var face : faces) {
            clone.add(face.clone());
        }

        return clone;
    }
}
