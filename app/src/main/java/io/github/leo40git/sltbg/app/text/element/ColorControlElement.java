package io.github.leo40git.sltbg.app.text.element;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

public final class ColorControlElement extends Element {
	private final @Nullable Color color;

	public ColorControlElement(int sourceStart, int sourceLength, @Nullable Color color) {
		super(sourceStart, sourceLength);
		this.color = color;
	}

	@Override
	public boolean isControlElement() {
		return true;
	}

	public @Nullable Color getColor() {
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
