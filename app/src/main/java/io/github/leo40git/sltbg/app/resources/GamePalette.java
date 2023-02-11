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

import org.jetbrains.annotations.NotNull;

public final class GamePalette {
	public static final int SIZE = 32;

	private final Color[] colors = new Color[SIZE];

	public GamePalette() {
		for (int i = 0; i < SIZE; i++) {
			colors[i] = Color.BLACK;
		}
	}

	// TODO read from text file

	public @NotNull Color get(int i) {
		return colors[i];
	}

	public void set(int i, @NotNull Color color) {
		colors[i] = color;
	}
}
