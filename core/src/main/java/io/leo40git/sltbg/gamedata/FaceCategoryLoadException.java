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

public final class FaceCategoryLoadException extends Exception {
	private final @NotNull FaceCategory category;
	private final @NotNull ArrayList<FaceLoadException> subExceptions;

	public FaceCategoryLoadException(@NotNull FaceCategory category) {
		super("Failed to load face images for category \"" + category.getName() + "\"");
		this.category = category;
		subExceptions = new ArrayList<>();
	}

	public void addSubException(@NotNull FaceLoadException e) {
		subExceptions.add(e);
		addSuppressed(e);
	}

	public @NotNull FaceCategory getCategory() {
		return category;
	}

	@Contract(pure = true)
	public @NotNull @UnmodifiableView List<FaceLoadException> getSubExceptions() {
		return Collections.unmodifiableList(subExceptions);
	}
}
