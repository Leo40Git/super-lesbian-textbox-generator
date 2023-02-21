/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public final class TextboxSheet {
	public static final int TEXTBOX_WIDTH = 640;
	public static final int TEXTBOX_HEIGHT = 120;
	public static final int FRAME_COUNT = 3;

	private final @NotNull BufferedImage sheet;

	public TextboxSheet(@NotNull BufferedImage sheet) {
		if (sheet.getWidth() != TEXTBOX_WIDTH || sheet.getHeight() != TEXTBOX_HEIGHT * FRAME_COUNT) {
			throw new IllegalArgumentException("sheet has incorrect dimensions: expected %d x %d, got %d x %d"
					.formatted(TEXTBOX_WIDTH, TEXTBOX_HEIGHT * FRAME_COUNT, sheet.getWidth(), sheet.getHeight()));
		}

		this.sheet = sheet;
	}

	public @NotNull BufferedImage getSheet() {
		return sheet;
	}

	public void drawTextboxBackground(@NotNull Graphics g, int x, int y) {
		g.drawImage(sheet,
				x, y, x + TEXTBOX_WIDTH, y + TEXTBOX_HEIGHT,
				0, 0, TEXTBOX_WIDTH, TEXTBOX_HEIGHT,
				null);
	}

	public void drawTextboxBorder(@NotNull Graphics g, int x, int y) {
		g.drawImage(sheet,
				x, y, x + TEXTBOX_WIDTH, y + TEXTBOX_HEIGHT,
				0, TEXTBOX_HEIGHT, TEXTBOX_WIDTH, TEXTBOX_HEIGHT,
				null);
	}

	public void drawTextboxArrow(@NotNull Graphics g, int x, int y) {
		g.drawImage(sheet,
				x, y, x + TEXTBOX_WIDTH, y + TEXTBOX_HEIGHT,
				0, TEXTBOX_HEIGHT * 2, TEXTBOX_WIDTH, TEXTBOX_HEIGHT,
				null);
	}
}
