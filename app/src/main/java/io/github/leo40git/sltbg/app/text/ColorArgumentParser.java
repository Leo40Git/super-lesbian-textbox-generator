package io.github.leo40git.sltbg.app.text;

import java.awt.Color;

import io.github.leo40git.sltbg.app.resources.GamePalette;
import io.github.leo40git.sltbg.app.util.ParsingUtils;
import org.jetbrains.annotations.NotNull;

public final class ColorArgumentParser {
	private ColorArgumentParser() {
		throw new UnsupportedOperationException("ColorArgumentParser only contains static declarations.");
	}

	public static @NotNull Color parse(@NotNull String arg, @NotNull GamePalette palette) {
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
			return palette.get(palIdx);
		}
	}

	private static int parseRGBComponent(String str, String name) {
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
