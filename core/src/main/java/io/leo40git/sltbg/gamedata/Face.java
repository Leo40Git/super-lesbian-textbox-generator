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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Face implements Comparable<Face> {
	private @Nullable FaceCategory category;
	private @NotNull String name;
	private @NotNull BufferedImage image;
	private int order;
	private boolean orderSet;
	private @Nullable String autoCharacterName;
	private boolean autoCharacterNameSet;

	private @Nullable ImageIcon imageAsIcon;

	public Face(@NotNull String name, @NotNull BufferedImage image) {
		this.name = name;
		this.image = image;

		category = null;
		order = 0;
		orderSet = false;
		autoCharacterName = null;
		autoCharacterNameSet = false;

		imageAsIcon = null;
	}

	void clearGeneratedAutoCharacterName() {
		if (!autoCharacterNameSet) {
			autoCharacterName = null;
		}
	}

	public @Nullable FaceCategory getCategory() {
		return category;
	}

	void setCategory(@NotNull FaceCategory category) {
		this.category = category;
		clearGeneratedAutoCharacterName();
		imageAsIcon = null;
	}

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		if (!this.name.equals(name)) {
			if (category != null) {
				category.rename(this, name);
			}
			clearGeneratedAutoCharacterName();
			imageAsIcon = null;
		}
	}

	public @NotNull BufferedImage getImage() {
		return image;
	}

	public void setImage(@NotNull BufferedImage image) {
		this.image = image;
		imageAsIcon = null;
	}

	boolean isOrderSet() {
		return orderSet;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		if (!orderSet || this.order != order) {
			this.order = order;
			orderSet = true;
			if (category != null) {
				category.markDirty();
			}
		}
	}

	public @NotNull String getAutoCharacterName() {
		if (autoCharacterName == null) {
			if (category != null && category.getAutoCharacterName() != null) {
				autoCharacterName = category.getAutoCharacterName();
			} else {
				int commaIndex = name.indexOf(',');
				if (commaIndex < 0) {
					autoCharacterName = name;
				} else {
					autoCharacterName = name.substring(0, commaIndex);
				}
			}
		}

		return autoCharacterName;
	}

	public void setAutoCharacterName(@NotNull String autoCharacterName) {
		this.autoCharacterName = autoCharacterName;
		autoCharacterNameSet = true;
	}

	public @NotNull ImageIcon getImageAsIcon() {
		if (imageAsIcon == null) {
			String description = name;
			if (category != null) {
				description = category + "/" + name;
			}
			imageAsIcon = new ImageIcon(image, description);
		}

		return imageAsIcon;
	}

	@Contract(" -> new")
	public @NotNull Face copy() {
		var clone = new Face(name, image);
		clone.order = order;
		clone.orderSet = orderSet;
		clone.autoCharacterName = autoCharacterName;
		clone.autoCharacterNameSet = autoCharacterNameSet;
		clone.imageAsIcon = imageAsIcon;
		return clone;
	}

	@Override
	public int compareTo(@NotNull Face o) {
		return order - o.order;
	}
}
