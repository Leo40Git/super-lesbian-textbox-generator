/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.parse;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class TextScanner {
    public static final char EOF = '\0';

    private final String source;
    private final int offset, length;
    private int position;

    public TextScanner(@NotNull String source, @Range(from = 0, to = Integer.MAX_VALUE) int offset, @Range(from = 0, to = Integer.MAX_VALUE) int length) {
        if (offset + length > source.length()) {
            throw new IllegalArgumentException("offset and length do not represent a valid range in source");
        }

        this.source = source;
        this.offset = offset;
        this.length = length;

        position = 0;
    }

    public TextScanner(@NotNull String source) {
        this(source, 0, source.length());
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int tell() {
        return position;
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int remaining() {
        return length - position;
    }

    public void seek(@Range(from = 0, to = Integer.MAX_VALUE) int position) {
        if (position >= length) {
            this.position = length;
        } else {
            this.position = position;
        }
    }

    public void skip() {
        position++;
        if (position > length) {
            position = length;
        }
    }

    public void skip(@Range(from = 0, to = Integer.MAX_VALUE) int delta) {
        position += delta;
        if (position > length) {
            position = length;
        }
    }

    public void rewind() {
        position--;
        if (position < 0) {
            position = 0;
        }
    }

    public void rewind(@Range(from = 0, to = Integer.MAX_VALUE) int delta) {
        position -= delta;
        if (position < 0) {
            position = 0;
        }
    }

    public char peek() {
        if (position >= length) {
            return EOF;
        } else {
            return source.charAt(offset + position);
        }
    }

    public char peek(int delta) {
        if (position + delta > length) {
            return EOF;
        } else {
            return source.charAt(offset + position + delta);
        }
    }

    public char read() {
        if (position >= length) {
            return EOF;
        }

        char ch = source.charAt(offset + position);
        position++;
        return ch;
    }

    @Contract(mutates = "param1")
    public @Range(from = 0, to = Integer.MAX_VALUE) int peek(char @NotNull [] buf, @Range(from = 0, to = Integer.MAX_VALUE) int offset, @Range(from = 0, to = Integer.MAX_VALUE) int length) {
        if (offset + length > buf.length) {
            throw new IllegalArgumentException("offset and length do not represent a valid range in buf");
        }

        if (position + length > this.length) {
            length = this.length - position;
        }

        if (length == 0) {
            return 0;
        }

        source.getChars(this.offset + position, this.offset + position + length, buf, offset);
        return length;
    }

    @Contract(mutates = "param1")
    public @Range(from = 0, to = Integer.MAX_VALUE) int peek(char @NotNull [] buf) {
        return peek(buf, 0, buf.length);
    }

    @Contract(mutates = "param1")
    public boolean readExact(char @NotNull [] buf, @Range(from = 0, to = Integer.MAX_VALUE) int offset, @Range(from = 0, to = Integer.MAX_VALUE) int length) {
        if (peek(buf, offset, length) == length) {
            skip(length);
            return true;
        } else {
            return false;
        }
    }

    @Contract(mutates = "param1")
    public boolean readExact(char @NotNull [] buf) {
        if (peek(buf, 0, buf.length) == buf.length) {
            skip(buf.length);
            return true;
        } else {
            return false;
        }
    }

    public @Nullable String read(int length) {
        if (position + length > this.length) {
            return null;
        } else {
            String result = source.substring(offset + position, offset + position + length);
            skip(length);
            return result;
        }
    }

    public @Nullable String until(char terminator) {
        final int startPosition = position;
        char ch = peek();
        if (ch == EOF) {
            return null;
        } else if (ch == terminator) {
            return "";
        }

        while (ch != EOF && ch != terminator) {
            ch = read();
        }

        if (ch == terminator) {
            return source.substring(offset + startPosition, offset + position - 1);
        } else {
            position = startPosition;
            return null;
        }
    }
}
