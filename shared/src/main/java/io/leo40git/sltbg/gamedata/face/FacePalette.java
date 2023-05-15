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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import org.quiltmc.json5.JsonReader;

public final class FacePalette implements Cloneable {
    private @NotNull String name;
    private @NotNull List<FaceGroup> groups;
    private @NotNull Map<String, FaceGroup> groupsLookup;
    private @Nullable List<String> description, credits;
    
    public FacePalette(@NotNull String name) {
        this.name = name;
        
        groups = new ArrayList<>();
        groupsLookup = new HashMap<>();
    }
    
    public FacePalette(@NotNull String name, int initialCapacity) {
        this.name = name;
        
        groups = new ArrayList<>(initialCapacity);
        groupsLookup = new HashMap<>(initialCapacity);
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    public @NotNull List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
        }
        return description;
    }

    public void setDescription(@NotNull Collection<String> description) {
        this.description = new ArrayList<>(description);
    }

    public void clearDescription() {
        description = null;
    }

    public boolean hasCredits() {
        return credits != null && !credits.isEmpty();
    }

    public @NotNull List<String> getCredits() {
        if (credits == null) {
            credits = new ArrayList<>();
        }
        return credits;
    }

    public void setCredits(@NotNull Collection<String> credits) {
        this.credits = new ArrayList<>(credits);
    }

    public void clearCredits() {
        credits = null;
    }

    public @NotNull @UnmodifiableView List<FaceGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public boolean containsGroup(@NotNull String name) {
        return groupsLookup.containsKey(name);
    }

    public @Nullable FaceGroup getGroup(@NotNull String name) {
        return groupsLookup.get(name);
    }

    public void add(@NotNull FaceGroup group) {
        if (groupsLookup.containsKey(group.getName())) {
            throw new IllegalArgumentException("FaceGroup with name \"" + group.getName() + "\" already exists in this category");
        }

        if (group.getPalette() != null) {
            group.getPalette().remove(group);
        }

        groups.add(group);
        groupsLookup.put(group.getName(), group);
        group.setPalette(this);
    }

    void rename(@NotNull FaceGroup group, @NotNull String newName) {
        if (groupsLookup.containsKey(newName)) {
            throw new IllegalArgumentException("FaceGroup with name \"" + newName + "\" already exists in this category");
        }

        groupsLookup.remove(group.getName());
        groupsLookup.put(newName, group);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean remove(@NotNull FaceGroup group) {
        if (groups.remove(group)) {
            groupsLookup.remove(group.getName());
            group.setPalette(null);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public @Nullable FaceGroup remove(@NotNull String name) {
        final FaceGroup group;

        group = groupsLookup.remove(name);
        if (group == null) {
            return null;
        }
        groups.remove(group);
        group.setPalette(null);

        return group;
    }

    public void clear() {
        groups.clear();
        groupsLookup.clear();
    }

    @Override
    public @NotNull FacePalette clone() {
        FacePalette clone;
        try {
            clone = (FacePalette) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Object.clone threw CloneNotSupportedException?!", e);
        }

        clone.groups = new ArrayList<>(groups.size());
        clone.groupsLookup = new HashMap<>(groups.size());
        for (var group : groups) {
            clone.add(group.clone());
        }

        return clone;
    }

    public void merge(@NotNull FacePalette other) {
        for (var otherGroup : other.getGroups()) {
            var myGroup = getGroup(otherGroup.getName());
            if (myGroup != null) {
                myGroup.merge(otherGroup);
            } else {
                add(otherGroup.clone());
            }
        }
    }

    @Contract("_, _ -> new")
    public static @NotNull FacePalette read(@NotNull JsonReader reader, @NotNull Path imageRoot) throws IOException {
        final String startLocation = reader.locationString();

        String name = null;
        Map<String, FaceGroup> groups = null;
        List<String> description = null, credits = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME -> name = reader.nextString();
                case FaceFields.GROUPS -> {
                    if (groups == null) {
                        groups = new HashMap<>();
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String groupName = reader.nextName();
                        if (groups.containsKey(groupName)) {
                            throw new MalformedJsonException("Duplicate group" + reader.locationString());
                        }

                        groups.put(groupName, readGroup(reader, imageRoot, groupName));
                    }
                    reader.endObject();
                }
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readStringArray(reader);
                case FaceFields.CREDITS -> credits = JsonReadUtils.readStringArray(reader);
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || groups == null) {
            var missingFields = new ArrayList<String>();
            if (name == null) {
                missingFields.add(FaceFields.NAME);
            }
            if (groups == null) {
                missingFields.add(FaceFields.GROUPS);
            }
            throw new MissingFieldsException("Face palette" + startLocation, missingFields);
        }

        var palette = new FacePalette(name, groups.size());
        for (var group : groups.values()) {
            palette.add(group);
        }

        if (description != null) {
            palette.setDescription(description);
        }

        if (credits != null) {
            palette.setCredits(credits);
        }

        return palette;
    }

    @Contract("_ -> new")
    public static @NotNull FacePalette read(@NotNull Path path) throws IOException {
        try (var reader = JsonReader.json5(path)) {
            return read(reader, path.getParent());
        }
    }

    @Contract("_, _, _ -> new")
    private static @NotNull FaceGroup readGroup(@NotNull JsonReader reader, @NotNull Path imageRoot,
                                                @NotNull String name) throws IOException {
        final String startLocation = reader.locationString();

        Map<String, Face> faces = null;
        String characterName = null;
        List<String> description = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME ->
                        throw new MalformedJsonException("'" + FaceFields.NAME + "' field not allowed" + reader.locationString());
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

                        faces.put(faceName, readFace(reader, imageRoot, faceName));
                    }
                    reader.endObject();
                }
                case FaceFields.CHARACTER_NAME -> characterName = reader.nextString();
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readStringArray(reader);
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (faces == null) {
            throw new MissingFieldsException("Face group" + startLocation, FaceFields.FACES);
        }

        var group = new FaceGroup(name, faces.size());
        for (var face : faces.values()) {
            group.add(face);
        }

        if (characterName != null) {
            group.setCharacterName(characterName);
        }

        if (description != null) {
            group.setDescription(description);
        }

        return group;
    }

    @Contract("_, _, _ -> new")
    private static @NotNull Face readFace(@NotNull JsonReader reader, @NotNull Path imageRoot,
                                          @NotNull String name) throws IOException {
        final String startLocation = reader.locationString();

        Path imagePath = null;
        String characterName = null;
        List<String> description = null;
        boolean icon = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME ->
                        throw new MalformedJsonException("'" + FaceFields.NAME + "' field not allowed" + reader.locationString());
                case FaceFields.IMAGE_PATH -> {
                    String relativePath = reader.nextString();
                    try {
                        imagePath = imageRoot.resolve(relativePath);
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

        if (imagePath == null) {
            throw new MissingFieldsException("Face" + startLocation, FaceFields.IMAGE_PATH);
        }

        var face = new Face(name, imagePath);

        if (characterName != null) {
            face.setCharacterName(characterName);
        }

        if (description != null) {
            face.setDescription(description);
        }

        face.setIcon(icon);

        return face;
    }
}
