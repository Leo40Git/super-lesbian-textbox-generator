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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import io.leo40git.sltbg.json.MissingFieldsException;
import io.leo40git.sltbg.util.ImageUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.JsonWriter;

public final class Face implements Comparable<Face> {
	public static final String PATH_DELIMITER = "/";

	private @Nullable FaceCategory category;
	@SuppressWarnings("FieldMayBeFinal")
	private @NotNull String name;
	private @NotNull BufferedImage image;
	private @NotNull String imagePath;
	private int order;
	private boolean orderSet;
	private @Nullable String characterName;
	private boolean characterNameSet;

	private @Nullable ImageIcon imageAsIcon;

	public Face(@NotNull String name, @NotNull BufferedImage image, @NotNull String imagePath) {
		this.name = name;
		this.image = image;
		this.imagePath = imagePath;

		category = null;
		order = 0;
		orderSet = false;
		characterName = null;
		characterNameSet = false;

		imageAsIcon = null;
	}

	public @Nullable FaceCategory getCategory() {
		return category;
	}

	void setCategory(@NotNull FaceCategory category) {
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

	public @NotNull BufferedImage getImage() {
		return image;
	}

	public void setImage(@NotNull BufferedImage image) {
		this.image = image;
		imageAsIcon = null;
	}

	public @NotNull String getImagePath() {
		return imagePath;
	}

	public void setImagePath(@NotNull String imagePath) {
		this.imagePath = imagePath;
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

	public void unsetCharacterName() {
		characterName = null;
		characterNameSet = false;
	}

	public @NotNull ImageIcon getImageAsIcon() {
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
		var clone = new Face(name, image, imagePath);
		clone.order = order;
		clone.orderSet = orderSet;
		clone.characterName = characterName;
		clone.characterNameSet = characterNameSet;
		clone.imageAsIcon = imageAsIcon;
		return clone;
	}

	@Override
	public int compareTo(@NotNull Face o) {
		return order - o.order;
	}

	@Contract("_, _, _ -> new")
	public static @NotNull Face read(@NotNull JsonReader reader, @NotNull String name, @NotNull Path rootPath) throws IOException {
		String imagePath = null;
		boolean orderSet = false;
		int order = 0;
		String charName = null;

		if (reader.peek() == JsonToken.STRING) {
			imagePath = reader.nextString();
		} else {
			reader.beginObject();
			while (reader.hasNext()) {
				String field = reader.nextName();
				switch (field) {
					case FaceFields.IMAGE_PATH -> imagePath = reader.nextString();
					case FaceFields.ORDER -> {
						order = reader.nextInt();
						orderSet = true;
					}
					case FaceFields.CHARACTER_NAME -> charName = reader.nextName();
					default -> reader.skipValue();
				}
			}
			reader.endObject();

			if (imagePath == null) {
				throw new MissingFieldsException(reader, "Face", FaceFields.IMAGE_PATH);
			}
		}

		BufferedImage image;
		try (var is = Files.newInputStream(rootPath.resolve(imagePath))) {
			image = ImageIO.read(is);
		} catch (IOException e) {
			throw new IOException("Failed to read face image", e);
		}

		var face = new Face(name, image, imagePath);
		if (orderSet) {
			face.setOrder(order);
		}
		if (charName != null) {
			face.setCharacterName(charName);
		}
		return face;
	}

	public void write(@NotNull JsonWriter writer, @NotNull Path rootDir) throws IOException {
		writer.name(name);
		if (!orderSet && !characterNameSet) {
			writer.value(imagePath);
		} else {
			writer.beginObject();
			writer.name(FaceFields.IMAGE_PATH);
			writer.value(imagePath);
			if (orderSet) {
				writer.name(FaceFields.ORDER);
				writer.value(order);
			}
			if (characterNameSet) {
				writer.name(FaceFields.CHARACTER_NAME);
				writer.value(characterName);
			}
			writer.endObject();
		}

		try {
			ImageUtils.writeImage(image, rootDir.resolve(imagePath));
		} catch (IOException e) {
			throw new IOException("Failed to write face image", e);
		}
	}
}
