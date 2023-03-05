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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonWriter;

public final class FacePoolWriter {
    private final List<Entry> faces;
    private final Map<String, Set<Entry>> byCategory;

    public FacePoolWriter() {
        faces = new ArrayList<>();
        byCategory = new LinkedHashMap<>();
    }

    public void add(@NotNull FacePoolWriter.Entry face) {
        faces.add(face);
        byCategory.computeIfAbsent(face.category(), ignored -> new TreeSet<>(Comparator.comparing(Entry::order))).add(face);
    }

    public void write(@NotNull Path dir) throws IOException {
        var facesRoot = dir.resolve("faces");
        for (var face : faces) {
            var path = facesRoot.resolve(face.path());
            Files.createDirectories(path.getParent());
            try (var os = Files.newOutputStream(path)) {
                ImageIO.write(face.image(), "PNG", os);
            }
        }

        try (var writer = JsonWriter.json5(dir.resolve("faces.json5"))) {
            writer.beginObject();

            for (var entry : byCategory.entrySet()) {
                writer.name(entry.getKey());

                writer.beginObject();

                for (var face : entry.getValue()) {
                    writer.name(face.name());
                    writer.value("faces/" + face.path());
                }

                writer.endObject();
            }

            writer.endObject();
        }
    }

    public record Entry(@NotNull String category, @NotNull String name, @NotNull String path, int order,
                        @NotNull BufferedImage image) {
    }
}
