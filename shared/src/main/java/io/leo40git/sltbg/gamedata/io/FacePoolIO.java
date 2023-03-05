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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.gamedata.NamedFacePool;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import io.leo40git.sltbg.status.StatusTreeNode;
import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import io.leo40git.sltbg.util.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePoolIO {
    private FacePoolIO() {
        throw new UnsupportedOperationException("FacePoolIO only contains static declarations.");
    }

    @Contract("_, _ -> new")
    public static @NotNull NamedFacePool read(@NotNull JsonReader reader, boolean sort) throws IOException {
        String name = null;
        String[] description = ArrayUtils.EMPTY_STRING_ARRAY, credits = ArrayUtils.EMPTY_STRING_ARRAY;
        HashSet<String> categoryNames = null;
        ArrayList<FaceCategory> categories = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case FaceFields.NAME -> name = reader.nextString();
                case FaceFields.DESCRIPTION -> description = JsonReadUtils.readStringArray(reader);
                case FaceFields.CREDITS -> credits = JsonReadUtils.readStringArray(reader);
                case FaceFields.CATEGORIES -> {
                    if (categories == null) {
                        categories = new ArrayList<>();
                        categoryNames = new HashSet<>();
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String categoryName = reader.nextName();
                        if (!categoryNames.add(categoryName)) {
                            throw new MalformedJsonException(reader, "Category with name \"" + categoryName + "\" defined twice");
                        }
                        categories.add(FaceCategoryIO.read(reader, categoryName, false));
                    }
                    reader.endObject();
                }
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || categories == null) {
            var missingFields = new ArrayList<String>();
            if (name == null) {
                missingFields.add(FaceFields.NAME);
            }
            if (categories == null) {
                missingFields.add(FaceFields.CATEGORIES);
            }
            throw new MissingFieldsException(reader, "Face pool", missingFields);
        }

        var pool = new NamedFacePool(name);
        pool.setDescription(description);
        pool.setCredits(credits);
        for (var category : categories) {
            pool.add(category);
        }
        if (sort) {
            pool.sortIfNeeded();
        }
        return pool;
    }

    @Contract("_ -> new")
    public static @NotNull NamedFacePool read(@NotNull JsonReader reader) throws IOException {
        return read(reader, true);
    }

    public static void readImages(@NotNull NamedFacePool pool, @NotNull Path rootDir) throws FacePoolIOException {
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

    public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull NamedFacePool pool, @NotNull Path rootDir, @NotNull Executor executor) {
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

    public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull NamedFacePool pool, @NotNull Path rootDir, @NotNull Executor executor,
                                                                   @NotNull StatusTreeNode node) {
        var categories = pool.getCategories();
        if (categories.isEmpty()) {
            // nothing to do
            return CompletableFuture.completedFuture(null);
        }

        final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

        node.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

        var futures = new CompletableFuture[categories.size()];
        int futureI = 0;
        for (var category : categories.values()) {
            final var child = node.addChild(StatusTreeNodeIcon.OPERATION_PENDING, category.getName());
            futures[futureI] = FaceCategoryIO.readImagesAsync(category, rootDir, executor, child)
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
                        node.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
                        return CompletableFuture.completedStage(null);
                    } else {
                        var e = new FacePoolIOException(pool, "Failed to read all face images", exceptions);
                        e.fillInStackTrace();
                        node.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
                        return CompletableFuture.failedStage(e);
                    }
                });
    }

    public static void write(@NotNull NamedFacePool pool, @NotNull JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name(FaceFields.NAME);
        writer.value(pool.getName());

        if (pool.hasDescription()) {
            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, pool.getDescription());
        }

        if (pool.hasCredits()) {
            JsonWriteUtils.writeStringArray(writer, pool.getCredits());
        }

        writer.name(FaceFields.CATEGORIES);
        JsonWriteUtils.writeObject(writer, FaceCategoryIO::write, pool.getCategories().values());

        writer.endObject();
    }

    public static void writeImages(@NotNull NamedFacePool pool, @NotNull Path rootDir) throws FacePoolIOException {
        FacePoolIOException bigExc = null;

        for (var category : pool.getCategories().values()) {
            try {
                FaceCategoryIO.writeImages(category, rootDir);
            } catch (FaceCategoryIOException e) {
                if (bigExc == null) {
                    bigExc = new FacePoolIOException(pool, "Failed to write all face images");
                }
                bigExc.addSubException(e);
            }
        }

        if (bigExc != null) {
            throw bigExc;
        }
    }

    public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull NamedFacePool pool, @NotNull Path rootDir, @NotNull Executor executor) {
        var categories = pool.getCategories();
        if (categories.isEmpty()) {
            // nothing to do
            return CompletableFuture.completedFuture(null);
        }

        final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

        var futures = new CompletableFuture[categories.size()];
        int futureI = 0;
        for (var category : categories.values()) {
            futures[futureI] = FaceCategoryIO.writeImagesAsync(category, rootDir, executor)
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
                        var e = new FacePoolIOException(pool, "Failed to write all face images", exceptions);
                        e.fillInStackTrace();
                        return CompletableFuture.failedStage(e);
                    }
                });
    }

    public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull NamedFacePool pool, @NotNull Path rootDir, @NotNull Executor executor,
                                                                    @NotNull StatusTreeNode node) {
        var categories = pool.getCategories();
        if (categories.isEmpty()) {
            // nothing to do
            return CompletableFuture.completedFuture(null);
        }

        final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

        node.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

        var futures = new CompletableFuture[categories.size()];
        int futureI = 0;
        for (var category : categories.values()) {
            final var child = node.addChild(StatusTreeNodeIcon.OPERATION_PENDING, category.getName());
            futures[futureI] = FaceCategoryIO.writeImagesAsync(category, rootDir, executor, child)
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
                        node.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
                        return CompletableFuture.completedStage(null);
                    } else {
                        var e = new FacePoolIOException(pool, "Failed to write all face images", exceptions);
                        e.fillInStackTrace();
                        node.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
                        return CompletableFuture.failedStage(e);
                    }
                });
    }
}
