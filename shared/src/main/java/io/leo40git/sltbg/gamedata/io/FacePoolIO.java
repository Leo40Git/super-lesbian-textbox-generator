/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import io.leo40git.sltbg.gamedata.FacePool;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePoolIO {
	private FacePoolIO() {
		throw new UnsupportedOperationException("FacePoolIO only contains static declarations.");
	}

	@Contract("_ -> new")
	public static @NotNull FacePool read(@NotNull JsonReader reader) throws IOException {
		var pool = new FacePool();
		JsonReadUtils.readSimpleMap(reader, FaceCategoryIO::read, pool::add);
		return pool;
	}

	public static void readImages(@NotNull FacePool pool, @NotNull Path rootDir) throws FacePoolIOException {
		FacePoolIOException bigExc = null;

		for (var category : pool.getCategories().values()) {
			try {
				FaceCategoryIO.readImages(category, rootDir);
			} catch (FaceCategoryIOException e) {
				if (bigExc == null) {
					bigExc = new FacePoolIOException(pool, "Failed to read all face images");
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull FacePool pool, @NotNull Path rootDir, @NotNull Executor executor) {
		var categories = pool.getCategories();
		if (categories.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

		var futures = new CompletableFuture[categories.size()];
		int futureI = 0;

		for (var category : categories.values()) {
			futures[futureI] = FaceCategoryIO.readImagesAsync(category, rootDir, executor)
					.exceptionallyCompose(ex -> {
						if (ex instanceof FaceCategoryIOException fcioe) {
							exceptions.add(fcioe);
							return CompletableFuture.completedStage(null);
						} else {
							return CompletableFuture.failedStage(ex);
						}
					});
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FacePoolIOException(pool, "Failed to read all face images", exceptions);
						e.fillInStackTrace();
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static void write(@NotNull JsonWriter writer, @NotNull FacePool pool) throws IOException {
		pool.sortIfNeeded();
		JsonWriteUtils.writeObject(writer, FaceCategoryIO::write, pool.getCategories().values());
	}

	public static void writeImages(@NotNull FacePool pool, @NotNull Path rootDir) throws IOException {
		pool.sortIfNeeded();
		for (var category : pool.getCategories().values()) {
			FaceCategoryIO.writeImages(category, rootDir);
		}
	}
}
