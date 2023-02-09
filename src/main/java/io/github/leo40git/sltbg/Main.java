package io.github.leo40git.sltbg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import io.github.leo40git.sltbg.util.MoreColors;
import io.github.leo40git.sltbg.window.WindowBackground;
import io.github.leo40git.sltbg.window.WindowContext;
import io.github.leo40git.sltbg.window.WindowTint;
import org.jetbrains.annotations.NotNull;

public final class Main {
	public static void main(String[] args) {
		var workDir = Paths.get("work").toAbsolutePath();
		if (!Files.isDirectory(workDir)) {
			String messageStart;
			try {
				Files.createDirectory(workDir);
				messageStart = "Created folder \"" + workDir + "\".";
			} catch (FileAlreadyExistsException e) {
				messageStart = "Please delete file \"" + workDir + "\" and create a folder with the same name.";
			} catch (Exception e) {
				messageStart = "Please create folder \"" + workDir + "\".";
			}

			JOptionPane.showMessageDialog(null,
					messageStart + "\n"
							+ "In this folder, add the following files from your SLARPG installation:\n"
							+ " - \"fonts/ChinaCat.ttf\"\n"
							+ " - \"Graphics/System/Window.png\" from the Game.rgss3a archive (NOT the one in the \"Graphics/System\" folder)\n"
							+ " - \"Graphics/Faces/melody faces.png\"",
					"Super Lesbian Textbox Generator", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Font font;
		BufferedImage window, facesM;
		try (var fontIn = Files.newInputStream(workDir.resolve("ChinaCat.ttf"));
			 var windowIn = Files.newInputStream(workDir.resolve("Window.png"));
			 var facesMIn = Files.newInputStream(workDir.resolve("melody faces.png"))) {
			font = Font.createFont(Font.TRUETYPE_FONT, fontIn).deriveFont(18f);
			window = ImageIO.read(windowIn);
			facesM = ImageIO.read(facesMIn);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Failed to read assets from working folder \"" + workDir + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
			return;
		}

		var tint = new WindowTint(-255, -255, -255);
		var ctx = new WindowContext(window, tint);

		final int textboxWidth = 640, textboxHeight = 120;
		final int textboxCount = 1;
		final int textboxVMargin = 2;
		var image = new BufferedImage(textboxWidth, textboxHeight * textboxCount + textboxVMargin * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);

		var g = image.createGraphics();
		g.setBackground(MoreColors.TRANSPARENT);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());

		g.setFont(font);

		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		final int lineHeight = 24;

		ctx.drawBackground(g,
				WindowBackground.MARGIN, WindowBackground.MARGIN,
				textboxWidth - WindowBackground.MARGIN * 2, textboxHeight - WindowBackground.MARGIN * 2,
				null);

		drawFace(g, facesM, 0, 12, 12);

		g.setColor(ctx.getPaletteColor(14));
		drawText(g, "Melody", 124, 12);
		g.setColor(ctx.getPaletteColor(0));
		drawText(g, "Bunny stew is delicious!", 124, 12 + lineHeight);

		ctx.drawBorder(g, 0, 0, textboxWidth, textboxHeight, null);

		ctx.drawArrow(g, 0, 0, textboxWidth, textboxHeight, 0, null);

		g.dispose();

		var outputFile = workDir.resolve("output.png");
		try (var out = Files.newOutputStream(outputFile)) {
			ImageIO.write(image, "png", out);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Failed to write output image to file \"" + outputFile + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void drawFace(@NotNull Graphics2D g, BufferedImage faceSheet, int index, int x, int y) {
		int sx = (index % 4) * 96;
		int sy = (index / 4) * 96;
		assert sx > 0 && sx + 96 < faceSheet.getWidth();
		assert sy > 0 && sy + 96 < faceSheet.getHeight();
		g.drawImage(faceSheet,
				x, y, x + 96, y + 96,
				sx, sy, sx + 96, sy + 96,
				null);
	}

	private static void drawText(@NotNull Graphics2D g, String text, int x, int y) {
		final int yo = g.getFontMetrics().getMaxAscent();
		var color = g.getColor();
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, y + yo + 1);
		g.setColor(color);
		g.drawString(text, x, y + yo);
	}
}
