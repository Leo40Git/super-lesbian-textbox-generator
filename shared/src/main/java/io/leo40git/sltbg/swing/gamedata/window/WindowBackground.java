/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.window;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import io.leo40git.sltbg.gamedata.window.WindowTone;
import io.leo40git.sltbg.gamedata.window.WindowVersion;
import org.jetbrains.annotations.NotNull;

final class WindowBackground {
    // there are 2 separate layers for this in the Window sheet: a "base" one and an "overlay" one
    // the "base" overlay is stretched in both directions and tinted by the editor-specified window tint,
    //  while the "overlay" is tiled
    // RPG Maker also renders both layers together at 75% opacity

    private static final int SRC_TILE_SIZE = 64;

    private WindowVersion version;
    private WindowTone tone;
    private float opacity;
    private BufferedImage skinImage;

    private final ThreadLocal<BufferedImage> tlScratch;

    public WindowBackground(WindowVersion version, WindowTone tone, float opacity, BufferedImage skinImage) {
        this.version = version;
        this.tone = tone;
        this.opacity = opacity;
        this.skinImage = skinImage;

        tlScratch = new ThreadLocal<>();
    }

    public void setSkin(WindowVersion version, BufferedImage skinImage) {
        this.version = version;
        this.skinImage = skinImage;
        clearScratch();
    }

    public WindowTone getTone() {
        return tone;
    }

    public void setTone(WindowTone tone) {
        if (!this.tone.equals(tone)) {
            this.tone = tone;
            clearScratch();
        }
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        if (this.opacity != opacity) {
            this.opacity = opacity;
            clearScratch();
        }
    }

    public void paint(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        if (opacity <= 0) {
            return;
        }

        g.drawImage(getScratch(width, height),
                x, y, x + width, y + height,
                0, 0, width, height,
                observer);
    }

    private BufferedImage getScratch(int width, int height) {
        var scratch = tlScratch.get();
        if (scratch == null || scratch.getWidth() < width || scratch.getHeight() < height) {
            if (scratch != null) {
                scratch.flush();
            }

            int tileSize = version.scale(SRC_TILE_SIZE);

            var scratch2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var g = scratch2.createGraphics();
            try {
                // draw stretched and tinted base
                g.setComposite(tone.isEmpty() ? AlphaComposite.Src : new BaseComposite(tone));
                g.drawImage(skinImage,
                        0, 0, width, height,
                        0, 0, tileSize, tileSize,
                        null);

                // draw tiled overlay
                g.setComposite(AlphaComposite.SrcOver);
                final int tilesWide = width / tileSize, tilesHigh = height / tileSize;
                for (int ty = 0; ty <= tilesHigh; ty++) {
                    for (int tx = 0; tx <= tilesWide; tx++) {
                        g.drawImage(skinImage,
                                tx * tileSize, ty * tileSize, tileSize, tileSize,
                                0, tileSize, tileSize, tileSize,
                                null);
                    }
                }
            } finally {
                g.dispose();
            }

            if (opacity >= 1) {
                scratch = scratch2;
            } else {
                scratch = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                g = scratch.createGraphics();
                try {
                    // render image at the requested opacity
                    //  (this could've been done in draw to avoid allocating another BufferedImage,
                    //   but that would require passing in Graphics2D to draw, which seems pointless)
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g.drawImage(scratch2, 0, 0, null);
                } finally {
                    g.dispose();
                    scratch2.flush();
                }
            }

            tlScratch.set(scratch);
        }
        return scratch;
    }

    private void clearScratch() {
        var scratch = tlScratch.get();
        if (scratch != null) {
            scratch.flush();
        }
        tlScratch.remove();
    }

    private static final class BaseComposite implements Composite {
        private final Context context;

        public BaseComposite(@NotNull WindowTone color) {
            context = new Context(color);
        }

        @Override
        public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
            return context;
        }

        @SuppressWarnings("ClassCanBeRecord")
        private static final class Context implements CompositeContext {
            private final WindowTone tone;

            private Context(WindowTone tone) {
                this.tone = tone;
            }

            @Override
            public void dispose() { }

            @Override
            public void compose(@NotNull Raster src, Raster dstIn, @NotNull WritableRaster dstOut) {
                int w = Math.min(src.getWidth(), dstOut.getWidth());
                int h = Math.min(src.getHeight(), dstOut.getHeight());

                int[] srcRgba = new int[4];

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        src.getPixel(x + src.getMinX(), y + src.getMinY(), srcRgba);
                        srcRgba[0] = Math.min(255, Math.max(0, srcRgba[0] + tone.red()));
                        srcRgba[1] = Math.min(255, Math.max(0, srcRgba[1] + tone.green()));
                        srcRgba[2] = Math.min(255, Math.max(0, srcRgba[2] + tone.blue()));
                        dstOut.setPixel(x + dstOut.getMinX(), y + dstOut.getMinY(), srcRgba);
                    }
                }
            }
        }
    }
}
