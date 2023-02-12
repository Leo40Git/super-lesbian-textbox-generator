/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;

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

	@Contract("_, _ -> new")
	public static @NotNull Color blend(@NotNull Color c1, @NotNull Color c2) {
		double totalAlpha = c1.getAlpha() + c2.getAlpha();
		double weight1 = c1.getAlpha() / totalAlpha;
		double weight2 = c2.getAlpha() / totalAlpha;

		double r = weight1 * c1.getRed() + weight2 * c2.getRed();
		double g = weight1 * c1.getGreen() + weight2 * c2.getGreen();
		double b = weight1 * c1.getBlue() + weight2 * c2.getBlue();
		double a = Math.max(c1.getAlpha(), c2.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}

	private static final ThreadLocal<BufferedImage> TL_PREMULTIPLY_SCRATCH = ThreadLocal.withInitial(() -> new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));

	@Contract("_, _ -> new")
	public static @NotNull Color preMultiply(@NotNull Color base, @NotNull Color overlay) {
		var image = TL_PREMULTIPLY_SCRATCH.get();
		var g = image.createGraphics();
		g.setBackground(base);
		g.clearRect(0, 0, 1, 1);
		g.setComposite(AlphaComposite.SrcOver);
		g.setColor(overlay);
		g.fillRect(0, 0, 1, 1);
		g.dispose();
		return new Color(image.getRGB(0, 0));
	}
}
