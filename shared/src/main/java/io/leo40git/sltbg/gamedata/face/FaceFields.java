/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

public final class FaceFields {
    private FaceFields() {
        throw new UnsupportedOperationException("FaceFields only contains static declarations.");
    }

    public static final String NAME = "name";
    public static final String GROUPS = "groups";
    public static final String FACES = "faces";
    public static final String IMAGE_PATH = "path";
    public static final String CHARACTER_NAME = "char_name";
    public static final String ICON = "icon";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String DESCRIPTION = "description";
    public static final String CREDITS = "credits";
}
