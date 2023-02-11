package io.github.leo40git.sltbg.app.text;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import io.github.leo40git.sltbg.app.resources.GamePalette;
import io.github.leo40git.sltbg.app.text.element.ColorControlElement;
import io.github.leo40git.sltbg.app.text.element.ContinueLineControlElement;
import io.github.leo40git.sltbg.app.text.element.Element;
import io.github.leo40git.sltbg.app.text.element.ErrorElement;
import io.github.leo40git.sltbg.app.text.element.EscapedTextElement;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;
import io.github.leo40git.sltbg.app.text.element.TextElement;
import io.github.leo40git.sltbg.app.util.ParsingUtils;
import io.github.leo40git.sltbg.app.util.StringScanner;
import org.jetbrains.annotations.NotNull;

public final class TextParser {
	private final GamePalette palette;

	public TextParser(GamePalette palette) {
		this.palette = palette;
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
							value = ParsingUtils.parseHexInt(valueStr.get());
						} catch (NumberFormatException ignored) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ErrorElement(sbStart, 6, true,
									"\\u: value is not a valid hex number"));
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
							value = ParsingUtils.parseHexInt(valueStr.get());
						} catch (NumberFormatException ignored) {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ErrorElement(sbStart, 10, true,
									"\\U: value is not a valid hex number"));
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
					case 'c' -> {
						if (scn.peek() == '[') {
							scn.next();
							String arg = scn.until(']').orElse(null);
							if (arg == null) {
								sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
								sbLength = 0;
								elems.add(new ErrorElement(sbStart, 3, true,
										"\\c: missing argument end ']'"));
								sbStart += 3;
								break;
							}

							Color color;
							try {
								color = ColorArgumentParser.parse(arg, palette);
							}
							catch (IllegalArgumentException e) {
								sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
								sbLength = 0;
								elems.add(new ErrorElement(sbStart, 4 + arg.length(), true,
										"\\c: invalid argument: %s".formatted(e.getLocalizedMessage())));
								sbStart += 4 + arg.length();
								break;
							}

							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ColorControlElement(sbStart, 4 + arg.length(), color));
							sbStart += 4 + arg.length();
						} else {
							sbStart = flushTextElement(elems, scn, sb, sbStart, sbLength);
							sbLength = 0;
							elems.add(new ColorControlElement(sbStart, 2, palette.get(0)));
							sbStart += 2;
						}
					}
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
