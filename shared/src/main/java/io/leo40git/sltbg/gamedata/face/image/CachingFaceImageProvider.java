/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face.image;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.IllegalComponentStateException;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
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
    @Contract("_, _ -> new")
    public static @NotNull Caffeine<Path, BufferedImage> createDefaultBuilder(int imageSize, boolean recordStats) {
        var builder = Caffeine.newBuilder()
                .weigher((Weigher<Path, BufferedImage>) (ignored, image) -> ImageUtils.getApproximateMemoryFootprint(image))
                .maximumWeight(imageSize * imageSize * 8L * 80) // 80 images with int data type (most common)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .scheduler(Scheduler.systemScheduler());
        if (recordStats) {
            builder.recordStats();
        }
        return builder;
    }

    private final int imageSize, iconSize;
    private final @NotNull AsyncLoadingCache<Path, BufferedImage> cache;
    private final @NotNull Cleaner iconCleaner;

    public CachingFaceImageProvider(int imageSize, int iconSize, @NotNull Caffeine<Path, BufferedImage> builder) {
        if (iconSize > imageSize) {
            throw new IllegalArgumentException("Icon size (%d x %1$d) cannot be larger than image size (%d x %2$d)"
                    .formatted(iconSize, imageSize));
        }

        this.imageSize = imageSize;
        this.iconSize = iconSize;

        cache = builder.buildAsync(this::loadImage);
        iconCleaner = Cleaner.create();
    }

    public CachingFaceImageProvider(int imageSize, int iconSize, @NotNull CaffeineSpec spec) {
        this(imageSize, iconSize, Caffeine.from(spec)
                .weigher((ignored, image) -> ImageUtils.getApproximateMemoryFootprint(image)));
    }

    public CachingFaceImageProvider(int imageSize, int iconSize) {
        this(imageSize, iconSize, createDefaultBuilder(imageSize, false));
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

    @Override
    public @NotNull CompletableFuture<BufferedImage> getFaceImage(@NotNull Face face) {
        return cache.get(face.getImagePath());
    }

    @Override
    public @NotNull Icon getFaceIcon(@NotNull Face face) {
        var icon = new IconImpl(face.getImagePath());
        icon.setDescription(face.toString());
        return icon;
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

    private final class IconImpl implements Icon, Accessible {
        private static class State implements Runnable {
            private final int size;
            private final @NotNull Path path;
            private @Nullable WeakReference<BufferedImage> imageRef;
            private @Nullable VolatileImage scaledImage;

            public State(int size, @NotNull Path path) {
                this.size = size;
                this.path = path;
            }

            public void paintIcon(@NotNull AsyncLoadingCache<Path, BufferedImage> cache,
                                  @NotNull GraphicsConfiguration gc, Graphics g, int x, int y) {
                BufferedImage image;

                boolean forceRedraw = false;
                if (imageRef == null || (image = imageRef.get()) == null) {
                    imageRef = null;
                    image = null;

                    var future = cache.get(path);
                    if (future.isDone() && !future.isCancelled()) {
                        try {
                            image = future.get();
                            imageRef = new WeakReference<>(image);
                            forceRedraw = true;
                        } catch (InterruptedException | ExecutionException ignored) {
                            // TODO draw error icon
                        }
                    } else {
                        // TODO draw loading icon
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
                    scaledImage = gc.createCompatibleVolatileImage(size, size);
                    redraw = true;
                } else if (forceRedraw || scaledImageVS == VolatileImage.IMAGE_RESTORED) {
                    redraw = true;
                }

                if (redraw) {
                    var scaledImageG = scaledImage.createGraphics();
                    scaledImageG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    scaledImageG.setBackground(ColorUtils.TRANSPARENT);
                    scaledImageG.clearRect(0, 0, size, size);
                    scaledImageG.drawImage(image, 0, 0, size, size, null);
                    scaledImageG.dispose();
                }

                g.drawImage(scaledImage, x, y, null);
            }

            @Override
            public void run() {
                if (imageRef != null) {
                    imageRef.enqueue();
                    imageRef = null;
                }

                if (scaledImage != null) {
                    scaledImage.flush();
                    scaledImage = null;
                }
            }
        }

        private final @NotNull State state;
        private @Nullable String description;
        private @Nullable AccessibleIconImpl accessibleContext;

        public IconImpl(@NotNull Path path) {
            state = new State(CachingFaceImageProvider.this.iconSize, path);
            iconCleaner.register(this, state);
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
            state.paintIcon(cache, gc, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return CachingFaceImageProvider.this.iconSize;
        }

        @Override
        public int getIconHeight() {
            return CachingFaceImageProvider.this.iconSize;
        }

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
                return IconImpl.this.description;
            }

            @Override
            public void setAccessibleIconDescription(String description) {
                IconImpl.this.description = description;
            }

            @Override
            public int getAccessibleIconWidth() {
                return CachingFaceImageProvider.this.iconSize;
            }

            @Override
            public int getAccessibleIconHeight() {
                return CachingFaceImageProvider.this.iconSize;
            }

            //<editor-fold desc="Unimplemented methods">
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
            //</editor-fold>
        }
    }
}
