/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.List;

import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.element.Element;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;
import io.github.leo40git.sltbg.app.text.element.TextElement;
import io.github.leo40git.sltbg.app.util.GraphicsState;
import org.jetbrains.annotations.NotNull;

public final class TextRenderer {
	private TextRenderer() {
		throw new UnsupportedOperationException("TextRenderer only contains static declarations.");
	}

	public static void render(@NotNull Graphics2D g, int x, int y, @NotNull List<Element> elems) {
		var oldState = GraphicsState.save(g);
		try {
			render0(g, x, y, elems);
		} finally {
			oldState.restore(g);
		}
	}

	private static final ThreadLocal<StringBuilder> TL_SB = new ThreadLocal<>();

	private static @NotNull StringBuilder getStringBuilder() {
		var sb = TL_SB.get();
		if (sb == null) {
			TL_SB.set(sb = new StringBuilder());
		} else {
			sb.setLength(0);
		}
		return sb;
	}

	private static void render0(@NotNull Graphics2D g, int x, int y, @NotNull List<Element> elems) {
		final int startX = x;
		final int lineHeight = 24;
		final var defaultFont = GameAssets.getFontAtDefaultSize();
		final var sb = getStringBuilder();
		final var tx = new AffineTransform();

		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setColor(GameAssets.getPaletteColor(0));
		g.setFont(defaultFont);
		final int defaultMaxAscent = g.getFontMetrics().getMaxAscent();

		for (var elem : elems) {
			if (elem instanceof TextElement text) {
				sb.append(text.getContents());
				continue;
			}

			final int yo = defaultMaxAscent / 2 - g.getFontMetrics().getMaxAscent() / 2;

			var layout = new TextLayout(sb.toString(), g.getFont(), g.getFontRenderContext());
			sb.setLength(0);

			tx.setToTranslation(x, y + defaultMaxAscent - yo);
			var outline = layout.getOutline(tx);

			// draw shadow...
			var oc = g.getColor();
			var gTx = g.getTransform();
			gTx.translate(1, 1);
			g.setColor(Color.BLACK);
			g.fill(outline);

			// ...then draw actual text
			g.setTransform(gTx);
			g.setColor(oc);
			g.fill(outline);

			x += layout.getAdvance();

			if (elem instanceof LineBreakElement) {
				x = startX;
				y += lineHeight;
			}

			// TODO the rest of the fucking owl
		}
	}
}
