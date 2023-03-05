/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.leo40git.sltbg.gamedata.FaceCategory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class FaceCategoryIOException extends Exception {
    private final @NotNull FaceCategory category;
    private final @NotNull List<FaceIOException> subExceptions;

    public FaceCategoryIOException(@NotNull FaceCategory category, String message) {
        super(message);
        this.category = category;
        subExceptions = new ArrayList<>();
    }

    public FaceCategoryIOException(@NotNull FaceCategory category, String message, @NotNull Collection<FaceIOException> subExceptions) {
        super(message);
        this.category = category;
        this.subExceptions = new ArrayList<>(subExceptions);
        for (var e : subExceptions) {
            addSuppressed(e);
        }
    }

    public void addSubException(@NotNull FaceIOException e) {
        subExceptions.add(e);
        addSuppressed(e);
    }

    public @NotNull FaceCategory getCategory() {
        return category;
    }

    @Contract(pure = true)
    public @NotNull @UnmodifiableView List<FaceIOException> getSubExceptions() {
        return Collections.unmodifiableList(subExceptions);
    }
}
