/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class TextPalette {
	public static final int SIZE = 32;

	private final Color[] colors;

	public TextPalette() {
		colors = new Color[32];
		Arrays.fill(colors, Color.BLACK);
	}

	public @NotNull Color get(@Range(from = 0, to = SIZE - 1) int index) {
		return colors[index];
	}

	public void put(@Range(from = 0, to = SIZE - 1) int index, @NotNull Color color) {
		colors[index] = color;
	}

	@Contract("_ -> new")
	public static @NotNull TextPalette read(@NotNull BufferedReader reader) throws IOException {
		var pal = new TextPalette();

		int index = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isBlank()) {
				continue;
			}

			if (index >= SIZE) {
				throw new IOException("Too many lines (more than %d)!".formatted(SIZE));
			}

			try {
				pal.put(index, Color.decode(line));
			} catch (NumberFormatException e) {
				throw new IOException("Line %d: invalid color format".formatted(index + 1), e);
			}

			index++;
		}

		if (index < SIZE - 1) {
			throw new IOException("Not enough lines! Expected 32, got %d".formatted(index));
		}

		return pal;
	}

	public void write(@NotNull BufferedWriter writer) throws IOException {
		for (int i = 0; i < SIZE; i++) {
			var c = colors[i];
			writer.write("#%02X%02X%02X".formatted(c.getRed(), c.getGreen(), c.getBlue()));
			writer.newLine();
		}
	}
}
