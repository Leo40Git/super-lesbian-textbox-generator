/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class FaceCategory implements Comparable<FaceCategory> {
    private @Nullable FacePalette palette;
    private final @NotNull ArrayList<Face> faces;
    private final @NotNull HashMap<String, Face> facesLookup;
    private @NotNull String name;
    private boolean orderSet = false;
    private long order;
    private @Nullable String characterName;
    private @Nullable ArrayList<String> description;

    private @Nullable Face iconFace;
    private long lastOrder = FacePalette.DEFAULT_ORDER_BASE;
    private volatile boolean needsSort = false;

    public FaceCategory(@NotNull String name) {
        validateName(name);
        this.name = name;
        faces = new ArrayList<>();
        facesLookup = new HashMap<>();
    }

    public FaceCategory(@NotNull String name, int initialCapacity) {
        validateName(name);
        this.name = name;
        faces = new ArrayList<>(initialCapacity);
        facesLookup = new HashMap<>(initialCapacity);
    }

    /**
     * This constructor "takes ownership" of the {@literal faces} list -
     * <em>you should not modify this list after passing it to this constructor!</em>
     */
    public FaceCategory(@NotNull String name, @NotNull ArrayList<Face> faces) {
        validateName(name);
        this.name = name;
        this.faces = faces;
        facesLookup = new HashMap<>(faces.size());

        for (var face : faces) {
            if (facesLookup.put(face.getName(), face) != null) {
                throw new IllegalArgumentException("faces contains 2 elements with the same name: \"%s\""
                        .formatted(face.getName()));
            }
            face.onAddedToCategory(this);
        }
    }

    public @Nullable FacePalette getPalette() {
        return palette;
    }

    void onAddedToPalette(@NotNull FacePalette palette) {
        this.palette = palette;
        for (var face : faces) {
            face.onAddedToPalette(palette);
        }
    }

    void onRemovedFromPalette() {
        palette = null;
        for (var face : faces) {
            face.onRemovedFromPalette();
        }
    }

    public @NotNull String getName() {
        return name;
    }

    private static void validateName(@NotNull String name) {
        if (name.contains(Face.PATH_DELIMITER)) {
            throw new IllegalArgumentException("Name \"%s\" contains path delimiter '%s'".formatted(name, Face.PATH_DELIMITER));
        }
    }

    public void setName(@NotNull String name) {
        validateName(name);

        if (palette != null) {
            palette.rename(this, name);
        }

        this.name = name;
        for (var face : faces) {
            face.onCategoryRenamed();
        }
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
            if (palette != null) {
                palette.markDirty();
            }
        }
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

    public void clearDescription() {
        description = null;
    }

    void markDirty() {
        needsSort = true;
    }

    public void sortIfNeeded() {
        if (needsSort) {
            synchronized (faces) {
                faces.sort(Comparator.naturalOrder());
            }

            needsSort = false;
        }
    }

    public @NotNull @UnmodifiableView List<Face> getFaces() {
        sortIfNeeded();
        return Collections.unmodifiableList(faces);
    }

    public @Nullable Face getFace(@NotNull String name) {
        return facesLookup.get(name);
    }

    public void add(@NotNull Face face) {
        if (face.getCategory() != null) {
            throw new IllegalArgumentException("Face is already part of other category: \"" + face.getCategory().getName() + "\"");
        }

        synchronized (faces) {
            if (facesLookup.put(face.getName(), face) != null) {
                throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
            }

            faces.add(face);
            face.onAddedToCategory(this);

            if (iconFace == null) {
                iconFace = face;
            }

            if (face.isOrderSet()) {
                if (face.getOrder() > lastOrder) {
                    lastOrder = face.getOrder();
                }
            } else {
                face.setOrder(lastOrder = FacePalette.getNextOrder(lastOrder));
            }
        }

        markDirty();
    }

    void rename(@NotNull Face face, @NotNull String newName) {
        synchronized (faces) {
            if (facesLookup.containsKey(newName)) {
                throw new IllegalArgumentException("Face with name \"" + newName + "\" already exists in this category");
            }

            facesLookup.remove(face.getName());
            facesLookup.put(newName, face);
        }
    }

    public boolean contains(@NotNull String name) {
        synchronized (faces) {
            return facesLookup.containsKey(name);
        }
    }

    private void remove0(@NotNull Face face) {
        face.onRemovedFromCategory();

        boolean doMarkDirty = true;

        if (iconFace == face) {
            if (!faces.isEmpty()) {
                faces.sort(Comparator.naturalOrder());
                doMarkDirty = needsSort = false;
                iconFace = faces.get(0);
            } else {
                iconFace = null;
            }
        }

        if (doMarkDirty) {
            markDirty();
        }
    }

    public boolean remove(@NotNull Face face) {
        synchronized (faces) {
            if (faces.remove(face)) {
                facesLookup.remove(face.getName());
                remove0(face);
                return true;
            } else {
                return false;
            }
        }
    }

    public @Nullable Face remove(@NotNull String name) {
        final Face face;

        synchronized (faces) {
            face = facesLookup.remove(name);
            if (face == null) {
                return null;
            }
            faces.remove(face);
            remove0(face);
        }

        return face;
    }

    public void clear() {
        synchronized (faces) {
            for (var face : faces) {
                face.onRemovedFromCategory();
            }

            faces.clear();
            facesLookup.clear();

            iconFace = null;
        }

        needsSort = false;
    }

    @Contract(" -> new")
    public @NotNull FaceCategory copy() {
        var clone = new FaceCategory(name, faces.size());

        for (var faces : faces) {
            clone.add(faces.copy());
        }

        clone.order = order;
        clone.orderSet = orderSet;
        clone.characterName = characterName;
        if (description != null) {
            clone.description = new ArrayList<>(description);
        }
        return clone;
    }

    @Override
    public int compareTo(@NotNull FaceCategory o) {
        if (order != o.order) {
            return Long.compare(order, o.getOrder());
        } else {
            return name.compareTo(o.getName());
        }
    }
}
