package io.github.leo40git.sltbg.app.test;

import io.github.leo40git.sltbg.app.text.TextParser;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;

public final class TextParserTest {
	public static void main(String[] args) {
		final String source = "Melody\nBunny stew is \\\ndelicious! \\u1234\\w";
		var parser = new TextParser();
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
