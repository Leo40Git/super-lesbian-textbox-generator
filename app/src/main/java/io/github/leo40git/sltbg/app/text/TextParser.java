/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

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
	private TextParser() {
		throw new UnsupportedOperationException("TextParser only contains static declarations.");
	}

	public static @NotNull List<Element> parse(@NotNull String source, boolean preserveEscapes) {
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
								color = parseColorArgument(arg);
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
							elems.add(new ColorControlElement(sbStart, 2, GamePalette.get(0)));
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

	private static int flushTextElement(@NotNull List<Element> elems, @NotNull StringScanner scn, @NotNull StringBuilder sb, int sbStart, int sbLength) {
		if (!sb.isEmpty()) {
			elems.add(new TextElement(sbStart, sbLength, sb.toString()));
			sb.setLength(0);
		}
		return sbStart + sbLength;
	}

	private static @NotNull Color parseColorArgument(@NotNull String arg) throws IllegalArgumentException {
		if (arg.startsWith("#")) {
			arg = arg.substring(1).trim();
			int hexLen = arg.length();
			if (hexLen != 3 && hexLen != 6) {
				throw new IllegalArgumentException("Invalid hexadecimal color format, should be 3 or 6 chars long (but was %d)".formatted(hexLen));
			}
			int rgb;
			try {
				rgb = ParsingUtils.parseHexInt(arg);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Failed to parse hexadecimal color", e);
			}
			int r, g, b;
			if (hexLen == 3) {
				// CSS-style
				r = rgb & 0xF;
				r += r << 4;
				g = (rgb >> 4) & 0xF;
				g += g << 4;
				b = (rgb >> 8) & 0xF;
				b += b << 4;
			} else {
				// standard
				r = rgb & 0xFF;
				g = (rgb >> 8) & 0xFF;
				b = (rgb >> 16) & 0xFF;
			}
			return new Color(r | g << 8 | b << 16, false);
		} else {
			int palIdx;
			try {
				palIdx = ParsingUtils.parseDecInt(arg);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Failed to parse palette index", e);
			}
			if (palIdx >= GamePalette.SIZE) {
				throw new IllegalArgumentException("Palette index is out of bounds (must be below %d, but was %d)"
						.formatted(palIdx, GamePalette.SIZE));
			}
			return GamePalette.get(palIdx);
		}
	}

	private static int parseRGBComponent(String str, String name) throws IllegalArgumentException {
		int i;
		try {
			i = ParsingUtils.parseDecInt(str);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Failed to parse %s component".formatted(name), e);
		}
		if (i < 0 || i > 255) {
			throw new IllegalArgumentException("%s component must be between 0 and 255, but was %d".formatted(name, i));
		}
		return i;
	}
}
