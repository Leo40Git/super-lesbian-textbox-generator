/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import io.leo40git.sltbg.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public final class NamedFacePool extends FacePool {
	private @NotNull String name;
	private String @NotNull [] description, credits;

	public NamedFacePool(@NotNull String name) {
		super();
		this.name = name;
		description = ArrayUtils.EMPTY_STRING_ARRAY;
		credits = ArrayUtils.EMPTY_STRING_ARRAY;
	}

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}

	public boolean hasDescription() {
		return description.length > 0;
	}

	public String @NotNull [] getDescription() {
		return ArrayUtils.clone(description);
	}

	public void setDescription(String @NotNull [] description) {
		this.description = ArrayUtils.clone(description);
	}

	public void clearDescription() {
		description = ArrayUtils.EMPTY_STRING_ARRAY;
	}

	public boolean hasCredits() {
		return credits.length > 0;
	}

	public String @NotNull [] getCredits() {
		return ArrayUtils.clone(credits);
	}

	public void setCredits(String @NotNull [] credits) {
		this.credits = ArrayUtils.clone(credits);
	}

	public void clearCredits() {
		credits = ArrayUtils.EMPTY_STRING_ARRAY;
	}

}
