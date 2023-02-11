/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.element;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

public final class ColorControlElement extends Element {
	private final @NotNull Color color;

	public ColorControlElement(int sourceStart, int sourceLength, @NotNull Color color) {
		super(sourceStart, sourceLength);
		this.color = color;
	}

	@Override
	public boolean isControlElement() {
		return true;
	}

	public @NotNull Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return "Control.Color{" +
				"sourceStart=" + sourceStart +
				", sourceLength=" + sourceLength +
				", color=" + color +
				'}';
	}
}
