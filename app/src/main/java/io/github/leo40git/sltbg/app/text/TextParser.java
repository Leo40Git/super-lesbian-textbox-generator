/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text;

import java.util.LinkedList;
import java.util.List;

import io.github.leo40git.sltbg.app.text.element.Element;
import io.github.leo40git.sltbg.app.text.element.ErrorElement;
import io.github.leo40git.sltbg.app.text.element.EscapedTextElement;
import io.github.leo40git.sltbg.app.text.element.InvisibleControlElement;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;
import io.github.leo40git.sltbg.app.text.element.TextElement;
import io.github.leo40git.sltbg.app.text.parse.ControlElementRegistry;
import io.github.leo40git.sltbg.app.text.parse.ParsingUtils;
import io.github.leo40git.sltbg.app.text.parse.TextScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class TextParser {
    private TextParser() {
        throw new UnsupportedOperationException("TextParser only contains static declarations.");
    }

    private static final ThreadLocal<StringBuilder> TL_SB = new ThreadLocal<>();

    private static @NotNull StringBuilder getStringBuilder() {
        var sb = TL_SB.get();
        if (sb == null) {
            TL_SB.set(sb = new StringBuilder());
        } else {
            sb.setLength(0);
        }
        return sb;
    }

    public static @NotNull List<Element> parse(@NotNull String source, boolean preserveInvisible) {
        final var elems = new LinkedList<Element>();
        final var scn = new TextScanner(source);
        final var sb = getStringBuilder();

        int sbStart = 0, sbLength = 0;
        char ch;
        while ((ch = scn.read()) != TextScanner.EOF) {
            switch (ch) {
                case '\\' -> {
                    ch = scn.peek(); // look at next character
                    switch (ch) {
                        case '\\' -> {
                            // escaped backslash
                            if (preserveInvisible) {
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new EscapedTextElement(sbStart, 2, "\\"));
                                sbStart += 2;
                            } else {
                                sb.append('\\');
                                sbLength += 2;
                            }
                            scn.skip();
                        }
                        case '0', '\n' -> {
                            // "null" escape, C-style escaped newline
                            if (preserveInvisible) {
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new InvisibleControlElement(sbStart, 2));
                                sbStart += 2;
                            } else {
                                sbLength += 2;
                            }
                            scn.skip();
                        }
                        case 'u' -> {
                            // 16-bit unicode escape
                            scn.skip();
                            String valueStr = scn.read(4);
                            if (valueStr == null) {
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new ErrorElement(sbStart, 2, true,
                                        "\\u: value is missing or not long enough"));
                                sbStart += 2;
                                break;
                            }

                            int value;
                            try {
                                value = ParsingUtils.parseHexInt(valueStr);
                            } catch (NumberFormatException ignored) {
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new ErrorElement(sbStart, 6, true,
                                        "\\u: value is not a valid hex number"));
                                sbStart += 6;
                                break;
                            }

                            try {
                                if (preserveInvisible) {
                                    String contents = Character.toString(value);
                                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                    sbLength = 0;
                                    elems.add(new EscapedTextElement(sbStart, 6, contents));
                                    sbStart += 6;
                                } else {
                                    sb.appendCodePoint(value);
                                    sbLength += 6;
                                }
                            } catch (IllegalArgumentException e) {
                                // can only come from Character.toString/StringBuilder.appendCodePoint
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new ErrorElement(sbStart, 6, true,
                                        "\\u: value 0x%04X is not a valid Unicode code point".formatted(value)));
                                sbStart += 6;
                            }
                        }
                        case 'U' -> {
                            // 32-bit unicode escape
                            scn.skip();
                            String valueStr = scn.read(8);
                            if (valueStr == null) {
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new ErrorElement(sbStart, 2, true,
                                        "\\U: value is missing or not long enough"));
                                sbStart += 2;
                                break;
                            }

                            int value;
                            try {
                                value = ParsingUtils.parseHexInt(valueStr);
                            } catch (NumberFormatException ignored) {
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new ErrorElement(sbStart, 10, true,
                                        "\\U: value is not a valid hex number"));
                                sbStart += 10;
                                break;
                            }

                            try {
                                if (preserveInvisible) {
                                    String contents = Character.toString(value);
                                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                    sbLength = 0;
                                    elems.add(new EscapedTextElement(sbStart, 10, contents));
                                    sbStart += 10;
                                } else {
                                    sb.appendCodePoint(value);
                                    sbLength += 10;
                                }
                            } catch (IllegalArgumentException e) {
                                // can only come from Character.toString/StringBuilder.appendCodePoint
                                sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                sbLength = 0;
                                elems.add(new ErrorElement(sbStart, 10, true,
                                        "\\U: value 0x%08X is not a valid Unicode code point".formatted(value)));
                                sbStart += 6;
                            }
                        }
                        default -> {
                            if (ch != TextScanner.EOF) {
                                var elem = ControlElementRegistry.parse(scn, sbStart + sbLength);
                                if (elem != null) {
                                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                                    sbLength = 0;
                                    elems.add(elem);
                                    sbStart += elem.getSourceLength();
                                    continue;
                                }
                            }

                            // return the backslash we ate
                            sb.append('\\');
                            sbLength++;
                        }
                    }
                }
                case '\n' -> {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
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

        flushTextElement(elems, sb, sbStart, sbLength);
        return elems;
    }

    private static int flushTextElement(@NotNull List<Element> elems, @NotNull StringBuilder sb,
                                        @Range(from = 0, to = Integer.MAX_VALUE) int sbStart, @Range(from = 1, to = Integer.MAX_VALUE) int sbLength) {
        if (!sb.isEmpty()) {
            elems.add(new TextElement(sbStart, sbLength, sb.toString()));
            sb.setLength(0);
        }
        return sbStart + sbLength;
    }

    // removed for now, may bring these back later (conflict with the underline control)
    private static void processUnicodeEscapes(boolean preserveEscapes, @NotNull TextScanner scn,
                                              @NotNull List<Element> elems, @NotNull StringBuilder sb,
                                              @Range(from = 0, to = Integer.MAX_VALUE) int sbStart, @Range(from = 1, to = Integer.MAX_VALUE) int sbLength,
                                              char ch) {
        switch (ch) {
            case 'u' -> {
                // 16-bit unicode escape
                String valueStr = scn.read(4);
                if (valueStr == null) {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                    sbLength = 0;
                    elems.add(new ErrorElement(sbStart, 2, true,
                            "\\u: value is missing or not long enough"));
                    sbStart += 2;
                    break;
                }

                int value;
                try {
                    value = ParsingUtils.parseHexInt(valueStr);
                } catch (NumberFormatException ignored) {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                    sbLength = 0;
                    elems.add(new ErrorElement(sbStart, 6, true,
                            "\\u: value is not a valid hex number"));
                    sbStart += 6;
                    break;
                }

                if (preserveEscapes) {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                    sbLength = 0;
                    elems.add(new EscapedTextElement(sbStart, 6, Character.toString(value)));
                    sbStart += 6;
                } else {
                    sb.appendCodePoint(value);
                    sbLength += 6;
                }
            }
            case 'U' -> {
                // 32-bit unicode escape
                String valueStr = scn.read(8);
                if (valueStr == null) {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                    sbLength = 0;
                    elems.add(new ErrorElement(sbStart, 2, true,
                            "\\U: value is missing or not long enough"));
                    sbStart += 2;
                    break;
                }

                int value;
                try {
                    value = ParsingUtils.parseHexInt(valueStr);
                } catch (NumberFormatException ignored) {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                    sbLength = 0;
                    elems.add(new ErrorElement(sbStart, 10, true,
                            "\\U: value is not a valid hex number"));
                    sbStart += 10;
                    break;
                }

                if (preserveEscapes) {
                    sbStart = flushTextElement(elems, sb, sbStart, sbLength);
                    sbLength = 0;
                    elems.add(new EscapedTextElement(sbStart, 10, Character.toString(value)));
                    sbStart += 10;
                } else {
                    sb.appendCodePoint(value);
                    sbLength += 10;
                }
            }
        }
    }
}
