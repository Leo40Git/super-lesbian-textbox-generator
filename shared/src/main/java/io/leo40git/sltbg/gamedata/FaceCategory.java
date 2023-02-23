/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class FaceCategory implements Comparable<FaceCategory> {
	private @Nullable FacePool pool;
	final @NotNull LinkedHashMap<String, Face> faces;
	private @NotNull String name;
	private boolean orderSet;
	private long order;
	private @Nullable String characterName;

	private @Nullable Face iconFace, lastFace;
	private boolean needsSort;

	private FaceCategory(@NotNull String name, @NotNull LinkedHashMap<String, Face> faces) {
		this.name = name;
		this.faces = faces;

		pool = null;
		orderSet = false;
		order = 0;
		characterName = null;

		iconFace = null;
		lastFace = null;
		needsSort = false;
	}

	public FaceCategory(@NotNull String name) {
		this(name, new LinkedHashMap<>());
	}

	public @Nullable FacePool getPool() {
		return pool;
	}

	void setPool(@NotNull FacePool pool) {
		this.pool = pool;
	}

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		if (!name.contains(Face.PATH_DELIMITER)) {
			throw new IllegalArgumentException("Name \"%s\" contains path delimiter '%s'".formatted(name, Face.PATH_DELIMITER));
		}

		if (!this.name.equals(name)) {
			if (pool != null) {
				pool.rename(this, name);
			}
			this.name = name;
			for (var face : faces.values()) {
				face.setCategory(this);
			}
		}
	}

	public boolean isOrderSet() {
		return orderSet;
	}

	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		if (!orderSet || this.order != order) {
			this.order = order;
			orderSet = true;
			if (pool != null) {
				pool.markDirty();
			}
		}
	}

	public @Nullable String getCharacterName() {
		return characterName;
	}

	public void setCharacterName(@Nullable String characterName) {
		this.characterName = characterName;
	}

	public @Nullable Face getIconFace() {
		return iconFace;
	}

	private static final ThreadLocal<ArrayList<Face>> TL_SORT_BUF = new ThreadLocal<>();

	void markDirty() {
		needsSort = true;
	}

	public void sortIfNeeded() {
		if (needsSort) {
			var sortBuf = TL_SORT_BUF.get();
			if (sortBuf == null) {
				TL_SORT_BUF.set(sortBuf = new ArrayList<>(faces.size()));
			} else {
				sortBuf.ensureCapacity(faces.size());
			}

			try {
				sortBuf.addAll(faces.values());
				sortBuf.sort(Comparator.naturalOrder());

				faces.clear();
				for (var face : sortBuf) {
					faces.put(face.getName(), face);
				}
			} finally {
				sortBuf.clear();
			}

			needsSort = false;
		}
	}

	public @NotNull @UnmodifiableView Map<String, Face> getFaces() {
		sortIfNeeded();
		return Collections.unmodifiableMap(faces);
	}

	public @Nullable Face getFace(@NotNull String name) {
		return faces.get(name);
	}

	public void load(@NotNull Path rootDir) throws FaceCategoryLoadException {
		sortIfNeeded();

		FaceCategoryLoadException bigExc = null;

		for (var face : faces.values()) {
			try {
				face.load(rootDir);
			} catch (FaceLoadException e) {
				if (bigExc == null) {
					bigExc = new FaceCategoryLoadException(this);
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public @NotNull CompletableFuture<Void> loadAsync(@NotNull Path rootDir, @NotNull Executor executor) {
		if (faces.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		sortIfNeeded();

		final var excs = new ConcurrentLinkedQueue<FaceLoadException>();

		var futures = new CompletableFuture[faces.size()];
		int i = 0;
		for (var face : faces.values()) {
			futures[i++] = CompletableFuture.runAsync(() -> {
				try {
					face.load(rootDir);
				} catch (FaceLoadException e) {
					excs.add(e);
				}
			}, executor);
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (excs.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var bigExc = new FaceCategoryLoadException(this);
						for (var exc : excs) {
							bigExc.addSubException(exc);
						}
						bigExc.fillInStackTrace();
						return CompletableFuture.failedStage(bigExc);
					}
				});
	}

	public void add(@NotNull Face face) {
		if (face.getCategory() != null) {
			throw new IllegalArgumentException("Face is already part of other category: \"" + face.getCategory().getName() + "\"");
		}

		if (faces.containsKey(face.getName())) {
			throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
		}

		faces.put(face.getName(), face);
		face.setCategory(this);

		if (iconFace == null) {
			iconFace = face;
		}

		if (!face.isOrderSet() && lastFace != null) {
			face.setOrder(FacePool.getNextOrder(lastFace.getOrder()));
		}
		lastFace = face;

		needsSort = true;
	}

	void rename(@NotNull Face face, @NotNull String newName) {
		if (faces.containsKey(newName)) {
			throw new IllegalArgumentException("Face with name \"" + newName + "\" already exists in this category");
		}

		faces.remove(face.getName(), face);
		faces.put(newName, face);
	}

	@Contract(" -> new")
	public @NotNull FaceCategory copy() {
		var clone = new FaceCategory(name, new LinkedHashMap<>(faces.size()));

		for (var faces : faces.values()) {
			clone.add(faces.copy());
		}

		clone.order = order;
		clone.orderSet = orderSet;
		clone.characterName = characterName;
		return clone;
	}

	@Override
	public int compareTo(@NotNull FaceCategory o) {
		return Long.compare(order, o.order);
	}
}
