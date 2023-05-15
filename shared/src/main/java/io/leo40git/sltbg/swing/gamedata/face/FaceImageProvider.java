/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.face;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

import javax.swing.Icon;

import io.leo40git.sltbg.gamedata.face.Face;
import io.leo40git.sltbg.gamedata.face.FaceGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FaceImageProvider {
    @NotNull CompletableFuture<BufferedImage> getFaceImage(@NotNull Face face);

    @NotNull Icon getFaceIcon(@NotNull Face face);

    default @NotNull Icon getFaceGroupIcon(@NotNull FaceGroup group) {
        var iconFace = group.getIconFace();
        if (iconFace != null) {
            return getFaceIcon(iconFace);
        } else {
            throw new IllegalArgumentException("category has no icon face");
        }
    }

    default void paintFaceIcon(@NotNull Face face, @Nullable Component c, @NotNull Graphics g, int x, int y) {
        getFaceIcon(face).paintIcon(c, g, x, y);
    }

    default void paintFaceGroupIcon(@NotNull FaceGroup group, @Nullable Component c, @NotNull Graphics g, int x, int y) {
        getFaceGroupIcon(group).paintIcon(c, g, x, y);
    }

    default void invalidateAll() { }
}
