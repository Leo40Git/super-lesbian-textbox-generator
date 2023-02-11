/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.assets.game;

import java.awt.image.BufferedImage;

import org.jetbrains.annotations.NotNull;

public record GameFace(@NotNull String category, @NotNull String name, @NotNull BufferedImage image) {
	@Override
	public String toString() {
		return "%s/%s".formatted(category, name);
	}
}
