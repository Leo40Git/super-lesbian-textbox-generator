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
		for (var face : faces) {
			var path = dir.resolve(face.path());
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

					var tags = face.tags();
					if (tags.length == 0) {
						writer.value(face.path());
					} else {
						writer.beginObject();

						writer.name("path");
						writer.value(face.path());

						writer.name("tags");
						if (tags.length == 1) {
							writer.value(tags[0]);
						} else {
							writer.beginArray();
							for (String tag : tags) {
								writer.value(tag);
							}
							writer.endArray();
						}

						writer.endObject();
					}
				}

				writer.endObject();
			}

			writer.endObject();
		}
	}

	public record Entry(@NotNull String category, @NotNull String name, @NotNull String path, int order, @NotNull String @NotNull [] tags,
						@NotNull BufferedImage image) { }
}
