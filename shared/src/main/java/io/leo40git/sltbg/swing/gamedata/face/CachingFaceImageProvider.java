/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.face;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.leo40git.sltbg.gamedata.face.Face;
import io.leo40git.sltbg.gamedata.face.FaceCategory;
import io.leo40git.sltbg.swing.AbstractIcon;
import io.leo40git.sltbg.swing.ErrorIcon;
import io.leo40git.sltbg.swing.util.ColorUtils;
import io.leo40git.sltbg.swing.util.ImageUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class CachingFaceImageProvider implements FaceImageProvider {
    private final int imageWidth, imageHeight;
    private final int iconWidth, iconHeight;
    private final @NotNull AsyncLoadingCache<Path, ImageReference> imageCache;
    private final @NotNull LoadingCache<Path, IconPainter> iconCache;

    public CachingFaceImageProvider(@NotNull Builder builder) {
        this.imageWidth = builder.imageWidth;
        this.imageHeight = builder.imageHeight;
        this.iconWidth = builder.iconWidth;
        this.iconHeight = builder.iconHeight;

        this.imageCache = builder.getImageCacheBuilder()
                .weigher(this::weighImage)
                .removalListener(this::onImageRemoved)
                .buildAsync(this::loadImage);

        this.iconCache = builder.getIconCacheBuilder()
                .removalListener(this::onIconRemoved)
                .build(IconPainter::new);
    }

    @Override
    public @NotNull CompletableFuture<BufferedImage> getFaceImage(@NotNull Face face) {
        return imageCache.get(face.getImagePath()).thenApply(ImageReference::get);
    }

    @Override
    public @NotNull Icon getFaceIcon(@NotNull Face face) {
        var icon = new IconImpl(face.getImagePath());
        icon.setDescription(face.toString());
        return icon;
    }

    @Override
    public void paintFaceIcon(@NotNull Face face, Component c, Graphics g, int x, int y) {
        iconCache.get(face.getImagePath()).paintIcon(c, g, x, y);
    }

    @Override
    public void paintFaceCategoryIcon(@NotNull FaceCategory category, Component c, Graphics g, int x, int y) {
        var iconFace = category.getIconFace();
        if (iconFace != null) {
            paintFaceIcon(iconFace, c, g, x, y);
        } else {
            throw new IllegalArgumentException("category has no icon face");
        }
    }

    @Override
    public void invalidateAll() {
        imageCache.synchronous().invalidateAll();
        iconCache.invalidateAll();
    }

    private int weighImage(Path ignored, @NotNull ImageReference image) {
        assert image.value != null;
        return ImageUtils.getApproximateMemoryFootprint(image.value);
    }

    private @NotNull ImageReference loadImage(@NotNull Path path) throws IOException {
        BufferedImage image;
        try (var input = Files.newInputStream(path)) {
            image = ImageIO.read(input);
        }

        if (image.getWidth() != imageWidth || image.getHeight() != imageHeight) {
            throw new IOException("Image at \"%s\" has incorrect dimensions: should be %d x %d, but was %d x %d"
                    .formatted(path, imageWidth, imageHeight, image.getWidth(), image.getHeight()));
        }

        return new ImageReference(image);
    }

    private void onImageRemoved(@Nullable Path path, @Nullable ImageReference image, RemovalCause cause) {
        if (image != null) {
            image.clear();
        }
    }

    private void onIconRemoved(@Nullable Path path, @Nullable CachingFaceImageProvider.IconPainter painter, RemovalCause cause) {
        if (painter != null) {
            painter.clear();
        }
    }

    public static final class Builder {
        private final @Range(from = 1, to = Integer.MAX_VALUE) int imageWidth, imageHeight;
        private @Range(from = 1, to = Integer.MAX_VALUE) int iconWidth, iconHeight;
        private @Nullable Caffeine<Object, Object> imageCacheBuilder, iconCacheBuilder;

        private Builder(@Range(from = 1, to = Integer.MAX_VALUE) int imageWidth, @Range(from = 1, to = Integer.MAX_VALUE) int imageHeight) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;

            iconWidth = Math.max(1, imageWidth / 2);
            iconHeight = Math.max(1, imageHeight / 2);
        }

        public Builder setIconWidth(@Range(from = 1, to = Integer.MAX_VALUE) int iconWidth) {
            if (iconWidth > imageWidth) {
                throw new IllegalArgumentException("iconWidth (%d) cannot be larger than imageWidth (%d)"
                        .formatted(iconWidth, imageWidth));
            }

            this.iconWidth = iconWidth;
            return this;
        }

        public Builder setIconHeight(@Range(from = 1, to = Integer.MAX_VALUE) int iconHeight) {
            if (iconHeight > imageHeight) {
                throw new IllegalArgumentException("iconHeight (%d) cannot be larger than imageHeight (%d)"
                        .formatted(iconHeight, imageHeight));
            }

            this.iconHeight = iconHeight;
            return this;
        }

        public Builder setIconSize(@Range(from = 1, to = Integer.MAX_VALUE) int iconSize) {
            if (iconSize > imageWidth || iconSize > imageHeight) {
                throw new IllegalArgumentException("iconSize (%d x %2$d) cannot be larger than image size (%d x %d)"
                        .formatted(iconSize, imageWidth, imageHeight));
            }

            this.iconWidth = iconSize;
            this.iconHeight = iconSize;
            return this;
        }

        public Builder setImageCacheBuilder(@NotNull Caffeine<Object, Object> imageCacheBuilder) {
            this.imageCacheBuilder = imageCacheBuilder;
            return this;
        }

        public Builder setIconCacheBuilder(@NotNull Caffeine<Object, Object> iconCacheBuilder) {
            this.iconCacheBuilder = iconCacheBuilder;
            return this;
        }

        private @NotNull Caffeine<Object, Object> getImageCacheBuilder() {
            return Objects.requireNonNullElseGet(imageCacheBuilder, () -> createDefaultImageCacheBuilder(imageWidth, imageHeight));
        }

        private @NotNull Caffeine<Object, Object> getIconCacheBuilder() {
            return Objects.requireNonNullElseGet(iconCacheBuilder, CachingFaceImageProvider::createDefaultIconCacheBuilder);
        }

        @Contract(" -> new")
        public @NotNull CachingFaceImageProvider build() {
            return new CachingFaceImageProvider(this);
        }
    }

    @Contract("_, _ -> new")
    public static @NotNull Builder builder(
            @Range(from = 1, to = Integer.MAX_VALUE) int imageWidth, @Range(from = 1, to = Integer.MAX_VALUE) int imageHeight) {
        return new Builder(imageWidth, imageHeight);
    }

    @Contract("_ -> new")
    public static @NotNull Builder builder(@Range(from = 1, to = Integer.MAX_VALUE) int imageSize) {
        return new Builder(imageSize, imageSize);
    }

    @Contract("_, _ -> new")
    public static @NotNull CachingFaceImageProvider create(
            @Range(from = 1, to = Integer.MAX_VALUE) int imageWidth, @Range(from = 1, to = Integer.MAX_VALUE) int imageHeight) {
        return new CachingFaceImageProvider(new Builder(imageWidth, imageHeight));
    }

    @Contract("_ -> new")
    public static @NotNull CachingFaceImageProvider create(@Range(from = 1, to = Integer.MAX_VALUE) int imageSize) {
        return new CachingFaceImageProvider(new Builder(imageSize, imageSize));
    }

    @Contract("_, _ -> new")
    public static @NotNull Caffeine<Object, Object> createDefaultImageCacheBuilder(
            @Range(from = 1, to = Integer.MAX_VALUE) int imageWidth, @Range(from = 1, to = Integer.MAX_VALUE) int imageHeight) {
        return Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler())
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumWeight(imageWidth * imageHeight * 8L * 80); // 80 images with int data type (most common)
    }

    @Contract("_ -> new")
    public static @NotNull Caffeine<Object, Object> createDefaultImageCacheBuilder(@Range(from = 1, to = Integer.MAX_VALUE) int imageSize) {
        return createDefaultImageCacheBuilder(imageSize, imageSize);
    }

    @Contract(" -> new")
    public static Caffeine<Object, Object> createDefaultIconCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(128)
                .expireAfterAccess(1, TimeUnit.MINUTES);
    }

    private static final class ImageReference {
        private @Nullable BufferedImage value;

        public ImageReference(@NotNull BufferedImage value) {
            this.value = value;
        }

        public boolean isExpired() {
            return value == null;
        }

        public @NotNull BufferedImage get() {
            if (value == null) {
                throw new IllegalStateException("Reference is expired");
            }
            return value;
        }

        public void clear() {
            if (value != null) {
                value.flush();
                value = null;
            }
        }
    }

    private final class IconPainter {
        private static final int STATE_ERROR = -1;
        private static final int STATE_LOADING = 0;
        private static final int STATE_READY = 1;

        private static final ImageCapabilities ACCELERATED_CAPS = new ImageCapabilities(true);

        private final @NotNull Path imagePath;

        private boolean expired;
        private @Nullable ImageReference image;
        private @Nullable VolatileImage scaledImage;

        public IconPainter(@NotNull Path imagePath) {
            this.imagePath = imagePath;
            expired = false;
        }

        public boolean isExpired() {
            return expired;
        }

        public void paintIcon(@Nullable Component c, @NotNull Graphics g, int x, int y) {
            if (expired || c == null) {
                ErrorIcon.paintIcon(c, g, x, y, CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight);
                return;
            }

            final var gc = c.getGraphicsConfiguration();
            if (gc == null) {
                ErrorIcon.paintIcon(c, g, x, y, CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight);
                return;
            }

            do {
                if (scaledImage == null || scaledImage.validate(gc) != VolatileImage.IMAGE_OK) {
                    int state = paintScaledImage(gc);
                    if (state == STATE_LOADING) {
                        // TODO loading icon
                        var oldColor = g.getColor();
                        try {
                            g.setColor(Color.BLUE);
                            g.fillRect(x, y, CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight);
                        } finally {
                            g.setColor(oldColor);
                        }
                        return;
                    } else if (state == STATE_ERROR) {
                        ErrorIcon.paintIcon(c, g, x, y, CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight);
                        return;
                    }
                }

                g.drawImage(scaledImage, x, y, c);
            } while (scaledImage == null || scaledImage.contentsLost());
        }

        private int paintScaledImage(@NotNull GraphicsConfiguration gc) {
            if (image == null || image.isExpired()) {
                image = null;
                try {
                    var future = CachingFaceImageProvider.this.imageCache.get(imagePath);
                    if (future.isDone()) {
                        image = future.get();
                    } else {
                        return STATE_LOADING;
                    }
                } catch (Exception ignored) { }
            }

            if (image == null) {
                return STATE_ERROR;
            }

            // hold onto full image, in case it takes us so long to draw that the image expires in the meantime
            var fullImage = image.get();

            do {
                if (scaledImage == null || scaledImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
                    if (scaledImage != null) {
                        scaledImage.flush();
                    }

                    try {
                        scaledImage = gc.createCompatibleVolatileImage(
                                CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight,
                                ACCELERATED_CAPS, Transparency.TRANSLUCENT);
                    } catch (AWTException e) {
                        scaledImage = gc.createCompatibleVolatileImage(
                                CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight,
                                Transparency.TRANSLUCENT);
                    }
                }

                var g = scaledImage.createGraphics();
                g.setBackground(ColorUtils.TRANSPARENT);
                g.clearRect(0, 0, CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(fullImage,
                        0, 0,
                        CachingFaceImageProvider.this.iconWidth, CachingFaceImageProvider.this.iconHeight,
                        null);
                g.dispose();
            } while (scaledImage.contentsLost());

            return STATE_READY;
        }

        public void clear() {
            expired = true;

            image = null;

            if (scaledImage != null) {
                scaledImage.flush();
                scaledImage = null;
            }
        }
    }

    private final class IconImpl extends AbstractIcon {
        private final @NotNull Path imagePath;
        private @Nullable CachingFaceImageProvider.IconPainter painter;

        public IconImpl(@NotNull Path imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        public int getIconWidth() {
            return CachingFaceImageProvider.this.iconWidth;
        }

        @Override
        public int getIconHeight() {
            return CachingFaceImageProvider.this.iconHeight;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (painter == null || painter.isExpired()) {
                painter = CachingFaceImageProvider.this.iconCache.get(imagePath);
            }
            painter.paintIcon(c, g, x, y);
        }
    }
}
