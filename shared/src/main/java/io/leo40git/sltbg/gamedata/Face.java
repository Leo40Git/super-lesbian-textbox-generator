/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import io.leo40git.sltbg.swing.util.ImageUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Face implements Comparable<Face> {
    public static final int IMAGE_SIZE = 96;
    public static final int ICON_SIZE = IMAGE_SIZE / 2;
    public static final String PATH_DELIMITER = "/";

    private @Nullable NamedFacePalette sourcePalette;
    private @Nullable FacePalette palette;
    private @Nullable FaceCategory category;
    @SuppressWarnings("FieldMayBeFinal")
    private @NotNull String name;
    private @NotNull String imagePath;
    private @Nullable BufferedImage image;
    private long order;
    private boolean orderSet;
    private @Nullable String characterName;
    private boolean characterNameSet;
    private @Nullable ArrayList<String> description;

    private @Nullable ImageIcon icon;

    public Face(@NotNull String name, @NotNull String imagePath) {
        this.name = name;
        this.imagePath = imagePath;

        sourcePalette = null;
        palette = null;
        category = null;
        image = null;
        order = 0;
        orderSet = false;
        characterName = null;
        characterNameSet = false;
        description = null;

        icon = null;
    }

    public @Nullable NamedFacePalette getSourcePalette() {
        return sourcePalette;
    }

    public @Nullable FacePalette getPalette() {
        return palette;
    }

    public @Nullable FaceCategory getCategory() {
        return category;
    }

    void onCategoryRenamed() {
        if (icon != null) {
            icon.setDescription(createIconDescription());
        }
    }

    void onAddedToPalette(@NotNull FacePalette palette) {
        if (palette instanceof NamedFacePalette namedPalette) {
            this.sourcePalette = namedPalette;
        }
        this.palette = palette;
    }

    void onAddedToCategory(@NotNull FaceCategory category) {
        if (category.getPalette() != null) {
            onAddedToPalette(category.getPalette());
        }
        this.category = category;
        onCategoryRenamed();
    }

    void onRemovedFromPalette() {
        if (sourcePalette == palette) {
            sourcePalette = null;
        }
        palette = null;
    }

    void onRemovedFromCategory() {
        onRemovedFromPalette();
        category = null;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        if (category != null) {
            category.rename(this, name);
        }

        if (!characterNameSet) {
            characterName = null;
        }
        if (icon != null) {
            icon.setDescription(createIconDescription());
        }
    }

    public @NotNull String getImagePath() {
        return imagePath;
    }

    public void setImagePath(@NotNull String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean hasImage() {
        return image != null;
    }

    public @NotNull BufferedImage getImage() {
        if (image == null) {
            throw new IllegalStateException("No image!");
        }

        return image;
    }

    public void setImage(@NotNull BufferedImage image) {
        if (image.getWidth() != IMAGE_SIZE || image.getHeight() != IMAGE_SIZE) {
            throw new IllegalArgumentException("image has incorrect dimensions: expected %1$d x %1$d, got %2$d x %3$d"
                    .formatted(IMAGE_SIZE, image.getWidth(), image.getHeight()));
        }

        this.image = image;
        icon = null;
    }

    public void clearImage() {
        image = null;
        icon = null;
    }

    public boolean isOrderSet() {
        return orderSet;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        if (!orderSet || this.order != order) {
            this.order = order;
            orderSet = true;
            if (category != null) {
                category.markDirty();
            }
        }
    }

    public boolean isCharacterNameSet() {
        return characterNameSet;
    }

    public @NotNull String getCharacterName() {
        if (characterNameSet) {
            assert characterName != null;
            return characterName;
        } else if (category != null && category.getCharacterName() != null) {
            return category.getCharacterName();
        } else {
            int commaIndex = name.indexOf(',');
            if (commaIndex < 0) {
                characterName = name;
            } else {
                characterName = name.substring(0, commaIndex);
            }
            return characterName;
        }
    }

    public void setCharacterName(@NotNull String characterName) {
        this.characterName = characterName;
        characterNameSet = true;
    }

    public void clearCharacterName() {
        characterName = null;
        characterNameSet = false;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    public synchronized @NotNull List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
        }
        return description;
    }

    public void clearDescription() {
        description = null;
    }

    public @Nullable ImageIcon getIcon() {
        if (image == null) {
            return null;
        }

        if (icon == null) {
            icon = new ImageIcon(ImageUtils.scaleImage(image, ICON_SIZE, ICON_SIZE), createIconDescription());
        }

        return icon;
    }

    private @NotNull String createIconDescription() {
        if (category == null) {
            return name;
        } else {
            return category + PATH_DELIMITER + name;
        }
    }

    @Contract(" -> new")
    public @NotNull Face copy() {
        var clone = new Face(name, imagePath);
        clone.sourcePalette = sourcePalette;
        clone.image = image;
        clone.order = order;
        clone.orderSet = orderSet;
        clone.characterName = characterName;
        clone.characterNameSet = characterNameSet;
        if (description != null) {
            clone.description = new ArrayList<>(description);
        }
        return clone;
    }

    @Override
    public int compareTo(@NotNull Face o) {
        if (order != o.order) {
            return Long.compare(order, o.getOrder());
        } else {
            return name.compareTo(o.getName());
        }
    }
}
