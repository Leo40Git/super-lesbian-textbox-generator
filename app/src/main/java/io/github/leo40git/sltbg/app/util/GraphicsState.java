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
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record GraphicsState(Composite composite,
                            Paint paint, Stroke stroke,
                            RenderingHints renderingHints, AffineTransform transform,
                            Color foreground, Color background, Shape clip, Font font) {
    @Contract("_ -> new")
    public static @NotNull GraphicsState save(@NotNull Graphics2D g) {
        return new GraphicsState(g.getComposite(), g.getPaint(), g.getStroke(), g.getRenderingHints(),
                g.getTransform(), g.getColor(), g.getBackground(), g.getClip(), g.getFont());
    }

    public void restore(@NotNull Graphics2D g) {
        g.setComposite(composite);
        g.setColor(foreground);
        g.setPaint(paint);
        g.setStroke(stroke);
        g.setRenderingHints(renderingHints);
        g.setTransform(transform);
        g.setBackground(background);
        g.setClip(clip);
        g.setFont(font);
    }
}
