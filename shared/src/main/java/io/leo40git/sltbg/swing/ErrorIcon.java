/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class ErrorIcon extends AbstractIcon {
    private static final class Images {
        private static final AtomicBoolean ready = new AtomicBoolean(false);
        private static ImageIcon img16, img24, img32, img64, img128;

        public static ImageIcon getClosest(int size) {
            init();

            if (size >= 128) {
                return img128;
            } else if (size >= 64) {
                return img64;
            } else if (size >= 32) {
                return img32;
            } else if (size >= 24) {
                return img24;
            } else {
                return img16;
            }
        }

        private static void init() {
            if (!ready.compareAndExchange(false, true)) {
                img16 = createImageIcon("/icons/error_16.png");
                img24 = createImageIcon("/icons/error_24.png");
                img32 = createImageIcon("/icons/error_32.png");
                img64 = createImageIcon("/icons/error_64.png");
                img128 = createImageIcon("/icons/error_128.png");
            }
        }

        private static ImageIcon createImageIcon(String path) {
            var url = Images.class.getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            } else {
                throw new InternalError("Missing image resource: " + path);
            }
        }
    }

    public static void paintIcon(Component c, Graphics g, int x, int y,
                                 @Range(from = 16, to = Integer.MAX_VALUE) int width,
                                 @Range(from = 16, to = Integer.MAX_VALUE) int height) {
        final int size = Math.max(Math.min(width, height) - 4, 16);
        final var image = Images.getClosest(size);

        paintIconFrame(g, x, y, width, height, size);
        image.paintIcon(c, g, x + width / 2 - image.getIconWidth() / 2, y + height / 2 - image.getIconHeight() / 2);
    }

    private static void paintIconFrame(Graphics g, int x, int y, int width, int height, int size) {
        int thickness;
        if (size >= 128) {
            thickness = 8;
        } else if (size >= 64) {
            thickness = 4;
        } else if (size >= 32) {
            thickness = 2;
        } else {
            thickness = 1;
        }

        var oldColor = g.getColor();
        try {
            g.setColor(Color.RED);
            for (int i = thickness; i >= 0; i--) {
                g.drawRect(x + i, y + i, width - i * 2, height - i * 2);
            }
        } finally {
            g.setColor(oldColor);
        }
    }

    private final int width, height, size;
    private final @NotNull ImageIcon image;

    public ErrorIcon(@Range(from = 16, to = Integer.MAX_VALUE) int width, @Range(from = 16, to = Integer.MAX_VALUE) int height) {
        this.width = width;
        this.height = height;
        size = Math.max(Math.min(width, height) - 4, 16);
        image = Images.getClosest(size);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        paintIconFrame(g, x, y, width, height, size);
        image.paintIcon(c, g, x + width / 2 - image.getIconWidth() / 2, y + height / 2 - image.getIconHeight() / 2);
    }
}
