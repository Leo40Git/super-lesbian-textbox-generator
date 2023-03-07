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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("ClassCanBeRecord")
public final class FaceDefinition {
    public final @NotNull String imagePath, category, name;
    public final long order;
    public final @Nullable String characterName;
    public final @NotNull @Unmodifiable List<String> description;

    public FaceDefinition(@NotNull String imagePath, @NotNull String category, @NotNull String name,
                          long order, @Nullable String characterName,
                          @NotNull List<String> description) {
        this.imagePath = imagePath;
        this.category = category;
        this.name = name;
        this.order = order;
        this.characterName = characterName;
        this.description = description;
    }
}
