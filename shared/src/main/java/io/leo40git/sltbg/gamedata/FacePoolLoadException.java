/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class FacePoolLoadException extends Exception {
	private final @NotNull ArrayList<FaceCategoryLoadException> subExceptions;

	public FacePoolLoadException() {
		super("Failed to load images for face pool");
		subExceptions = new ArrayList<>();
	}

	public void addSubException(@NotNull FaceCategoryLoadException e) {
		subExceptions.add(e);
		addSuppressed(e);
	}

	@Contract(pure = true)
	public @NotNull @UnmodifiableView List<FaceCategoryLoadException> getSubExceptions() {
		return Collections.unmodifiableList(subExceptions);
	}
}
