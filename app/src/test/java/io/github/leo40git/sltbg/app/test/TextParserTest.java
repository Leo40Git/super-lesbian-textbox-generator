/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.test;

import java.awt.Color;

import io.github.leo40git.sltbg.app.resources.GamePalette;
import io.github.leo40git.sltbg.app.text.TextParser;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;

public final class TextParserTest {
	public static void main(String[] args) {
		final String source = "\\c[14]Melody\n\\c[0]Bunny stew is \\\n\\c[#BB2929]delicious\\c!";

		var palette = new GamePalette();
		palette.set(0, Color.WHITE);
		palette.set(14, new Color(255, 241, 120));

		var parser = new TextParser(palette);
		var elems = parser.parse(source, true);

		System.out.format("%d element(s):%n", elems.size());
		for (var elem : elems) {
			if (elem instanceof LineBreakElement) {
				System.out.println(elem);
			} else {
				System.out.print(elem);
				String sourcePart = source.substring(elem.getSourceStart(), elem.getSourceEnd());
				System.out.format(" (from '%s')%n", sourcePart.replaceAll("\\n", "<\\\\n>"));
			}
		}
	}
}
