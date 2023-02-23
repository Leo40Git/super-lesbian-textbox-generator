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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class FacePool {
	public static final long DEFAULT_ORDER_BASE = 1000;

	public static long getNextOrder(long order) {
		order += DEFAULT_ORDER_BASE;
		long rem = order % DEFAULT_ORDER_BASE;
		if (rem > 0) {
			order += DEFAULT_ORDER_BASE - rem;
		}
		return order;
	}

	final @NotNull LinkedHashMap<String, FaceCategory> categories;
	private @Nullable FaceCategory lastCategory;
	private boolean needsSort;

	public FacePool() {
		categories = new LinkedHashMap<>();

		lastCategory = null;
		needsSort = false;
	}

	private static final ThreadLocal<ArrayList<FaceCategory>> TL_SORT_BUF = new ThreadLocal<>();

	void markDirty() {
		needsSort = true;
	}

	public void sortIfNeeded() {
		if (needsSort) {
			var sortBuf = TL_SORT_BUF.get();
			if (sortBuf == null) {
				TL_SORT_BUF.set(sortBuf = new ArrayList<>(categories.size()));
			} else {
				sortBuf.ensureCapacity(categories.size());
			}

			try {
				sortBuf.addAll(categories.values());
				sortBuf.sort(Comparator.naturalOrder());

				categories.clear();
				for (var category : sortBuf) {
					categories.put(category.getName(), category);
					category.sortIfNeeded();
				}
			} finally {
				sortBuf.clear();
			}

			needsSort = false;
		}
	}

	public @NotNull @UnmodifiableView Map<String, FaceCategory> getCategories() {
		sortIfNeeded();
		return Collections.unmodifiableMap(categories);
	}

	public @Nullable FaceCategory getCategory(@NotNull String name) {
		return categories.get(name);
	}

	public @Nullable Face getFace(@NotNull String path) {
		int delIdx = path.indexOf(Face.PATH_DELIMITER);
		if (delIdx < 0) {
			throw new IllegalArgumentException("Path \"%s\" is missing delimiter '%s'".formatted(path, Face.PATH_DELIMITER));
		}

		var category = getCategory(path.substring(0, delIdx));
		if (category == null) {
			return null;
		}

		return category.getFace(path.substring(delIdx + 1));
	}

	public void load(@NotNull Path rootDir) throws FacePoolLoadException {
		sortIfNeeded();

		FacePoolLoadException bigExc = null;

		for (var category : categories.values()) {
			try {
				category.load(rootDir);
			} catch (FaceCategoryLoadException e) {
				if (bigExc == null) {
					bigExc = new FacePoolLoadException();
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public CompletableFuture<Void> loadAsync(@NotNull Path rootDir, @NotNull Executor executor) {
		if (categories.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		sortIfNeeded();

		final var excs = new ConcurrentLinkedQueue<FaceCategoryLoadException>();

		var futures = new CompletableFuture[categories.size()];
		int i = 0;
		for (var category : categories.values()) {
			futures[i++] = category.loadAsync(rootDir, executor)
					.exceptionally(throwable -> {
						if (throwable instanceof FaceCategoryLoadException e) {
							excs.add(e);
							return null;
						} else {
							throw new RuntimeException("Unexpected exception (was expecting FaceCategoryLoadException)", throwable);
						}
					});
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (excs.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var bigExc = new FacePoolLoadException();
						for (var exc : excs) {
							bigExc.addSubException(exc);
						}
						bigExc.fillInStackTrace();
						return CompletableFuture.failedStage(bigExc);
					}
				});
	}

	public void add(@NotNull FaceCategory category) {
		if (category.getPool() != null) {
			throw new IllegalArgumentException("Category is already part of other pool");
		}

		if (categories.containsKey(category.getName())) {
			throw new IllegalArgumentException("Category with name \"" + category.getName() + "\" already exists in this pool");
		}

		categories.put(category.getName(), category);
		category.setPool(this);

		if (!category.isOrderSet() && lastCategory != null) {
			category.setOrder(getNextOrder(lastCategory.getOrder()));
		}
		lastCategory = category;

		needsSort = true;
	}

	void rename(@NotNull FaceCategory category, @NotNull String newName) {
		if (categories.containsKey(newName)) {
			throw new IllegalArgumentException("Category with name \"" + newName + "\" already exists in this pool");
		}

		categories.remove(category.getName(), category);
		categories.put(newName, category);
	}

	public @Nullable FaceCategory remove(@NotNull String category) {
		var catObj = categories.remove(category);
		if (catObj == null) {
			return null;
		}

		catObj.setPool(null);
		if (lastCategory == catObj) {
			for (var anCat : categories.values()) {
				lastCategory = anCat;
			}
		}
		needsSort = true;
		return catObj;
	}

	public void clear() {
		for (var category : categories.values()) {
			category.setPool(null);
		}

		categories.clear();
		lastCategory = null;
		needsSort = false;
	}
}
