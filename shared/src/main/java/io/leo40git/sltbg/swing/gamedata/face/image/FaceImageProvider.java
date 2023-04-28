/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.face.image;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

import javax.swing.Icon;

import io.leo40git.sltbg.gamedata.face.Face;
import io.leo40git.sltbg.gamedata.face.FaceCategory;
import org.jetbrains.annotations.NotNull;

public interface FaceImageProvider {
    @NotNull CompletableFuture<BufferedImage> getFaceImage(@NotNull Face face);

    @NotNull Icon getFaceIcon(@NotNull Face face);

    @NotNull Icon getFaceCategoryIcon(@NotNull FaceCategory category);

    default void invalidate() { }
}
