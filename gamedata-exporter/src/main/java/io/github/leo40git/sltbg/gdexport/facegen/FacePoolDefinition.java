/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("ClassCanBeRecord")
public final class FacePoolDefinition {
    public final @NotNull String name;
    public final @NotNull Map<String, FaceCategoryDefinition> categories;
    public final @NotNull @Unmodifiable List<String> description, credits;

    public FacePoolDefinition(@NotNull String name,
                              @NotNull Map<String, FaceCategoryDefinition> categories,
                              @Nullable List<String> description, @Nullable List<String> credits) {
        this.name = name;
        this.categories = categories;
        this.description = description != null ? description : List.of();
        this.credits = credits != null ? credits : List.of();
    }
}
