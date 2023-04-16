/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.window;

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

import org.jetbrains.annotations.NotNull;

final class WindowBackground {
    // there are 2 separate layers for this in the Window sheet: a "base" one and an "overlay" one
    // the "base" overlay is stretched in both directions and tinted by the editor-specified window tint,
    //  while the "overlay" is tiled
    // RPG Maker also renders both layers together at 75% opacity

    private static final int SRC_TILE_SIZE = 64;

    private final WindowTone tone;
    private final BufferedImage skinImage;
    private final int tileSize;
    private final ThreadLocal<BufferedImage> tlScratch;

    public WindowBackground(WindowVersion version, WindowTone tone, BufferedImage skinImage) {
        this.tone = tone;
        this.skinImage = skinImage;
        tileSize = version.scale(SRC_TILE_SIZE);
        tlScratch = new ThreadLocal<>();
    }

    public void draw(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        g.drawImage(getScratch(width, height),
                x, y, x + width, y + height,
                0, 0, width, height,
                observer);
    }

    private static final AlphaComposite OPACITY_COMP = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f);

    private BufferedImage getScratch(int width, int height) {
        var scratch = tlScratch.get();
        if (scratch == null || scratch.getWidth() < width || scratch.getHeight() < height) {
            if (scratch != null) {
                scratch.flush();
            }
            scratch = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            var scratch2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var g = scratch2.createGraphics();
            try {
                // draw stretched and tinted base
                g.setComposite(tone.intensity() > 0 ? new BaseComposite(tone) : AlphaComposite.Src);
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

            g = scratch.createGraphics();
            try {
                // render image is at 75% opacity
                //  (this could've been done in draw to avoid allocating another BufferedImage,
                //   but that would require passing in Graphics2D to draw, which seems pointless)
                g.setComposite(OPACITY_COMP);
                g.drawImage(scratch2, 0, 0, null);
            } finally {
                g.dispose();
                scratch2.flush();
            }

            tlScratch.set(scratch);
        }
        return scratch;
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
                        srcRgba[0] = Math.min(255, Math.max(0, srcRgba[0] + tone.redScaled()));
                        srcRgba[1] = Math.min(255, Math.max(0, srcRgba[1] + tone.greenScaled()));
                        srcRgba[2] = Math.min(255, Math.max(0, srcRgba[2] + tone.blueScaled()));
                        dstOut.setPixel(x + dstOut.getMinX(), y + dstOut.getMinY(), srcRgba);
                    }
                }
            }
        }
    }
}
