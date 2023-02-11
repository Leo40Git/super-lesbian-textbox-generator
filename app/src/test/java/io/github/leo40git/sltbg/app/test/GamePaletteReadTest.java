/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.leo40git.sltbg.app.assets.game.GamePalette;

public final class GamePaletteReadTest {
	public static void main(String[] args) {
		var is = GamePaletteReadTest.class.getResourceAsStream("/palette.txt");
		if (is == null) {
			System.err.println("create resource palette.txt pls");
			System.exit(1);
			return;
		}

		try (is; var isr = new InputStreamReader(is); var reader = new BufferedReader(isr)) {
			GamePalette.read(reader);
		} catch (IOException e) {
			System.err.println("failed to read palette.txt");
			e.printStackTrace();
			System.exit(1);
			return;
		}

		for (int i = 0; i < GamePalette.SIZE; i++) {
			var color = GamePalette.get(i);
			System.out.format("#%02X%02X%02X%n", color.getRed(), color.getGreen(), color.getBlue());
		}
	}
}
