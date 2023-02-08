package io.github.leo40git.sltbg.window;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <strong>NOTE:</strong> This class is <em>not safe</em> for multithreading.
 *
 * <p>If you need to render the window background on multiple threads, use the {@link #copy()} method to get a copy to
 * pass to another thread.
 */
public final class WindowBackground {
	// there are 2 separate layers for this in the Window sheet: a "base" one and an "overlay" one
	// the "base" overlay is stretched into the correct width and height and tinted by the editor-specified window tint,
	//  while the "overlay" is tiled
	// RPG Maker also renders both layers together at 75% opacity

	public static final int TILE_SIZE = 64;

	private final @NotNull BufferedImage base, overlay;
	private final @NotNull WindowTint color;
	private BufferedImage scratchBuf;

	public WindowBackground(@NotNull BufferedImage window, @NotNull WindowTint color) {
		base = window.getSubimage(0, 0, TILE_SIZE, TILE_SIZE);
		overlay = window.getSubimage(0, TILE_SIZE, TILE_SIZE, TILE_SIZE);
		this.color = color;
	}

	private WindowBackground(@NotNull BufferedImage base, @NotNull BufferedImage overlay, @NotNull WindowTint color, @Nullable BufferedImage scratchBuf) {
		this.base = base;
		this.overlay = overlay;
		this.color = color;
		this.scratchBuf = scratchBuf;
	}

	@Contract(" -> new")
	public @NotNull WindowBackground copy() {
		BufferedImage scratchBufCopy = null;
		if (scratchBuf != null) {
			scratchBufCopy = new BufferedImage(scratchBuf.getColorModel(),
					scratchBuf.copyData(scratchBuf.getRaster().createCompatibleWritableRaster()),
					scratchBuf.isAlphaPremultiplied(), null);
		}
		return new WindowBackground(base, overlay, color, scratchBufCopy);
	}

	public void draw(Graphics2D g, int x, int y, int width, int height, ImageObserver observer) {
		if (resizeScratchBuf(width, height)) {
			Graphics2D sg = scratchBuf.createGraphics();

			// draw stretched and tinted base
			sg.setComposite(new BaseComposite(color));
			sg.drawImage(base, 0, 0, width, height, 0, 0, base.getWidth(), base.getHeight(), null);
			// draw tiled overlay
			sg.setComposite(AlphaComposite.SrcOver);
			final int tilesWide = width / TILE_SIZE, tilesHigh = height / TILE_SIZE;
			for (int ty = 0; ty <= tilesHigh; ty++) {
				for (int tx = 0; tx <= tilesWide; tx++) {
					sg.drawImage(overlay, tx * TILE_SIZE, ty * TILE_SIZE, null);
				}
			}
			sg.dispose();

			// reduce everyone's alpha by 25%
			// NOTE: this loop relies on the scratch buffer being of TYPE_INT_ARGB!
			var buf = (DataBufferInt) scratchBuf.getRaster().getDataBuffer();
			for (int i = 0; i < buf.getSize(); i++) {
				int pixel = buf.getElem(i);
				// mask out old alpha and OR in new alpha
				pixel = (pixel & ~0xFF000000) | (((int) Math.floor(((pixel >> 24) & 0xFF) * 0.75)) << 24);
				buf.setElem(i, pixel);
			}
		}

		g.drawImage(scratchBuf, x, y, x + width, y + height, 0, 0, width, height, observer);
	}

	private boolean resizeScratchBuf(int width, int height) {
		if (scratchBuf == null || scratchBuf.getWidth() < width || scratchBuf.getHeight() < height) {
			scratchBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			return true;
		}
		return false;
	}

	private static final class BaseComposite implements Composite {
		private final Context context;

		public BaseComposite(@NotNull WindowTint color) {
			context = new Context(color);
		}

		@Override
		public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
			return context;
		}

		private record Context(WindowTint color) implements CompositeContext {
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
						srcRgba[0] = Math.min(255, Math.max(0, srcRgba[0] + color.red()));
						srcRgba[1] = Math.min(255, Math.max(0, srcRgba[1] + color.green()));
						srcRgba[2] = Math.min(255, Math.max(0, srcRgba[2] + color.blue()));
						dstOut.setPixel(x + dstOut.getMinX(), y + dstOut.getMinY(), srcRgba);
					}
				}
			}
		}
	}
}
