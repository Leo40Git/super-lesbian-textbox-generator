/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.io.IOException;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

public final class FaceLoadException extends Exception {
	private final @NotNull Face face;
	private final @NotNull Path imagePath;

	public FaceLoadException(@NotNull Face face, @NotNull Path imagePath, @NotNull IOException cause) {
		super("Failed to load image \"%s\" for face \"%s\"".formatted(imagePath, face), cause);
		this.face = face;
		this.imagePath = imagePath;
	}

	@Override
	public synchronized IOException getCause() {
		return (IOException) super.getCause();
	}

	public @NotNull Face getFace() {
		return face;
	}

	public @NotNull Path getImagePath() {
		return imagePath;
	}
}
