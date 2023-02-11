package io.github.leo40git.sltbg.app.text;

import java.util.LinkedList;
import java.util.List;

import io.github.leo40git.sltbg.app.text.element.ContinueLineControlElement;
import io.github.leo40git.sltbg.app.text.element.Element;
import io.github.leo40git.sltbg.app.text.element.ErrorElement;
import io.github.leo40git.sltbg.app.text.element.EscapedTextElement;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;
import io.github.leo40git.sltbg.app.text.element.TextElement;
import io.github.leo40git.sltbg.app.util.StringScanner;
import org.jetbrains.annotations.NotNull;

public final class TextParser {
	public TextParser() {
		// TODO receive palette
	}

	public @NotNull List<Element> parse(@NotNull String source, boolean preserveEscapes) {
		final var elems = new LinkedList<Element>();
		final var scn = new StringScanner(source);
		final var sb = new StringBuilder();

		int sbStart = 0, sbLength = 0;
		boolean escaped = false;
		char ch;
		while ((ch = scn.read()) != StringScanner.EOF) {
			if (escaped) {
				escaped = false;
				switch (ch) {
					case '\n' -> {
						if (preserveEscapes) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ContinueLineControlElement(sbStart));
							sbStart += 2;
						} else {
							sbLength += 2;
						}
					}
					case 'u' -> {
						var valueStr = scn.read(4);
						if (valueStr.isEmpty()) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ErrorElement(sbStart, 2, true,
									"\\u: value is missing or not long enough"));
							sbStart += 2;
							break;
						}

						int value;
						try {
							value = Integer.parseUnsignedInt(valueStr.get(), 16);
						} catch (NumberFormatException ignored) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ErrorElement(sbStart, 6, true,
									"\\u: value is not valid hex number"));
							sbStart += 6;
							break;
						}

						if (preserveEscapes) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new EscapedTextElement(sbStart, 6, Character.toString(value)));
							sbStart += 6;
						} else {
							sb.appendCodePoint(value);
							sbLength += 6;
						}
					}
					case 'U' -> {
						var valueStr = scn.read(8);
						if (valueStr.isEmpty()) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ErrorElement(sbStart, 2, true,
									"\\U: value is missing or not long enough"));
							sbStart += 2;
							break;
						}

						int value;
						try {
							value = Integer.parseUnsignedInt(valueStr.get(), 16);
						} catch (NumberFormatException ignored) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ErrorElement(sbStart, 10, true,
									"\\U: value is not valid hex number"));
							sbStart += 10;
							break;
						}

						if (preserveEscapes) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new EscapedTextElement(sbStart, 10, Character.toString(value)));
							sbStart += 10;
						} else {
							sb.appendCodePoint(value);
							sbLength += 10;
						}
					}
					// TODO color
					// TODO size up/down
					// TODO style (bold, italic, underline, strikethrough)
					// TODO style? (subscript, superscript)
					default -> {
						sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
						sbLength = 0;
						elems.add(new ErrorElement(sbStart, 2, true,
								"unknown control code '\\%c'".formatted(ch)));
						sbStart += 2;
					}
				}
			} else switch (ch) {
				case '\\' -> escaped = true;
				case '\n' -> {
					sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
					sbLength = 0;
					elems.add(new LineBreakElement(sbStart));
					sbStart++;
				}
				default -> {
					sb.append(ch);
					sbLength++;
				}
			}
		}

		flushTextElement(elems, scn, sb, sbStart, sbLength);
		return elems;
	}

	private int flushTextElement(@NotNull List<Element> elems, @NotNull StringScanner scn, @NotNull StringBuilder sb, int sbStart, int sbLength) {
		if (!sb.isEmpty()) {
			elems.add(new TextElement(sbStart, sbLength, sb.toString()));
			sb.setLength(0);
		}
		return sbStart + sbLength;
	}
}
