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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.element.ColorControlElement;
import io.github.leo40git.sltbg.app.text.element.Element;
import io.github.leo40git.sltbg.app.text.element.IconControlElement;
import io.github.leo40git.sltbg.app.text.element.LineBreakElement;
import io.github.leo40git.sltbg.app.text.element.SizeControlElement;
import io.github.leo40git.sltbg.app.text.element.StyleControlElement;
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
        final var sb = getStringBuilder();
        final var tx = new AffineTransform();

        int fontSizeDelta = 0;
        final var fontActiveStyles = new boolean[TextStyle.COUNT];
        Arrays.fill(fontActiveStyles, false);
        boolean fontNeedsUpdate = false;

        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(GameAssets.getPaletteColor(0));
        g.setFont(GameAssets.getFontAtDefaultSize());
        final int defaultMaxAscent = g.getFontMetrics().getMaxAscent();

        for (int j = 0, elemsSize = elems.size(); j < elemsSize; j++) {
            var elem = elems.get(j);
            if (elem instanceof TextElement text) {
                sb.append(text.getContents());
                // don't skip rendering if this is the last element
                if (j < elemsSize - 1) {
                    continue;
                }
            }

            if (sb.length() > 0) {
                if (fontNeedsUpdate) {
                    g.setFont(getDerivedFont(fontSizeDelta, fontActiveStyles));
                    fontNeedsUpdate = false;
                }

                var layout = new TextLayout(sb.toString(), g.getFont(), g.getFontRenderContext());
                sb.setLength(0);

                tx.setToTranslation(x, y + defaultMaxAscent + fontSizeDelta * 6);
                var outline = layout.getOutline(tx);

                // draw shadow...
                var oc = g.getColor();
                var gTx = g.getTransform();
                gTx.translate(-1, -1);
                g.setColor(Color.BLACK);
                g.fill(outline);

                // ...then draw the actual text
                g.setTransform(gTx);
                g.setColor(oc);
                g.fill(outline);

                x += layout.getAdvance();
            }

            if (elem instanceof LineBreakElement) {
                x = startX;
                // TODO make this accurate
                y += lineHeight + fontSizeDelta * 8;
            } else if (elem instanceof IconControlElement e) {
                //final int yo = defaultMaxAscent / 2 - GameAssets.ICON_SIZE / 2;
                GameAssets.drawIcon(g, x, y /* + yo */, e.getIndex());
                x += GameAssets.ICON_SIZE;
            } else if (elem instanceof ColorControlElement e) {
                g.setColor(e.getColor());
            } else if (elem instanceof StyleControlElement.Toggle e) {
                int i = e.getTarget().ordinal();
                fontActiveStyles[i] = !fontActiveStyles[i];
                fontNeedsUpdate = true;
            } else if (elem instanceof StyleControlElement.Reset) {
                for (int i = 0; i < fontActiveStyles.length; i++) {
                    if (fontActiveStyles[i]) {
                        fontNeedsUpdate = true;
                    }
                    fontActiveStyles[i] = false;
                }
            } else if (elem instanceof SizeControlElement e) {
                if (e.getDelta() == 0) {
                    if (fontSizeDelta != 0) {
                        fontNeedsUpdate = true;
                    }
                    fontSizeDelta = 0;
                } else {
                    fontSizeDelta += e.getDelta();
                    fontNeedsUpdate = true;
                }
            }
        }
    }

    private static final ThreadLocal<HashMap<TextAttribute, Object>> TL_ATTR_MAP = ThreadLocal.withInitial(() -> new HashMap<>(5));

    private static @NotNull Font getDerivedFont(int sizeDelta, boolean @NotNull [] activeStyles) {
        var map = TL_ATTR_MAP.get();
        map.clear();

        if (sizeDelta != 0) {
            // TODO make this accurate
            map.put(TextAttribute.SIZE, GameAssets.DEFAULT_FONT_SIZE + sizeDelta * 8);
        }

        if (activeStyles[TextStyle.BOLD.ordinal()]) {
            map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }

        if (activeStyles[TextStyle.ITALIC.ordinal()]) {
            map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }

        if (activeStyles[TextStyle.UNDERLINE.ordinal()]) {
            map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }

        if (activeStyles[TextStyle.STRIKETHROUGH.ordinal()]) {
            map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }

        if (map.isEmpty()) {
            return GameAssets.getFontAtDefaultSize();
        } else {
            return GameAssets.getFont().deriveFont(map);
        }
    }
}
