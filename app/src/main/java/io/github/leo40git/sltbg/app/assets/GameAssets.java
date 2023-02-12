/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.assets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.TestOnly;

import org.quiltmc.json5.JsonReader;

public final class GameAssets {
	public static final Path ROOT_FOLDER = Paths.get("assets").toAbsolutePath();

	public static final float FONT_DEFAULT_SIZE = 18;
	public static final int PALETTE_SIZE = 32;
	public static final int TEXTBOX_WIDTH = 640;
	public static final int TEXTBOX_HEIGHT = 120;

	private static Color[] palette;
	private static BufferedImage textboxSheet;
	private static Font font;
	private static Map<String, Map<String, Face>> faces;

	private GameAssets() {
		throw new UnsupportedOperationException("GameAssets only contains static declarations.");
	}

	public static void load() throws IOException {
		palette = null;
		textboxSheet = null;
		font = null;
		faces = null;

		if (!Files.isDirectory(ROOT_FOLDER)) {
			throw new IOException("Root folder '" + ROOT_FOLDER + "' does not exist!");
		}

		var palettePath = ROOT_FOLDER.resolve("palette.txt");
		try (var reader = Files.newBufferedReader(palettePath)) {
			palette = new Color[PALETTE_SIZE];

			int index = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}

				if (index >= PALETTE_SIZE) {
					throw new IOException("Too many lines!");
				}

				try {
					palette[index] = Color.decode(line);
				} catch (NumberFormatException e) {
					throw new IOException("Line %d: invalid color format".formatted(index + 1), e);
				}

				index++;
			}

			if (index < PALETTE_SIZE - 1) {
				throw new IOException("Not enough lines! Expected 32, got %d".formatted(index));
			}
		} catch (IOException e) {
			throw new IOException("Failed to read palette color list at '%s'".formatted(palettePath), e);
		}

		var textboxSheetPath = ROOT_FOLDER.resolve("textbox.png");
		try (var in = Files.newInputStream(textboxSheetPath)) {
			textboxSheet = ImageIO.read(in);
		} catch (IOException e) {
			throw new IOException("Failed to read image at '%s'".formatted(textboxSheetPath));
		}

		if (textboxSheet.getWidth() != TEXTBOX_WIDTH || textboxSheet.getHeight() != TEXTBOX_HEIGHT * 3) {
			throw new IOException("Image '%s' has incorrect dimensions: expected %dx%d, got %dx%d"
					.formatted(textboxSheetPath, TEXTBOX_WIDTH, TEXTBOX_HEIGHT * 3, textboxSheet.getWidth(), textboxSheet.getHeight()));
		}

		var fontPath = ROOT_FOLDER.resolve("font.ttf");
		try (var in = Files.newInputStream(fontPath)) {
			font = Font.createFont(Font.TRUETYPE_FONT, in);
		} catch (IOException | FontFormatException e) {
			throw new IOException("Failed to read font at '%s'".formatted(fontPath), e);
		}

		var facesPath = ROOT_FOLDER.resolve("faces.json5");
		try (var reader = JsonReader.json5(facesPath)) {
			faces = new LinkedHashMap<>();

			reader.beginObject();
			while (reader.hasNext()) {
				String category = reader.nextName();
				reader.beginObject();
				faces.put(category, loadFacesCategory(reader, category));
				reader.endObject();
			}
			reader.endObject();
		} catch (IllegalStateException | IOException e) {
			throw new IOException("Failed to read face pool from '%s'".formatted(facesPath), e);
		}

		GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
	}

	private static @NotNull Map<String, Face> loadFacesCategory(@NotNull JsonReader reader, @NotNull String category) throws IOException {
		var faces = new LinkedHashMap<String, GameAssets.Face>();

		while (reader.hasNext()) {
			String name = reader.nextName();
			String rawPath = reader.nextString();

			BufferedImage image;
			var path = ROOT_FOLDER.resolve(rawPath).toAbsolutePath();
			try (var imageIn = Files.newInputStream(path)) {
				image = ImageIO.read(imageIn);
			} catch (IOException e) {
				throw new IOException("%s/%s: failed to read image from file '%s'".formatted(category, name, path));
			}

			faces.put(name, new GameAssets.Face(category, name, image));
		}

		return faces;
	}

	public record Face(@NotNull String category, @NotNull String name, @NotNull BufferedImage image) {
		@Override
		public String toString() {
			return "%s/%s".formatted(category, name);
		}
	}

	public static @NotNull Color getPaletteColor(@Range(from = 0, to = PALETTE_SIZE - 1) int index) {
		if (palette == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		return palette[index];
	}

	@TestOnly
	public static void setPaletteColor(@Range(from = 0, to = PALETTE_SIZE - 1) int index, @NotNull Color color) {
		if (palette == null) {
			palette = new Color[PALETTE_SIZE];
		}

		palette[index] = color;
	}

	public static @NotNull BufferedImage getTextboxSheet() {
		if (textboxSheet == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		return textboxSheet;
	}

	public static void drawTextboxBackground(@NotNull Graphics g, int x, int y) {
		if (textboxSheet == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		g.drawImage(textboxSheet,
				x, y, x + TEXTBOX_WIDTH, y + TEXTBOX_HEIGHT,
				0, 0, TEXTBOX_WIDTH, TEXTBOX_HEIGHT,
				null);
	}

	public static void drawTextboxBorder(@NotNull Graphics g, int x, int y) {
		if (textboxSheet == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		g.drawImage(textboxSheet,
				x, y, x + TEXTBOX_WIDTH, y + TEXTBOX_HEIGHT,
				0, TEXTBOX_HEIGHT, TEXTBOX_WIDTH, TEXTBOX_HEIGHT,
				null);
	}

	public static void drawTextboxArrow(@NotNull Graphics g, int x, int y) {
		if (textboxSheet == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		g.drawImage(textboxSheet,
				x, y, x + TEXTBOX_WIDTH, y + TEXTBOX_HEIGHT,
				0, TEXTBOX_HEIGHT * 2, TEXTBOX_WIDTH, TEXTBOX_HEIGHT,
				null);
	}

	public static @NotNull Font getFont() {
		if (font == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		return font;
	}

	public static @NotNull Map<String, Map<String, Face>> getAllFaces() {
		if (faces == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}

		return faces;
	}

	public static @Nullable Map<String, Face> getFacesByCategory(String category) {
		if (faces == null) {
			throw new IllegalStateException("Game assets haven't been loaded yet (or failed to load)");
		}
		return faces.get(category);
	}
}
