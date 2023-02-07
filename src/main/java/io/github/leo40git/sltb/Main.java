package io.github.leo40git.sltb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.leo40git.sltb.util.MoreColors;
import io.github.leo40git.sltb.window.WindowContext;
import io.github.leo40git.sltb.window.WindowTint;
import org.jetbrains.annotations.Nullable;

public class Main {
	public static void main(String[] args) {
		var window = openWindowSheet();
		if (window == null) {
			return;
		}

		// TODO figure this value out
		//  it's in System.rvdata2, need to unmarshal it
		var tint = new WindowTint(0, 0, 0);
		var ctx = new WindowContext(window, tint);

		var image = new BufferedImage(544, 120, BufferedImage.TYPE_INT_ARGB);

		var g = image.createGraphics();
		g.setBackground(MoreColors.TRANSPARENT);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());

		final int margin = 2;
		ctx.drawBackground(g, margin, margin, image.getWidth() - margin * 2, image.getHeight() - margin * 2, null);
		ctx.drawBorder(g, 0, 0, image.getWidth(), image.getHeight(), null);

		g.dispose();

		File outputFile = new File("output.png");
		try {
			ImageIO.write(image, "png", outputFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed to write image to file \"" + outputFile.getAbsolutePath() + "\":\n" + e,
					"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static @Nullable BufferedImage openWindowSheet() {
		var fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
		while (true) {
			if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			var windowFile = fc.getSelectedFile();
			if (windowFile == null) {
				return null;
			}

			if (!windowFile.canRead()) {
				JOptionPane.showMessageDialog(null,
						"File \"" + windowFile.getAbsolutePath() + "\" does not exist or is inaccessible.",
						"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			try {
				return ImageIO.read(windowFile);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,
						"Failed to read image from file \"" + windowFile.getAbsolutePath() + "\":\n" + e,
						"Super Lesbian Textbox Generator", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
