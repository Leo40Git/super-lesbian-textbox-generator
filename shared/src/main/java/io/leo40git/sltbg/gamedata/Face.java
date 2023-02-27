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

import javax.swing.ImageIcon;

import io.leo40git.sltbg.util.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Face implements Comparable<Face> {
	public static final int IMAGE_SIZE = 96;
	public static final String PATH_DELIMITER = "/";

	private @Nullable FaceCategory category;
	@SuppressWarnings("FieldMayBeFinal")
	private @NotNull String name;
	private @NotNull String imagePath;
	private @Nullable BufferedImage image;
	private long order;
	private boolean orderSet;
	private @Nullable String characterName;
	private boolean characterNameSet;
	private String @NotNull [] description;

	private @Nullable ImageIcon imageAsIcon;

	public Face(@NotNull String name, @NotNull String imagePath) {
		this.name = name;
		this.imagePath = imagePath;

		category = null;
		image = null;
		order = 0;
		orderSet = false;
		characterName = null;
		characterNameSet = false;
		description = ArrayUtils.EMPTY_STRING_ARRAY;

		imageAsIcon = null;
	}

	public @Nullable FaceCategory getCategory() {
		return category;
	}

	void setCategory(@Nullable FaceCategory category) {
		this.category = category;

		if (imageAsIcon != null) {
			imageAsIcon.setDescription(createImageAsIconDescription());
		}
	}

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		if (!this.name.equals(name)) {
			if (category != null) {
				category.rename(this, name);
			}
			
			if (!characterNameSet) {
				characterName = null;
			}
			if (imageAsIcon != null) {
				imageAsIcon.setDescription(createImageAsIconDescription());
			}
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
		if (image.getWidth() != Face.IMAGE_SIZE || image.getHeight() != Face.IMAGE_SIZE) {
			throw new IllegalArgumentException("image has incorrect dimensions: expected %1$d x %1$d, got %2$d x %3$d"
					.formatted(Face.IMAGE_SIZE, image.getWidth(), image.getHeight()));
		}

		this.image = image;
		imageAsIcon = null;
	}

	public void clearImage() {
		image = null;
		imageAsIcon = null;
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

	public String @NotNull [] getDescription() {
		return ArrayUtils.clone(description);
	}

	public void setDescription(String @NotNull [] description) {
		this.description = ArrayUtils.clone(description);
	}

	public @Nullable ImageIcon getImageAsIcon() {
		if (image == null) {
			return null;
		}

		if (imageAsIcon == null) {
			imageAsIcon = new ImageIcon(image, createImageAsIconDescription());
		}

		return imageAsIcon;
	}

	private @NotNull String createImageAsIconDescription() {
		if (category == null) {
			return name;
		} else {
			return category + PATH_DELIMITER + name;
		}
	}

	@Contract(" -> new")
	public @NotNull Face copy() {
		var clone = new Face(name, imagePath);
		clone.image = image;
		clone.order = order;
		clone.orderSet = orderSet;
		clone.characterName = characterName;
		clone.characterNameSet = characterNameSet;
		clone.description = ArrayUtils.clone(description);
		return clone;
	}

	@Override
	public int compareTo(@NotNull Face o) {
		return Long.compare(order, o.order);
	}
}
