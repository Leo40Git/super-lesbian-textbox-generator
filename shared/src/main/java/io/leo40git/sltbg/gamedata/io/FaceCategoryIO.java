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

import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MissingFieldsException;
import io.leo40git.sltbg.status.StatusTreeNode;
import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FaceCategoryIO {
	private FaceCategoryIO() {
		throw new UnsupportedOperationException("FaceCategoryIO only contains static declarations.");
	}

	@Contract("_, _, _ -> new")
	public static @NotNull FaceCategory read(@NotNull JsonReader reader, @NotNull String name, boolean sort) throws IOException {
		var category = new FaceCategory(name);

		boolean gotFaces = false;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case FaceFields.FACES -> {
					JsonReadUtils.readSimpleMap(reader, FaceIO::read, category::add);
					gotFaces = true;
				}
				case FaceFields.ORDER -> category.setOrder(reader.nextLong());
				case FaceFields.CHARACTER_NAME -> category.setCharacterName(reader.nextString());
				default -> reader.skipValue();
			}
		}
		reader.endObject();

		if (!gotFaces) {
			throw new MissingFieldsException("Category", FaceFields.FACES);
		}

		if (sort) {
			category.sortIfNeeded();
		}
		return category;
	}

	@Contract("_, _ -> new")
	public static @NotNull FaceCategory read(@NotNull JsonReader reader, @NotNull String name) throws IOException {
		return read(reader, name, true);
	}

	public static void readImages(@NotNull FaceCategory category, @NotNull Path rootDir) throws FaceCategoryIOException {
		FaceCategoryIOException bigExc = null;

		for (var face : category.getFaces().values()) {
			try {
				FaceIO.readImage(face, rootDir);
			} catch (FaceIOException e) {
				if (bigExc == null) {
					bigExc = new FaceCategoryIOException(category, "Failed to read all face images");
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull FaceCategory category, @NotNull Path rootDir, @NotNull Executor executor) {
		var faces = category.getFaces();
		if (faces.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceIOException>();

		var futures = new CompletableFuture[faces.size()];
		int futureI = 0;
		for (var face : faces.values()) {
			futures[futureI] = CompletableFuture.runAsync(() -> {
				try {
					FaceIO.readImage(face, rootDir);
				} catch (FaceIOException e) {
					exceptions.add(e);
				}
			}, executor);
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FaceCategoryIOException(category, "Failed to read all face images", exceptions);
						e.fillInStackTrace();
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull FaceCategory category, @NotNull Path rootDir, @NotNull Executor executor,
			@NotNull StatusTreeNode node) {
		var faces = category.getFaces();
		if (faces.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceIOException>();

		node.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

		var futures = new CompletableFuture[faces.size()];
		int futureI = 0;
		for (var face : faces.values()) {
			final var child = node.addChild(StatusTreeNodeIcon.OPERATION_PENDING, face.getName());
			futures[futureI] = CompletableFuture.runAsync(() -> {
				child.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);
				try {
					FaceIO.readImage(face, rootDir);
					child.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
				} catch (FaceIOException e) {
					exceptions.add(e);
					child.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
					child.addException(e);
				}
			}, executor);
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						node.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FaceCategoryIOException(category, "Failed to read all face images", exceptions);
						e.fillInStackTrace();
						node.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static void write(@NotNull FaceCategory category, @NotNull JsonWriter writer) throws IOException {
		category.sortIfNeeded();

		writer.name(category.getName());

		writer.beginObject();

		if (category.isOrderSet()) {
			writer.name(FaceFields.ORDER);
			writer.value(category.getOrder());
		}

		if (category.getCharacterName() != null) {
			writer.name(FaceFields.CHARACTER_NAME);
			writer.value(category.getCharacterName());
		}

		writer.name(FaceFields.FACES);
		JsonWriteUtils.writeObject(writer, FaceIO::write, category.getFaces().values());

		writer.endObject();
	}

	public static void writeImages(@NotNull FaceCategory category, @NotNull Path rootDir) throws FaceCategoryIOException {
		FaceCategoryIOException bigExc = null;

		for (var face : category.getFaces().values()) {
			try {
				FaceIO.writeImage(face, rootDir);
			} catch (FaceIOException e) {
				if (bigExc == null) {
					bigExc = new FaceCategoryIOException(category, "Failed to write all face images");
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull FaceCategory category, @NotNull Path rootDir, @NotNull Executor executor) {
		var faces = category.getFaces();
		if (faces.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceIOException>();

		var futures = new CompletableFuture[faces.size()];
		int futureI = 0;
		for (var face : faces.values()) {
			futures[futureI] = CompletableFuture.runAsync(() -> {
				try {
					FaceIO.writeImage(face, rootDir);
				} catch (FaceIOException e) {
					exceptions.add(e);
				}
			}, executor);
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FaceCategoryIOException(category, "Failed to write all face images", exceptions);
						e.fillInStackTrace();
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull FaceCategory category, @NotNull Path rootDir, @NotNull Executor executor,
			@NotNull StatusTreeNode node) {
		var faces = category.getFaces();
		if (faces.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceIOException>();

		node.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

		var futures = new CompletableFuture[faces.size()];
		int futureI = 0;
		for (var face : faces.values()) {
			final var child = node.addChild(StatusTreeNodeIcon.OPERATION_PENDING, face.getName());
			futures[futureI] = CompletableFuture.runAsync(() -> {
				child.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);
				try {
					FaceIO.writeImage(face, rootDir);
					child.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
				} catch (FaceIOException e) {
					exceptions.add(e);
					child.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
					child.addException(e);
				}
			}, executor);
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						node.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FaceCategoryIOException(category, "Failed to write all face images", exceptions);
						e.fillInStackTrace();
						node.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
						return CompletableFuture.failedStage(e);
					}
				});
	}
}
