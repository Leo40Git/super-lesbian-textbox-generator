/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.assext.window;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class WindowPalette {
	// this is simple: there are 32 colored squares on the Window sheet,
	//  these directly map to the available 32 preset colors

	public static final int SIZE = 32;

	private final Color[] colors = new Color[SIZE];

	public WindowPalette(@NotNull final BufferedImage window) {
		final int colorWidth = 8, colorHeight = 8;
		final int startX = 64, startY = 96;
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 8; x++) {
				colors[y * 8 + x] = new Color(window.getRGB(startX + (x * colorWidth), startY + (y * colorHeight)), false);
			}
		}
	}

	public @NotNull Color get(@Range(from = 0, to = WindowPalette.SIZE) int index) {
		return colors[index];
	}
}
