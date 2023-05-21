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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FacePaletteMerger {
    private FacePaletteMerger() {
        throw new UnsupportedOperationException("FacePaletteMerger only contains static declarations.");
    }

    @Contract("_, _, _ -> new")
    public static @NotNull FacePalette merge(@NotNull String name, @NotNull FacePalette first, FacePalette @NotNull ... rest) {
        var myPalette = new PaletteHelper(name, first);

        for (var otherPalette : rest) {
            for (var otherGroup : otherPalette.getGroups()) {
                myPalette.add(otherGroup);
            }
        }

        return myPalette.build();
    }

    private static final class PaletteHelper {
        private final @NotNull String name;
        private final @NotNull List<GroupHelper> groups;
        private final @NotNull Map<String, GroupHelper> groupsLookup;
        private @Nullable Map<String, Map<String, GroupHelper>> pendingGroupsByAfter;

        public PaletteHelper(@NotNull String name, @NotNull FacePalette first) {
            this.name = name;
            groups = new ArrayList<>(first.size());
            groupsLookup = new HashMap<>(first.size());

            for (var group : first.getGroups()) {
                add(group);
            }
        }

        public void add(@NotNull FaceGroup group) {
            var helper = groupsLookup.get(group.getName());

            if (helper == null) {
                helper = new GroupHelper(group);
                if (group.getAfter() == null) {
                    groups.add(helper);
                    groupsLookup.put(group.getName(), helper);
                } else {
                    var precedingGroup = groupsLookup.get(group.getAfter());
                    if (precedingGroup != null) {
                        int index = groups.indexOf(precedingGroup);
                        groups.add(index + 1, helper);
                        groupsLookup.put(group.getName(), helper);
                    } else {
                        if (pendingGroupsByAfter == null) {
                            pendingGroupsByAfter = new LinkedHashMap<>();
                        }
                        var pendingGroups = pendingGroupsByAfter
                                .computeIfAbsent(group.getAfter(), ignored -> new LinkedHashMap<>());
                        var pendingGroup = pendingGroups.get(group.getName());
                        if (pendingGroup == null) {
                            pendingGroups.put(group.getName(), new GroupHelper(group));
                        } else {
                            for (var face : group.getFaces()) {
                                pendingGroup.add(face);
                            }
                        }
                        return;
                    }
                }

                if (pendingGroupsByAfter != null && !pendingGroupsByAfter.isEmpty()) {
                    var groupsAfterMe = pendingGroupsByAfter.remove(group.getName());
                    if (groupsAfterMe != null) {
                        int index = groups.indexOf(helper) + 1;
                        for (var pendingGroup : groupsAfterMe.values()) {
                            groups.add(index++, pendingGroup);
                            groupsLookup.put(pendingGroup.name, pendingGroup);
                        }
                    }
                }
            }

            for (var face : group.getFaces()) {
                helper.add(face);
            }
        }

        @Contract("-> new")
        public @NotNull FacePalette build() {
            var palette = new FacePalette(name, groups.size());
            for (var group : groups) {
                palette.add(group.build());
            }

            if (pendingGroupsByAfter != null && !pendingGroupsByAfter.isEmpty()) {
                for (var entry : pendingGroupsByAfter.entrySet()) {
                    final String warningLine = "(preceding group '" + entry.getKey() + "' is missing)";
                    for (var group : entry.getValue().values()) {
                        group.description.add(warningLine);
                        palette.add(group.build());
                    }
                }
            }

            return palette;
        }
    }

    private static final class GroupHelper {
        public final @NotNull String name;
        public final @Nullable String characterName;
        public final @NotNull List<String> description;

        private final @NotNull List<Face> faces;
        private final @NotNull Map<String, Face> facesLookup;
        private @Nullable Map<String, Map<String, Face>> pendingFacesByAfter;

        public GroupHelper(@NotNull FaceGroup group) {
            this.name = group.getName();
            this.characterName = group.getCharacterName();
            this.description = new ArrayList<>(group.getDescription());

            faces = new ArrayList<>(group.size());
            facesLookup = new HashMap<>(group.size());
            for (var face : group.getFaces()) {
                add(face);
            }
        }

        public void add(@NotNull Face face) {
            if (facesLookup.containsKey(face.getName())) {
                throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
            }

            final int insertIndex;
            if (face.getAfter() == null) {
                var myFace = face.clone();
                faces.add(myFace);
                facesLookup.put(face.getName(), myFace);
                insertIndex = faces.size();
            } else {
                var precedingFace = facesLookup.get(face.getAfter());
                if (precedingFace != null) {
                    var myFace = face.clone();
                    int index = faces.indexOf(precedingFace);
                    faces.add(index + 1, myFace);
                    facesLookup.put(face.getName(), myFace);
                    insertIndex = index + 2;
                } else {
                    if (pendingFacesByAfter == null) {
                        pendingFacesByAfter = new LinkedHashMap<>();
                    }
                    var pendingFaces = pendingFacesByAfter
                            .computeIfAbsent(face.getAfter(), ignored -> new LinkedHashMap<>());

                    if (pendingFaces.containsKey(face.getName())) {
                        throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
                    }
                    pendingFaces.put(face.getName(), face.clone());
                    return;
                }
            }

            if (pendingFacesByAfter != null && !pendingFacesByAfter.isEmpty()) {
                var facesAfterMe = pendingFacesByAfter.remove(face.getName());
                if (facesAfterMe != null) {
                    int index = insertIndex;
                    for (var pendingGroup : facesAfterMe.values()) {
                        faces.add(index++, pendingGroup);
                        facesLookup.put(pendingGroup.getName(), pendingGroup);
                    }
                }
            }
        }

        @Contract("-> new")
        public @NotNull FaceGroup build() {
            var group = new FaceGroup(name, faces.size());
            for (var face : faces) {
                group.add(face);
            }

            if (pendingFacesByAfter != null && !pendingFacesByAfter.isEmpty()) {
                for (var entry : pendingFacesByAfter.entrySet()) {
                    final String warningLine = "(preceding face '" + entry.getKey() + "' is missing)";
                    for (var face : entry.getValue().values()) {
                        var description = new ArrayList<>(face.getDescription());
                        description.add(warningLine);
                        face.setDescription(description);
                        group.add(face);
                    }
                }
            }

            return group;
        }
    }
}
