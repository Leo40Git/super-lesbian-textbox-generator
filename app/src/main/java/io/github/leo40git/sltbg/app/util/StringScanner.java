/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.util;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public final class StringScanner {
	public static final char EOF = 0;

	private final char[] chars;
	private final StringBuilder tempSB, tempSB2;
	private int pos;

	public StringScanner(@NotNull String string) {
		chars = string.toCharArray();
		tempSB = new StringBuilder();
		tempSB2 = new StringBuilder();
		pos = 0;
	}

	public int tell() {
		return pos;
	}

	public int end() {
		return chars.length - 1;
	}

	public int remaining() {
		return chars.length - 1 - pos;
	}

	public void seek(int newPos) {
		this.pos = Math.max(0, Math.min(chars.length, newPos));
	}

	public char peek() {
		if (pos >= chars.length) {
			return EOF;
		}
		return chars[pos];
	}

	public void next() {
		pos++;
	}

	public char read() {
		if (pos >= chars.length) {
			return EOF;
		}
		return chars[pos++];
	}

	public @NotNull Optional<String> read(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("length <= 0");
		}
		if (pos + length > chars.length) {
			return Optional.empty();
		}
		var s = new String(chars, pos, length);
		pos += length;
		return Optional.of(s);
	}

	public @NotNull Optional<String> until(char terminator) {
		if (pos >= chars.length) {
			return Optional.empty();
		}
		if (chars[pos] == terminator) {
			return Optional.of("");
		}
		final int startPos = pos;
		char c = read();
		while (c != EOF && c != terminator) {
			c = read();
		}
		if (c == terminator) {
			var s = new String(chars, startPos, pos - startPos - 1);
			return Optional.of(s);
		} else {
			pos = startPos;
			return Optional.empty();
		}
	}

	public @NotNull Optional<String> until(@NotNull String terminator) {
		final int oldPos = pos;
		tempSB.setLength(0);
		boolean found = false;
		final char[] termChars = terminator.toCharArray();
		final int maxSearchPos = chars.length - termChars.length + 1;
		for (; pos < maxSearchPos; pos++) {
			found = true;
			for (int termPos = 0; termPos < termChars.length; termPos++) {
				if (chars[pos + termPos] != termChars[termPos]) {
					found = false;
					break;
				}
			}
			if (found) {
				break;
			} else {
				tempSB.append(peek());
			}
		}
		if (found) {
			pos += termChars.length;
			return Optional.of(tempSB.toString());
		} else {
			pos = oldPos;
			return Optional.empty();
		}
	}

	public @NotNull Optional<String> untilLast(@NotNull String terminator) {
		final int oldPos = pos;
		tempSB.setLength(0);
		tempSB2.setLength(0);
		boolean foundOne = false, found;
		final char[] termChars = terminator.toCharArray();
		final int maxSearchPos = chars.length - termChars.length + 1;
		int endPos = 0;
		for (; pos < maxSearchPos; pos++) {
			found = true;
			for (int termPos = 0; termPos < termChars.length; termPos++) {
				if (chars[pos + termPos] != termChars[termPos]) {
					found = false;
					break;
				}
			}
			if (found) {
				if (foundOne) {
					tempSB.append(terminator);
				}
				tempSB.append(tempSB2);
				tempSB2.setLength(0);
				pos += termChars.length - 1;
				endPos = pos + 1;
				foundOne = true;
			} else {
				tempSB2.append(peek());
			}
		}
		if (foundOne) {
			pos = endPos;
			return Optional.of(tempSB.toString());
		} else {
			pos = oldPos;
			return Optional.empty();
		}
	}
}
