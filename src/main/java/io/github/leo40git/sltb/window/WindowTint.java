package io.github.leo40git.sltb.window;

import org.jetbrains.annotations.Range;

/**
 * Represents a tint. Each channel of said tint can be additive or subtractive.
 */
public record WindowTint(
		@Range(from = -255, to = 255) int red,
		@Range(from = -255, to = 255) int green,
		@Range(from = -255, to = 255) int blue)
{ }
