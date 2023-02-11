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
