/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.util;

import java.awt.Color;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MoreColors {
	private MoreColors() {
		throw new UnsupportedOperationException("MoreColors only contains static declarations.");
	}

	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	@Contract("_, _ -> new")
	public static @NotNull Color withAlpha(@NotNull Color original, int newAlpha) {
		return new Color(original.getRed(), original.getGreen(), original.getBlue(), newAlpha);
	}
}
