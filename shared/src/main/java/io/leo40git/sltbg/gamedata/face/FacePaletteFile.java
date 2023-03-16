/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FacePaletteFile extends FacePalette {
    private @NotNull String name;
    private @NotNull Path rootDirectory;
    private @Nullable ArrayList<String> description = null, credits = null;

    public FacePaletteFile(@NotNull String name, @NotNull Path rootDirectory) {
        super();
        this.name = name;
        this.rootDirectory = rootDirectory;
    }

    public FacePaletteFile(@NotNull String name, @NotNull Path rootDirectory, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
        this.rootDirectory = rootDirectory;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull Path getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(@NotNull Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    public synchronized @NotNull List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
        }
        return description;
    }

    public void clearDescription() {
        description = null;
    }

    public boolean hasCredits() {
        return credits != null && !credits.isEmpty();
    }

    public synchronized @NotNull List<String> getCredits() {
        if (credits == null) {
            credits = new ArrayList<>();
        }
        return credits;
    }

    public void clearCredits() {
        credits = null;
    }
}
