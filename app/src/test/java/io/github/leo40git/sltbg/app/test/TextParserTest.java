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
