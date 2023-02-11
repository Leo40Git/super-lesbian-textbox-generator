/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.assext;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import io.github.leo40git.sltbg.assext.face.FaceCollector;
import io.github.leo40git.sltbg.assext.face.FaceListParser;
import io.github.leo40git.sltbg.assext.face.FacePoolWriter;
import io.github.leo40git.sltbg.assext.util.MoreColors;
import io.github.leo40git.sltbg.assext.util.MoreFiles;
import io.github.leo40git.sltbg.assext.window.WindowBackground;
import io.github.leo40git.sltbg.assext.window.WindowContext;
import io.github.leo40git.sltbg.assext.window.WindowPalette;
import io.github.leo40git.sltbg.assext.window.WindowTone;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Main {
	public static void main(String[] args) {
		var inputDir = Paths.get("input").toAbsolutePath();
		if (!Files.isDirectory(inputDir)) {
			String message;
			try {
				Files.createDirectories(inputDir);
				message = "Created folder \"" + inputDir + "\".";
			} catch (FileAlreadyExistsException e) {
				message = "Please delete file \"" + inputDir + "\" and create a folder with the same name.";
			} catch (IOException e) {
				message = "Please create folder \"" + inputDir + "\".";
			}

			JOptionPane.showMessageDialog(null, message, "Super Lesbian Textbox Generator", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		var outputDir = Paths.get("output").toAbsolutePath();
		try {
			MoreFiles.deleteDirectoryIfExists(outputDir);
			Files.createDirectories(outputDir);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to create output folder \"" + outputDir + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
			return;
		}

		var ctx = createWindowContext(inputDir);
		if (ctx == null) {
			return;
		}

		createTextboxSheet(ctx, outputDir);
		createPaletteFile(ctx, outputDir);

		extractFaces(inputDir, outputDir);
	}

	private static @Nullable WindowContext createWindowContext(@NotNull Path inputDir) {
		BufferedImage window;
		try (var windowIn = Files.newInputStream(inputDir.resolve("Window.png"))) {
			window = ImageIO.read(windowIn);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to read image \"Window.png\" from input folder \"" + inputDir + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		var tint = new WindowTone(-255, -255, -255, 0);
		return new WindowContext(window, tint);
	}

	private static void createTextboxSheet(@NotNull WindowContext ctx, @NotNull Path outputDir) {
		final int textboxWidth = 640, textboxHeight = 120;
		var image = new BufferedImage(textboxWidth, textboxHeight * 3, BufferedImage.TYPE_INT_ARGB);

		var g = image.createGraphics();
		g.setBackground(MoreColors.TRANSPARENT);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());

		ctx.drawBackground(g,
				WindowBackground.MARGIN, WindowBackground.MARGIN,
				textboxWidth - WindowBackground.MARGIN * 2, textboxHeight - WindowBackground.MARGIN * 2,
				null);
		ctx.drawBorder(g, 0, textboxHeight, textboxWidth, textboxHeight, null);
		ctx.drawArrow(g, 0, textboxHeight * 2, textboxWidth, textboxHeight, 0, null);

		g.dispose();

		try (var os = Files.newOutputStream(outputDir.resolve("textbox.png"))) {
			ImageIO.write(image, "PNG", os);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to write image \"textbox.png\" to output folder \"" + outputDir + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void createPaletteFile(@NotNull WindowContext ctx, @NotNull Path outputDir) {
		try (var writer = Files.newBufferedWriter(outputDir.resolve("palette.txt"))) {
			for (int i = 0; i < WindowPalette.SIZE; i++) {
				var color = ctx.getPaletteColor(i);
				writer.write("#%02X%02X%02X%n".formatted(color.getRed(), color.getGreen(), color.getBlue()));
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to write file \"palette.txt\" to output folder \"" + outputDir + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void extractFaces(@NotNull Path inputDir, @NotNull Path outputDir) {
		List<FaceCollector> collectors;
		try (var reader = Files.newBufferedReader(inputDir.resolve("faces.lst"))) {
			collectors = FaceListParser.parse(inputDir, reader);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to parse \"faces.lst\" in \"" + inputDir + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
			return;
		}

		var executor = Executors.newWorkStealingPool();
		var completionService = new ExecutorCompletionService<List<FacePoolWriter.Entry>>(executor);

		for (var collector : collectors) {
			collector.run(completionService, inputDir);
		}

		FacePoolWriter pool = new FacePoolWriter();
		int done = 0;
		while (done < collectors.size()) {
			Future<List<FacePoolWriter.Entry>> future;
			try {
				future = completionService.take();
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(null,
						"Interrupted while collecting faces",
						"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
				break;
			}
			done++;

			List<FacePoolWriter.Entry> results;
			try {
				results = future.get();
			} catch (ExecutionException e) {
				JOptionPane.showMessageDialog(null,
						"Failed to get face collection results:\n" + e,
						"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
				continue;
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(null,
						"Interrupted while collecting faces",
						"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
				break;
			}

			for (var face : results) {
				pool.add(face);
			}
		}

		executor.shutdownNow();

		var facesDir = outputDir.resolve("faces");
		try {
			Files.createDirectories(facesDir);
			pool.write(facesDir);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to write face pool to directory \"" + facesDir + "\"\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
		}
	}
}
