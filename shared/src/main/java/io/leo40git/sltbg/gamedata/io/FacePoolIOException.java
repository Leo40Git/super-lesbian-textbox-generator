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

import io.leo40git.sltbg.gamedata.FacePool;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class FacePoolIOException extends Exception {
	private final @NotNull FacePool pool;
	private final @NotNull List<FaceCategoryIOException> subExceptions;

	public FacePoolIOException(@NotNull FacePool pool, String message) {
		super(message);
		this.pool = pool;
		subExceptions = new ArrayList<>();
	}

	public FacePoolIOException(@NotNull FacePool pool, String message, @NotNull Collection<FaceCategoryIOException> subExceptions) {
		super(message);
		this.pool = pool;
		this.subExceptions = new ArrayList<>(subExceptions);
		for (var e : subExceptions) {
			addSuppressed(e);
		}
	}

	public void addSubException(@NotNull FaceCategoryIOException e) {
		subExceptions.add(e);
		addSuppressed(e);
	}

	public @NotNull FacePool getPool() {
		return pool;
	}

	@Contract(pure = true)
	public @NotNull @UnmodifiableView List<FaceCategoryIOException> getSubExceptions() {
		return Collections.unmodifiableList(subExceptions);
	}
}