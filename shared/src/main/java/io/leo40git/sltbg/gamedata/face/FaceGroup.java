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

    public @Nullable String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(@Nullable String characterName) {
        this.characterName = characterName;
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

    public @Nullable FacePalette getPalette() {
        return palette;
    }

    void setPalette(@Nullable FacePalette palette) {
        this.palette = palette;
    }

    public synchronized @NotNull @UnmodifiableView List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    public synchronized boolean containsFace(@NotNull String name) {
        return facesLookup.containsKey(name);
    }

    public synchronized @Nullable Face getFace(@NotNull String name) {
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

    private synchronized void markDirty() {
        iconFace = null;
        iconFaceSet = false;
    }

    public synchronized void add(@NotNull Face face) {
        if (facesLookup.containsKey(face.getName())) {
            throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
        }

        if (face.getGroup() != null) {
            face.getGroup().remove(face);
        }

        faces.add(face);
        face.setGroup(this);
        markDirty();
    }

    synchronized void rename(@NotNull Face face, @NotNull String newName) {
        if (facesLookup.containsKey(newName)) {
            throw new IllegalArgumentException("Face with name \"" + newName + "\" already exists in this category");
        }

        facesLookup.remove(face.getName());
        facesLookup.put(newName, face);
    }

    @SuppressWarnings("UnusedReturnValue")
    public synchronized boolean remove(@NotNull Face face) {
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
    public synchronized @Nullable Face remove(@NotNull String name) {
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

    public synchronized void clear() {
        faces.clear();
        facesLookup.clear();
        markDirty();
    }

    @Override
    public synchronized @NotNull FaceGroup clone() {
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

    public synchronized void merge(@NotNull FaceGroup other) {
        for (var otherFace : other.getFaces()) {
            if (facesLookup.containsKey(otherFace.getName())) {
                throw new IllegalArgumentException("Face with name \"" + otherFace.getName() + "\" already exists in this category");
            }

            var myFace = otherFace.clone();
            faces.add(myFace);
            facesLookup.put(myFace.getName(), myFace);
            myFace.setGroup(this);
        }

        markDirty();
    }
}
