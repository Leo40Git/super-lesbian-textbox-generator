package io.leo40git.sltbg.gamedata.io;

import io.leo40git.sltbg.gamedata.Face;
import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.gamedata.NamedFacePool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FaceImageWriteObserver {
    default void preWriteFacePoolImages(@NotNull NamedFacePool pool) {}
    default void preWriteFaceCategoryImages(@NotNull FaceCategory category) {}
    default void preWriteFaceImage(@NotNull Face face) {}
    default void postWriteFaceImage(@NotNull Face face, @Nullable FaceIOException exc) {}
    default void postWriteFaceCategoryImages(@NotNull FaceCategory category, @Nullable FaceCategoryIOException exc) {}
    default void postWriteFacePoolImages(@NotNull NamedFacePool pool, @Nullable FacePoolIOException exc) {}
}
