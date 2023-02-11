/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.assets.game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

public final class GameTextbox {
	public static final int WIDTH = 640;
	public static final int HEIGHT = 120;

	private static BufferedImage sheet;

	private GameTextbox() {
		throw new UnsupportedOperationException("GameTextbox only contains static declarations.");
	}

	public static void read(@NotNull Path path) throws IOException {
		try (var is = Files.newInputStream(path)) {
			sheet = ImageIO.read(is);
		} catch (IOException e) {
			throw new IOException("Failed to read image at '%s'".formatted(path));
		}

		if (sheet.getWidth() != WIDTH || sheet.getHeight() != HEIGHT * 3) {
			var mySheet = sheet;
			sheet = null;
			throw new IOException("Image '%s' has incorrect dimensions: expected %dx%d, got %dx%d"
					.formatted(path, WIDTH, HEIGHT * 3, mySheet.getWidth(), mySheet.getHeight()));
		}
	}

	public static void drawBackground(@NotNull Graphics g, int x, int y) {
		if (sheet == null) {
			throw new IllegalStateException("Textbox sheet hasn't been loaded yet!");
		}

		g.drawImage(sheet,
				x, y, x + WIDTH, y + HEIGHT,
				0, 0, WIDTH, HEIGHT,
				null);
	}

	public static void drawBorder(@NotNull Graphics g, int x, int y) {
		if (sheet == null) {
			throw new IllegalStateException("Textbox sheet hasn't been loaded yet!");
		}

		g.drawImage(sheet,
				x, y, x + WIDTH, y + HEIGHT,
				0, HEIGHT, WIDTH, HEIGHT,
				null);
	}

	public static void drawArrow(@NotNull Graphics g, int x, int y) {
		if (sheet == null) {
			throw new IllegalStateException("Textbox sheet hasn't been loaded yet!");
		}

		g.drawImage(sheet,
				x, y, x + WIDTH, y + HEIGHT,
				0, HEIGHT * 2, WIDTH, HEIGHT,
				null);
	}
}
