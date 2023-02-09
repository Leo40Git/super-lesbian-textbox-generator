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
	public static final int ROW_SIZE = 4;

	private final Path sheetPath;
	private final List<FaceListEntry> entries;

	public FaceCollector(@NotNull Path sheetPath, @NotNull List<FaceListEntry> entries) {
		this.sheetPath = sheetPath;
		this.entries = List.copyOf(entries);
	}

	public @NotNull Future<List<FacePoolWriter.Entry>> run(@NotNull ExecutorCompletionService<List<FacePoolWriter.Entry>> completionService, @NotNull Path inputDir) {
		return completionService.submit(() -> run0(inputDir));
	}

	private @NotNull List<FacePoolWriter.Entry> run0(@NotNull Path inputDir) throws IOException {
		BufferedImage sheet;
		try (var is = Files.newInputStream(inputDir.resolve(sheetPath))) {
			sheet = ImageIO.read(is);
		}

		var output = new ArrayList<FacePoolWriter.Entry>();
		int index = 0;
		for (var entry : entries) {
			if (entry instanceof FaceListEntry.Add addEntry) {
				var image = sheet.getSubimage((index % ROW_SIZE) * FACE_SIZE, (index / ROW_SIZE) * FACE_SIZE, FACE_SIZE, FACE_SIZE);
				output.add(new FacePoolWriter.Entry(addEntry.category(), addEntry.name(), addEntry.path(), addEntry.tags(), image));
			}

			index += entry.indexAdvance();
		}

		return output;
	}
}
