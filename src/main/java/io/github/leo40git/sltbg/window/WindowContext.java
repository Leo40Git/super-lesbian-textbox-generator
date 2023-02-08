package io.github.leo40git.sltbg.window;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <strong>NOTE:</strong> This class is <em>not safe</em> for multithreading.
 *
 * <p>If you need to render the window on multiple threads, use the {@link #copy()} method to get a copy to
 * pass to another thread.
 */
public final class WindowContext {
	private final @NotNull WindowBackground background;
	private final @NotNull WindowBorder border;
	private final @NotNull WindowPalette palette;
	private final @NotNull WindowArrow arrow;

	public WindowContext(@NotNull BufferedImage window, @NotNull WindowTint tint) {
		background = new WindowBackground(window, tint);
		border = new WindowBorder(window);
		arrow = new WindowArrow(window);
		palette = new WindowPalette(window);
	}

	private WindowContext(@NotNull WindowBackground background, @NotNull WindowBorder border, @NotNull WindowPalette palette, @NotNull WindowArrow arrow) {
		this.background = background;
		this.border = border;
		this.palette = palette;
		this.arrow = arrow;
	}

	@Contract(" -> new")
	public @NotNull WindowContext copy() {
		return new WindowContext(background.copy(), border, palette, arrow);
	}

	public void drawBackground(@NotNull Graphics2D g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
		background.draw(g, x, y, width, height, observer);
	}

	public void drawBorder(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
		border.draw(g, x, y, width, height, observer);
	}

	public void drawArrow(@NotNull Graphics g, int boxX, int boxY, int boxWidth, int boxHeight, int frame, @Nullable ImageObserver observer) {
		arrow.draw(g, boxX, boxY, boxWidth, boxHeight, frame, observer);
	}

	public @NotNull WindowPalette getPalette() {
		return palette;
	}

	public @NotNull Color getColor(int index) {
		return palette.get(index);
	}
}
