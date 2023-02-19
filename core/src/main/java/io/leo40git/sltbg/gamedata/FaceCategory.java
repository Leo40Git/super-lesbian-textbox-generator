/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FaceCategory implements Comparable<FaceCategory> {
	private @Nullable FacePool pool;
	final @NotNull LinkedHashMap<String, Face> faces;
	private @NotNull String name;
	private boolean orderSet;
	private int order;
	private @Nullable String autoCharacterName;

	private @Nullable Face iconFace, lastFace;
	private boolean needsSort;

	private FaceCategory(@NotNull String name, @NotNull LinkedHashMap<String, Face> faces) {
		this.name = name;
		this.faces = faces;

		pool = null;
		autoCharacterName = null;
		orderSet = false;
		order = 0;

		iconFace = null;
		lastFace = null;
		needsSort = false;
	}

	public FaceCategory(@NotNull String name) {
		this(name, new LinkedHashMap<>());
	}

	public @Nullable FacePool getPool() {
		return pool;
	}

	void setPool(@NotNull FacePool pool) {
		this.pool = pool;
	}

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		if (!this.name.equals(name)) {
			if (pool != null) {
				pool.rename(this, name);
			}
			this.name = name;
			for (var face : faces.values()) {
				face.setCategory(this);
			}
		}
	}

	boolean isOrderSet() {
		return orderSet;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
		orderSet = true;
	}

	public @Nullable String getAutoCharacterName() {
		return autoCharacterName;
	}

	public void setAutoCharacterName(@Nullable String autoCharacterName) {
		if (!Objects.equals(this.autoCharacterName, autoCharacterName)) {
			this.autoCharacterName = autoCharacterName;
			for (var face : faces.values()) {
				face.clearGeneratedAutoCharacterName();
			}
		}
	}

	public @Nullable Face getIconFace() {
		return iconFace;
	}

	private static final ThreadLocal<ArrayList<Face>> TL_SORT_BUF = new ThreadLocal<>();

	void markDirty() {
		needsSort = true;
	}

	public void sortIfNeeded() {
		if (needsSort) {
			var sortBuf = TL_SORT_BUF.get();
			if (sortBuf == null) {
				TL_SORT_BUF.set(sortBuf = new ArrayList<>(faces.size()));
			} else {
				sortBuf.ensureCapacity(faces.size());
			}

			try {
				sortBuf.addAll(faces.values());
				sortBuf.sort(Comparator.naturalOrder());

				faces.clear();
				for (var face : sortBuf) {
					faces.put(face.getName(), face);
				}
			} finally {
				sortBuf.clear();
			}

			needsSort = false;
		}
	}

	public @NotNull @UnmodifiableView Map<String, Face> getFaces() {
		sortIfNeeded();
		return Collections.unmodifiableMap(faces);
	}

	public void add(@NotNull Face face) {
		if (face.getCategory() != null) {
			throw new IllegalArgumentException("Face is already part of other category: \"" + face.getCategory().getName() + "\"");
		}

		if (faces.containsKey(face.getName())) {
			throw new IllegalArgumentException("Face with name \"" + face.getName() + "\" already exists in this category");
		}

		faces.put(face.getName(), face);
		face.setCategory(this);

		if (iconFace == null) {
			iconFace = face;
		}

		if (!face.isOrderSet() && lastFace != null) {
			face.setOrder(FacePool.getNextOrder(lastFace.getOrder()));
		}
		lastFace = face;

		needsSort = true;
	}

	void rename(@NotNull Face face, @NotNull String newName) {
		if (faces.containsKey(newName)) {
			throw new IllegalArgumentException("Face with name \"" + newName + "\" already exists in this category");
		}

		faces.remove(face.getName(), face);
		faces.put(newName, face);
	}

	@Contract(" -> new")
	public @NotNull FaceCategory copy() {
		var clone = new FaceCategory(name, new LinkedHashMap<>(faces.size()));

		for (var faces : faces.values()) {
			clone.add(faces.copy());
		}

		clone.order = order;
		clone.orderSet = orderSet;
		clone.autoCharacterName = autoCharacterName;
		return clone;
	}

	@Override
	public int compareTo(@NotNull FaceCategory o) {
		return order - o.order;
	}

	@Contract("_, _ -> new")
	public static @NotNull FaceCategory read(@NotNull JsonReader reader, @NotNull Path rootDir) throws IOException {
		String name = reader.nextName();

		var category = new FaceCategory(name);

		boolean gotFaces = false;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case FaceFields.FACES -> {
					reader.beginObject();
					while (reader.hasNext()) {
						category.add(Face.read(reader, rootDir));
					}
					reader.endObject();
					gotFaces = true;
				}
				case FaceFields.ORDER -> category.setOrder(reader.nextInt());
				case FaceFields.CHARACTER_NAME -> category.setAutoCharacterName(reader.nextString());
				default -> reader.skipValue();
			}
		}
		reader.endObject();

		if (!gotFaces) {
			throw new MissingFieldsException("Category", FaceFields.FACES);
		}

		return category;
	}

	public void write(@NotNull JsonWriter writer, @NotNull Path rootDir) throws IOException {
		sortIfNeeded();

		writer.name(name);

		writer.beginObject();

		writer.name(FaceFields.FACES);
		writer.beginObject();
		for (var face : faces.values()) {
			face.write(writer, rootDir);
		}
		writer.endObject();

		if (orderSet) {
			writer.name(FaceFields.ORDER);
			writer.value(order);
		}

		if (autoCharacterName != null) {
			writer.name(FaceFields.CHARACTER_NAME);
			writer.value(order);
		}

		writer.endObject();
	}
}
