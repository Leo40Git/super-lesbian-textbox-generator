/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.resources;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;

public final class GamePalette {
	public static final int SIZE = 32;
	private static final Color[] COLORS = new Color[SIZE];

	private GamePalette() {
		throw new UnsupportedOperationException("GamePalette only contains static declarations.");
	}

	public static @NotNull Color get(int i) {
		var color = COLORS[i];
		if (color == null) {
			return COLORS[i] = Color.BLACK;
		} else {
			return color;
		}
	}

	public static void set(int i, @NotNull Color color) {
		COLORS[i] = color;
	}

	public static void read(@NotNull BufferedReader reader) throws IOException {
		int i = 0;

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isBlank()) {
				continue;
			}

			if (i >= SIZE) {
				throw new IOException("Too many lines!");
			}

			try {
				COLORS[i] = Color.decode(line);
			} catch (NumberFormatException e) {
				throw new IOException("Line %d: invalid color format".formatted(i + 1), e);
			}

			i++;
		}

		if (i < SIZE - 1) {
			throw new IOException("Not enough lines! Expected 32, got %d".formatted(i));
		}
	}
}
