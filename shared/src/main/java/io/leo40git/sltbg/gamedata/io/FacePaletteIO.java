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
import io.leo40git.sltbg.gamedata.NamedFacePalette;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import io.leo40git.sltbg.util.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePaletteIO {
    private FacePaletteIO() {
        throw new UnsupportedOperationException("FacePaletteIO only contains static declarations.");
    }

    @Contract("_, _ -> new")
    public static @NotNull NamedFacePalette read(@NotNull JsonReader reader, boolean sort) throws IOException {
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
            throw new MissingFieldsException(reader, "Face palette", missingFields);
        }

        var palette = new NamedFacePalette(name);
        palette.setDescription(description);
        palette.setCredits(credits);
        for (var category : categories) {
            palette.add(category);
        }
        if (sort) {
            palette.sortIfNeeded();
        }
        return palette;
    }

    @Contract("_ -> new")
    public static @NotNull NamedFacePalette read(@NotNull JsonReader reader) throws IOException {
        return read(reader, true);
    }

    public static void readImages(@NotNull NamedFacePalette palette, @NotNull Path rootDir,
                                  @Nullable FaceImageReadObserver observer) throws FacePaletteIOException {
        if (observer != null) {
            observer.preReadFacePaletteImages(palette);
        }

        FacePaletteIOException bigExc = null;

        for (var category : palette.getCategories()) {
            try {
                FaceCategoryIO.readImages(category, rootDir, observer);
            } catch (FaceCategoryIOException e) {
                if (bigExc == null) {
                    bigExc = new FacePaletteIOException(palette, "Failed to read all face images");
                }
                bigExc.addSubException(e);
            }
        }

        if (observer != null) {
            observer.postReadFacePaletteImages(palette, bigExc);
        }

        if (bigExc != null) {
            throw bigExc;
        }
    }

    public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull Executor executor,
                                                                   @NotNull NamedFacePalette palette, @NotNull Path rootDir,
                                                                   @Nullable FaceImageReadObserver observer) {
        if (observer != null) {
            observer.preReadFacePaletteImages(palette);
        }

        var categories = palette.getCategories();
        if (categories.isEmpty()) {
            // nothing to do
            if (observer != null) {
                observer.postReadFacePaletteImages(palette, null);
            }
            return CompletableFuture.completedFuture(null);
        }

        final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

        var futures = new CompletableFuture[categories.size()];
        int futureI = 0;
        for (var category : categories) {
            futures[futureI] = FaceCategoryIO.readImagesAsync(executor, category, rootDir, observer)
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
                        if (observer != null) {
                            observer.postReadFacePaletteImages(palette, null);
                        }
                        return CompletableFuture.completedStage(null);
                    } else {
                        var exc = new FacePaletteIOException(palette, "Failed to read all face images", exceptions);
                        if (observer != null) {
                            observer.postReadFacePaletteImages(palette, exc);
                        }
                        return CompletableFuture.failedStage(exc);
                    }
                });
    }

    public static void write(@NotNull NamedFacePalette palette, @NotNull JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name(FaceFields.NAME);
        writer.value(palette.getName());

        if (palette.hasDescription()) {
            writer.name(FaceFields.DESCRIPTION);
            JsonWriteUtils.writeStringArray(writer, palette.getDescription());
        }

        if (palette.hasCredits()) {
            JsonWriteUtils.writeStringArray(writer, palette.getCredits());
        }

        writer.name(FaceFields.CATEGORIES);
        JsonWriteUtils.writeObject(writer, FaceCategoryIO::write, palette.getCategories());

        writer.endObject();
    }

    public static void writeImages(@NotNull NamedFacePalette palette, @NotNull Path rootDir,
                                   @Nullable FaceImageWriteObserver observer) throws FacePaletteIOException {
        if (observer != null) {
            observer.preWriteFacePaletteImages(palette);
        }

        FacePaletteIOException bigExc = null;

        for (var category : palette.getCategories()) {
            try {
                FaceCategoryIO.writeImages(category, rootDir, observer);
            } catch (FaceCategoryIOException e) {
                if (bigExc == null) {
                    bigExc = new FacePaletteIOException(palette, "Failed to write all face images");
                }
                bigExc.addSubException(e);
            }
        }

        if (observer != null) {
            observer.postWriteFacePaletteImages(palette, bigExc);
        }

        if (bigExc != null) {
            throw bigExc;
        }
    }

    public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull Executor executor,
                                                                    @NotNull NamedFacePalette palette, @NotNull Path rootDir,
                                                                    @Nullable FaceImageWriteObserver observer) {
        if (observer != null) {
            observer.preWriteFacePaletteImages(palette);
        }

        var categories = palette.getCategories();
        if (categories.isEmpty()) {
            // nothing to do
            if (observer != null) {
                observer.postWriteFacePaletteImages(palette, null);
            }
            return CompletableFuture.completedFuture(null);
        }

        final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

        var futures = new CompletableFuture[categories.size()];
        int futureI = 0;
        for (var category : categories) {
            futures[futureI] = FaceCategoryIO.writeImagesAsync(executor, category, rootDir, observer)
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
                        if (observer != null) {
                            observer.postWriteFacePaletteImages(palette, null);
                        }
                        return CompletableFuture.completedStage(null);
                    } else {
                        var exc = new FacePaletteIOException(palette, "Failed to write all face images", exceptions);
                        if (observer != null) {
                            observer.postWriteFacePaletteImages(palette, exc);
                        }
                        return CompletableFuture.failedStage(exc);
                    }
                });
    }
}
