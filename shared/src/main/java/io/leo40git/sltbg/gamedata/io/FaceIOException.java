/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import io.leo40git.sltbg.gamedata.Face;
import org.jetbrains.annotations.NotNull;

public final class FaceIOException extends Exception {
	private final @NotNull Face face;

	public FaceIOException(@NotNull Face face, String message) {
		super(message);
		this.face = face;
	}

	public FaceIOException(@NotNull Face face, String message, Throwable cause) {
		super(message, cause);
		this.face = face;
	}

	public @NotNull Face getFace() {
		return face;
	}
}
