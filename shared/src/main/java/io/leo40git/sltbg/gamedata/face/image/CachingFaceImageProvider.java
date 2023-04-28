/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face.image;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.IllegalComponentStateException;
import java.awt.ImageCapabilities;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.imageio.ImageIO;
import javax.swing.Icon;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Weigher;
import io.leo40git.sltbg.gamedata.face.Face;
import io.leo40git.sltbg.gamedata.face.FaceCategory;
import io.leo40git.sltbg.swing.util.ColorUtils;
import io.leo40git.sltbg.swing.util.ImageUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CachingFaceImageProvider implements FaceImageProvider {
    private static final Weigher<Path, BufferedImage> IMAGE_WEIGHER =
            (ignored, image) -> ImageUtils.getApproximateMemoryFootprint(image);

    public static final class Builder {
        private final int imageSize;
        private int iconSize;
        private @Nullable Caffeine<Object, Object> imageCacheBuilder, iconCacheBuilder;

        private Builder(int imageSize) {
            if (imageSize <= 0) {
                throw new IllegalArgumentException("Image size must be a positive non-zero number");
            }

            this.imageSize = imageSize;
            iconSize = imageSize / 2;
        }

        public Builder setIconSize(int iconSize) {
            if (iconSize <= 0) {
                throw new IllegalArgumentException("Icon size must be a positive non-zero number");
            }

            if (iconSize < imageSize) {
                throw new IllegalArgumentException("Icon size (%d x %1$d) cannot be larger than image size (%d x %2$d)"
                        .formatted(iconSize, imageSize));
            }

            this.iconSize = iconSize;
            return this;
        }

        public Builder setImageCacheBuilder(Caffeine<Object, Object> imageCacheBuilder) {
            this.imageCacheBuilder = imageCacheBuilder;
            return this;
        }

        public Builder setImageCacheSpec(@NotNull CaffeineSpec spec) {
            this.imageCacheBuilder = Caffeine.from(spec)
                    .scheduler(Scheduler.systemScheduler());
            return this;
        }

        public Builder setIconCacheBuilder(Caffeine<Object, Object> iconCacheBuilder) {
            this.iconCacheBuilder = iconCacheBuilder;
            return this;
        }

        public Builder setIconCacheSpec(@NotNull CaffeineSpec spec) {
            this.iconCacheBuilder = Caffeine.from(spec);
            return this;
        }

        @Contract(" -> new")
        public @NotNull CachingFaceImageProvider build() {
            var imageCacheBuilder = this.imageCacheBuilder;
            if (imageCacheBuilder == null) {
                imageCacheBuilder = createDefaultImageCacheBuilder(imageSize);
            }

            var iconCacheBuilder = this.iconCacheBuilder;
            if (iconCacheBuilder == null) {
                iconCacheBuilder = createDefaultIconCacheBuilder();
            }

            return new CachingFaceImageProvider(imageSize, iconSize, imageCacheBuilder, iconCacheBuilder);
        }
    }

    @Contract("_ -> new")
    public static @NotNull Builder builder(int imageSize) {
        return new Builder(imageSize);
    }

    @Contract("_ -> new")
    public static @NotNull Caffeine<Object, Object> createDefaultImageCacheBuilder(int imageSize) {
        return Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler())
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumWeight(imageSize * imageSize * 8L * 80); // 80 images with int data type (most common)
    }

    @Contract(" -> new")
    public static Caffeine<Object, Object> createDefaultIconCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(128)
                .expireAfterAccess(1, TimeUnit.MINUTES);
    }

    private final int imageSize, iconSize;
    private final @NotNull AsyncLoadingCache<Path, BufferedImage> imageCache;
    private final @NotNull LoadingCache<Path, IconData> iconCache;

    private CachingFaceImageProvider(int imageSize, int iconSize,
                                     Caffeine<Object, Object> imageCacheBuilder,
                                     Caffeine<Object, Object> iconCacheBuilder) {
        this.imageSize = imageSize;
        this.iconSize = iconSize;

        this.imageCache = imageCacheBuilder
                .weigher(IMAGE_WEIGHER)
                .removalListener(this::onImageRemoved)
                .buildAsync(this::loadImage);

        this.iconCache = iconCacheBuilder
                .removalListener(this::onIconRemoved)
                .build(IconData::new);
    }

    private @NotNull BufferedImage loadImage(@NotNull Path path) throws IOException {
        BufferedImage image;
        try (var input = Files.newInputStream(path)) {
            image = ImageIO.read(input);
        }

        if (image.getWidth() != imageSize || image.getHeight() != imageSize) {
            throw new IOException("Image at \"%s\" has incorrect dimensions: should be %d x %2$d, but was %d x %d"
                    .formatted(path, imageSize, image.getWidth(), image.getHeight()));
        }
        return image;
    }

    private void onImageRemoved(@Nullable Path path, @Nullable BufferedImage image, RemovalCause cause) {
        if (path != null && !cause.wasEvicted()) {
            var iconData = iconCache.getIfPresent(path);
            if (iconData != null) {
                iconData.notifyImageRemoved();
            }
        }
    }

    private void onIconRemoved(@Nullable Path path, @Nullable IconData iconData, RemovalCause cause) {
        if (iconData != null) {
            iconData.cleanUp();
        }
    }

    @Override
    public @NotNull CompletableFuture<BufferedImage> getFaceImage(@NotNull Face face) {
        return imageCache.get(face.getImagePath());
    }

    @Override
    public @NotNull Icon getFaceIcon(@NotNull Face face) {
        var data = iconCache.get(face.getImagePath());
        var icon = new IconImpl(data);
        icon.setDescription(face.toString());
        return icon;
    }

    @Override
    public void invalidate() {
        imageCache.synchronous().invalidateAll();
        iconCache.invalidateAll();
    }

    @Override
    public @NotNull Icon getFaceCategoryIcon(@NotNull FaceCategory category) {
        var iconFace = category.getIconFace();
        if (iconFace != null) {
            return getFaceIcon(iconFace);
        } else {
            // TODO return fallback icon
            throw new UnsupportedOperationException("fallback icon NYI");
        }
    }

    private final class IconData {
        private static final int STATE_ERROR = -1;
        private static final int STATE_LOADING = 0;
        private static final int STATE_READY = 1;

        private static final ImageCapabilities ACCELERATED_CAPS = new ImageCapabilities(true);

        private final @NotNull Path path;
        private @Nullable BufferedImage image;
        private @Nullable VolatileImage scaledImage;
        private boolean validated, dead;

        public IconData(@NotNull Path path) {
            this.path = path;
            validated = false;
            dead = false;
        }

        public int validate(GraphicsConfiguration gc) {
            if (dead) {
                return STATE_ERROR;
            }

            boolean forceRedraw = false;
            if (image == null) {
                var future = imageCache.get(path);
                if (future.isDone()) {
                    try {
                        image = future.get();
                        forceRedraw = true;
                    } catch (InterruptedException | CancellationException | ExecutionException ignored) {
                        validated = false;
                        return STATE_ERROR;
                    }
                } else {
                    validated = false;
                    return STATE_LOADING;
                }
            }

            int scaledImageVS = VolatileImage.IMAGE_INCOMPATIBLE;
            if (scaledImage != null) {
                scaledImageVS = scaledImage.validate(gc);
            }

            boolean redraw = false;
            if (scaledImageVS == VolatileImage.IMAGE_INCOMPATIBLE) {
                if (scaledImage != null) {
                    scaledImage.flush();
                }

                try {
                    scaledImage = gc.createCompatibleVolatileImage(CachingFaceImageProvider.this.iconSize, CachingFaceImageProvider.this.iconSize, ACCELERATED_CAPS);
                } catch (AWTException ignored) {
                    scaledImage = gc.createCompatibleVolatileImage(CachingFaceImageProvider.this.iconSize, CachingFaceImageProvider.this.iconSize);
                }

                redraw = true;
            } else if (forceRedraw || scaledImageVS == VolatileImage.IMAGE_RESTORED) {
                redraw = true;
            }

            if (redraw) {
                var g = scaledImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setBackground(ColorUtils.TRANSPARENT);
                g.clearRect(0, 0, CachingFaceImageProvider.this.iconSize, CachingFaceImageProvider.this.iconSize);
                g.drawImage(image, 0, 0, CachingFaceImageProvider.this.iconSize, CachingFaceImageProvider.this.iconSize, null);
                g.dispose();
            }

            validated = true;
            return STATE_READY;
        }

        public @NotNull VolatileImage getScaledImage() {
            if (dead) {
                throw new IllegalStateException("This icon data has been cleaned up and is no longer usable!");
            }

            if (!validated) {
                throw new IllegalStateException("Not validated! Call validate and ensure it returns STATE_READY!");
            }
            validated = false;

            assert scaledImage != null;
            return scaledImage;
        }

        public void notifyImageRemoved() {
            validated = false;

            image = null;
        }

        public void cleanUp() {
            validated = false;
            dead = true;

            if (scaledImage != null) {
                scaledImage.flush();
                scaledImage = null;
            }
        }
    }

    private final class IconImpl implements Icon, Accessible {
        private final @NotNull IconData data;
        private @Nullable String description;

        private IconImpl(@NotNull IconData data) {
            this.data = data;
        }

        public @Nullable String getDescription() {
            return description;
        }

        public void setDescription(@Nullable String description) {
            this.description = description;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            var gc = c.getGraphicsConfiguration();
            if (gc == null) {
                return;
            }

            int state = data.validate(gc);
            switch (state) {
                case IconData.STATE_ERROR -> {
                    // TODO draw error icon
                }
                case IconData.STATE_LOADING -> {
                    // TODO draw loading icon
                }
                case IconData.STATE_READY -> g.drawImage(data.getScaledImage(), x, y, null);
            }
        }

        @Override
        public int getIconWidth() {
            return CachingFaceImageProvider.this.iconSize;
        }

        @Override
        public int getIconHeight() {
            return CachingFaceImageProvider.this.iconSize;
        }

        /// region AccessibleContext junk
        private @Nullable AccessibleIconImpl accessibleContext;

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleIconImpl();
            }
            return accessibleContext;
        }

        private final class AccessibleIconImpl extends AccessibleContext implements AccessibleIcon {
            @Override
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.ICON;
            }

            @Override
            public String getAccessibleIconDescription() {
                return IconImpl.this.getDescription();
            }

            @Override
            public void setAccessibleIconDescription(String description) {
                IconImpl.this.setDescription(description);
            }

            @Override
            public int getAccessibleIconWidth() {
                return CachingFaceImageProvider.this.iconSize;
            }

            @Override
            public int getAccessibleIconHeight() {
                return CachingFaceImageProvider.this.iconSize;
            }

            @Override
            public AccessibleStateSet getAccessibleStateSet() {
                return null;
            }

            @Override
            public int getAccessibleIndexInParent() {
                return -1;
            }

            @Override
            public int getAccessibleChildrenCount() {
                return 0;
            }

            @Override
            public Accessible getAccessibleChild(int i) {
                return null;
            }

            @Override
            public Locale getLocale() throws IllegalComponentStateException {
                return null;
            }
        }
        /// endregion
    }
}
