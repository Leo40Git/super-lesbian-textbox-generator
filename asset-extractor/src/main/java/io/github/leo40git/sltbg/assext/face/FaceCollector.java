/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.assext.face;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

public final class FaceCollector {
    public static final int FACE_SIZE = 96;

    private final Path sheetPath;
    private final List<FaceListEntry> entries;

    public FaceCollector(@NotNull Path sheetPath, @NotNull List<FaceListEntry> entries) {
        this.sheetPath = sheetPath;
        this.entries = List.copyOf(entries);
    }

    public @NotNull Future<List<FacePoolWriter.Entry>> runAsync(@NotNull ExecutorCompletionService<List<FacePoolWriter.Entry>> completionService, @NotNull Path inputDir) {
        return completionService.submit(() -> run(inputDir));
    }

    public @NotNull List<FacePoolWriter.Entry> run(@NotNull Path inputDir) throws IOException {
        BufferedImage sheet;
        try (var is = Files.newInputStream(inputDir.resolve(sheetPath))) {
            sheet = ImageIO.read(is);
        }
        final int rowSize = sheet.getWidth() / FACE_SIZE;

        var output = new ArrayList<FacePoolWriter.Entry>();
        int index = 0;
        for (var entry : entries) {
            if (entry instanceof FaceListEntry.Add addEntry) {
                var image = sheet.getSubimage((index % rowSize) * FACE_SIZE, (index / rowSize) * FACE_SIZE, FACE_SIZE, FACE_SIZE);
                output.add(new FacePoolWriter.Entry(addEntry.category(), addEntry.name(), addEntry.path(), addEntry.order(), image));
            }

            index += entry.indexAdvance();
        }

        return output;
    }
}
