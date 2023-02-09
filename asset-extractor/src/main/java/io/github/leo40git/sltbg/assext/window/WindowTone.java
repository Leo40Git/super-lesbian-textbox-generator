package io.github.leo40git.sltbg.assext.window;

import org.jetbrains.annotations.Range;

/**
 * Represents a tone. Each channel of said tone can be additive or subtractive.
 * <p>
 * There is also an <em>intensity</em> channel that the 3 color channels are scaled by.
 */
public record WindowTone(
		@Range(from = -255, to = 255) int red,
		@Range(from = -255, to = 255) int green,
		@Range(from = -255, to = 255) int blue,
		@Range(from = 0, to = 255) int intensity)
{
	public double intensityScale() {
		return intensity / 255.0;
	}

	public int redScaled() {
		return (int)Math.floor(red * intensityScale());
	}

	public int greenScaled() {
		return (int)Math.floor(green * intensityScale());
	}

	public int blueScaled() {
		return (int)Math.floor(blue * intensityScale());
	}
}
