/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FacePaletteMerger {
    private FacePaletteMerger() {
        throw new UnsupportedOperationException("FacePaletteMerger only contains static declarations.");
    }

    @Contract("_, _ -> new")
    public static @NotNull FacePalette merge(@NotNull FacePalette first, FacePalette @NotNull ... rest) {
        var merged = first.clone();

        for (var other : rest) {
            for (var otherGroup : other.getGroups()) {
                var myGroup = merged.getGroup(otherGroup.getName());
                if (myGroup == null) {
                    myGroup = new FaceGroup(otherGroup.getName(), otherGroup.getFaces().size());
                    if (otherGroup.hasDescription()) {
                        myGroup.setDescription(otherGroup.getDescription());
                    }
                    merged.add(myGroup);
                }

                for (var face : otherGroup.getFaces()) {
                    myGroup.add(face);
                }
            }
        }

        return merged;
    }
}
