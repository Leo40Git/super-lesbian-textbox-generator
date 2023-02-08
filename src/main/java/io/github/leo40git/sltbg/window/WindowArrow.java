package io.github.leo40git.sltbg.window;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WindowArrow {
	// this simple "arrow moving up and down" animation is stored as 4 pre-moved frames in the Window sheet
	// (possibly to allow for more interesting animations)
	// anyway, it's drawn centered on the center-bottom piece of the window border when there's no more text to display,
	// and the next button press will begin the next textbox
	// TODO document animation speed

	public static final int FRAME_SIZE = 16;

	private final BufferedImage[] frames = new BufferedImage[4];

	public WindowArrow(@NotNull BufferedImage window) {
		final int startX = 96, startY = 64;

		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 2; x++) {
				frames[y * 2 + x] = window.getSubimage(startX + (x * FRAME_SIZE), startY + (y * FRAME_SIZE), FRAME_SIZE, FRAME_SIZE);
			}
		}
	}

	public void draw(@NotNull Graphics g, int boxX, int boxY, int boxWidth, int boxHeight, int frame, @Nullable ImageObserver observer) {
		int x = boxX + (boxWidth / 2) - (FRAME_SIZE / 2);
		int y = boxY + boxHeight - FRAME_SIZE;

		g.drawImage(frames[frame], x, y, observer);
	}
}
