/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import javax.imageio.ImageTypeSpecifier;

import io.leo40git.sltbg.gamedata.Face;
import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.gamedata.FacePool;
import io.leo40git.sltbg.gamedata.NamedFacePool;
import io.leo40git.sltbg.gamedata.io.FacePoolIO;
import io.leo40git.sltbg.util.CollectionUtils;
import io.leo40git.sltbg.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import org.quiltmc.json5.JsonWriter;

@SuppressWarnings("ClassCanBeRecord")
public final class FacePoolDefinition {
    private final @NotNull String name;
    private final @NotNull @Unmodifiable Map<String, FaceCategoryDefinition> categories;
    private final @NotNull @Unmodifiable List<FaceSheet> sheets;
    private final @NotNull @Unmodifiable List<String> description, credits;

    public FacePoolDefinition(@NotNull String name,
                              @NotNull Map<String, FaceCategoryDefinition> categories, @NotNull List<FaceSheet> sheets,
                              @Nullable List<String> description, @Nullable List<String> credits) {
        this.name = name;
        this.categories = CollectionUtils.copyOf(categories);
        this.sheets = CollectionUtils.copyOf(sheets);
        this.description = CollectionUtils.copyOrEmpty(description);
        this.credits = CollectionUtils.copyOrEmpty(credits);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull @Unmodifiable Map<String, FaceCategoryDefinition> getCategories() {
        return categories;
    }

    public @NotNull @Unmodifiable List<FaceSheet> getSheets() {
        return sheets;
    }

    public @NotNull @Unmodifiable List<String> getDescription() {
        return description;
    }

    public @NotNull @Unmodifiable List<String> getCredits() {
        return credits;
    }

    private void append(@NotNull NamedFacePool pool, @NotNull List<Pair<String, Face>> pairs) {
        for (var pair : pairs) {
            var category = pool.getCategory(pair.left());
            if (category == null) {
                var entry = categories.get(pair.left());
                assert entry != null : "Undefined category \"" + pair.left() + "\" referenced (should've been caught at parse time!)";
                category = new FaceCategory(entry.getName());
                category.setOrder(entry.getOrder());
                if (entry.getCharacterName() != null) {
                    category.setCharacterName(entry.getCharacterName());
                }
                pool.add(category);
            }
            category.add(pair.right());
        }
    }

    public @NotNull NamedFacePool build(@NotNull Path inputDir) throws IOException {
        var pool = new NamedFacePool(name);
        for (var sheet : sheets) {
            append(pool, sheet.split(inputDir, null));
        }
        return pool;
    }

    public @NotNull CompletableFuture<NamedFacePool> buildAsync(@NotNull Executor executor, @NotNull Path inputDir) {
        if (sheets.isEmpty()) {
            // nothing to do
            return CompletableFuture.completedFuture(new NamedFacePool(name));
        }

        final var exceptions = new ConcurrentLinkedQueue<IOException>();

        var futures = new ArrayList<CompletableFuture<List<Pair<String, Face>>>>(sheets.size());
        for (var sheet : sheets) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return sheet.split(inputDir, null);
                } catch (IOException e) {
                    exceptions.add(e);
                    return List.of();
                }
            }, executor));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenCompose(unused -> {
                    if (exceptions.isEmpty()) {
                        var pool = new NamedFacePool(name);
                        for (var future : futures) {
                            append(pool, future.join());
                        }
                        return CompletableFuture.completedStage(pool);
                    } else {
                        var e = new IOException("Failed to build face pool");
                        for (var suppressed : exceptions) {
                            e.addSuppressed(suppressed);
                        }
                        e.fillInStackTrace();
                        return CompletableFuture.failedStage(e);
                    }
                });
    }

    public void write(@NotNull Path inputDir, @NotNull Path outputDir, @NotNull String fileName) throws IOException {
        var imageFlusher = new AsyncFaceImageFlusher(outputDir, ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB));
        imageFlusher.start();
        try {
            var pool = new NamedFacePool(name);
            for (var sheet : sheets) {
                append(pool, sheet.split(inputDir, imageFlusher));
            }
            imageFlusher.finish();
            if (!imageFlusher.getExceptions().isEmpty()) {
                throw new IOException("AsyncFaceImageFlusher reported exceptions");
            }
            try (var writer = JsonWriter.json5(outputDir.resolve(fileName))) {
                FacePoolIO.write(pool, writer);
            }
        } catch (IOException e) {
            imageFlusher.abort();
            for (var exc : imageFlusher.getExceptions()) {
                e.addSuppressed(exc);
            }
            throw e;
        }
    }

    public @NotNull CompletableFuture<Void> writeAsync(@NotNull Executor executor,
                                                       @NotNull Path inputDir, @NotNull Path outputDir, @NotNull String fileName) {
        if (sheets.isEmpty()) {
            // nothing to do
            return CompletableFuture.completedFuture(null);
        }

        final var imageFlusher = new AsyncFaceImageFlusher(outputDir, ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB));
        imageFlusher.start();

        final var exceptions = new ConcurrentLinkedQueue<IOException>();

        var futures = new ArrayList<CompletableFuture<List<Pair<String, Face>>>>(sheets.size());
        for (var sheet : sheets) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return sheet.split(inputDir, imageFlusher);
                } catch (IOException e) {
                    exceptions.add(e);
                    return List.of();
                }
            }, executor));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenCompose(unused -> {
                    if (exceptions.isEmpty()) {
                        var pool = new NamedFacePool(name);
                        for (var future : futures) {
                            append(pool, future.join());
                        }
                        try {
                            if (!imageFlusher.getExceptions().isEmpty()) {
                                throw new IOException("AsyncFaceImageFlusher reported exceptions");
                            }
                            try (var writer = JsonWriter.json5(outputDir.resolve(fileName))) {
                                FacePoolIO.write(pool, writer);
                            }
                            return CompletableFuture.completedStage(null);
                        } catch (IOException e) {
                            imageFlusher.abort();
                            for (var exc : imageFlusher.getExceptions()) {
                                e.addSuppressed(exc);
                            }
                            return CompletableFuture.failedStage(e);
                        }
                    } else {
                        var e = new IOException("Failed to build face pool");
                        for (var suppressed : exceptions) {
                            e.addSuppressed(suppressed);
                        }
                        e.fillInStackTrace();
                        return CompletableFuture.failedStage(e);
                    }
                });
    }
}
