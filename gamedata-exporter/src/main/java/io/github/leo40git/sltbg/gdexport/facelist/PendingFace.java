/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facelist;

import java.awt.image.BufferedImage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
public final class PendingFace {
	private final @NotNull String imagePath, category, name;
	private final long order;
	private final @Nullable String characterName;
	private final @NotNull BufferedImage image;

	public PendingFace(@NotNull String imagePath, @NotNull String category, @NotNull String name, long order, @Nullable String characterName,
			@NotNull BufferedImage image) {
		this.imagePath = imagePath;
		this.category = category;
		this.name = name;
		this.order = order;
		this.characterName = characterName;
		this.image = image;
	}

	public @NotNull String getImagePath() {
		return imagePath;
	}

	public @NotNull String getCategory() {
		return category;
	}

	public @NotNull String getName() {
		return name;
	}

	public long getOrder() {
		return order;
	}

	public @Nullable String getCharacterName() {
		return characterName;
	}

	public @NotNull BufferedImage getImage() {
		return image;
	}
}
