/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.json;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;

public final class JsonReadUtils {
	private JsonReadUtils() {
		throw new UnsupportedOperationException("JsonReadUtils only contains static declarations.");
	}

	public static <T> void readArray(@NotNull JsonReader reader, @NotNull JsonReadDelegate<T> delegate, @NotNull Consumer<T> consumer) throws IOException {
		if (reader.peek() == JsonToken.BEGIN_ARRAY) {
			reader.beginArray();
			while (reader.hasNext()) {
				consumer.accept(delegate.read(reader));
			}
			reader.endArray();
		} else {
			// assume single value
			consumer.accept(delegate.read(reader));
		}
	}

	public static <T> List<T> readArray(@NotNull JsonReader reader, @NotNull JsonReadDelegate<T> delegate) throws IOException {
		List<T> list = new ArrayList<>();
		readArray(reader, delegate, list::add);
		return list;
	}

	public static <T> List<T> readUniqueArray(@NotNull JsonReader reader, @NotNull JsonReadDelegate<T> delegate) throws IOException {
		Set<T> set = new LinkedHashSet<>();
		readArray(reader, delegate, set::add);
		return new ArrayList<>(set);
	}

	@FunctionalInterface
	public interface KeyDeserializer<N> {
		@Nullable N deserialize(@NotNull String name) throws Exception;
	}

	/**
	 * Reads a map from JSON. This method uses the simple object format:
	 * <pre><code>
	 * {
	 *   "key1": "value1",
	 *   "key2": "value2"
	 * }
	 * </code></pre>
	 *
	 * As such, this method can only be used if the type of the key can be serialized as a string.
	 *
	 * @param reader          the reader
	 * @param keyDeserializer a delegate to convert strings to key objects
	 * @param valueDelegate   a delegate to read value objects
	 * @param consumer        a consumer to accept every read entry
	 * @param <K>             the type of the map's keys
	 * @param <V>             the type of the map's values
	 * @throws IOException if an I/O exception occurs.
	 */
	public static <K, V> void readSimpleMap(@NotNull JsonReader reader,
			@NotNull KeyDeserializer<K> keyDeserializer, @NotNull JsonReadDelegate<V> valueDelegate,
			@NotNull BiConsumer<K, V> consumer) throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			K key;
			try {
				key = keyDeserializer.deserialize(name);
			} catch (Exception e) {
				throw new MalformedJsonException(reader, "Failed to deserialize key from \"" + name + "\"", e);
			}
			consumer.accept(key, valueDelegate.read(reader));
		}
		reader.endObject();
	}

	/**
	 * Reads a map from JSON. This method uses the simple object format:
	 * <pre><code>
	 * {
	 *   "key1": "value1",
	 *   "key2": "value2"
	 * }
	 * </code></pre>
	 *
	 * This method is a specialization of {@link #readSimpleMap(JsonReader, KeyDeserializer, JsonReadDelegate, BiConsumer)},
	 * for maps with string keys.
	 *
	 * @param reader        the reader
	 * @param valueDelegate a delegate to read value objects
	 * @param consumer      a consumer to accept every read entry
	 * @param <V>           the type of the map's values
	 * @throws IOException if an I/O exception occurs.
	 */
	public static <V> void readSimpleMap(@NotNull JsonReader reader,
			@NotNull JsonReadDelegate<V> valueDelegate,
			@NotNull BiConsumer<String, V> consumer)
			throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			consumer.accept(reader.nextName(), valueDelegate.read(reader));
		}
		reader.endObject();
	}

	/**
	 * Reads a map from JSON. This method uses a complex format of an array of entry objects:
	 * <pre><code>
	 * [
	 *   {
	 *     "key": {
	 *       ...
	 *     }
	 *     "value": {
	 *       ...
	 *     }
	 *   }
	 * ]
	 * </code></pre>
	 *
	 * Note that this method does not allow {@code null} keys.
	 *
	 * @param reader        the reader
	 * @param keyDelegate   a delegate to read key objects
	 * @param valueDelegate a delegate to read value objects
	 * @param consumer      a consumer to accept every read entry
	 * @param <K>           the type of the map's keys
	 * @param <V>           the type of the map's values
	 * @throws IOException if an I/O exception occurs.
	 */
	public static <K, V> void readComplexMap(@NotNull JsonReader reader,
			@NotNull JsonReadDelegate<K> keyDelegate, @NotNull JsonReadDelegate<V> valueDelegate,
			@NotNull BiConsumer<K, V> consumer) throws IOException {
		List<String> missingFields = new ArrayList<>();

		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			K key = null;
			boolean gotValue = false;
			V value = null;
			while (reader.hasNext()) {
				String field = reader.nextName();
				switch (field) {
					case "key" -> key = keyDelegate.read(reader);
					case "value" -> {
						value = valueDelegate.read(reader);
						gotValue = true;
					}
					default -> reader.skipValue();
				}
			}
			reader.endObject();

			if (key == null) {
				missingFields.add("key");
			}
			if (!gotValue) {
				missingFields.add("value");
			}

			if (!missingFields.isEmpty()) {
				throw new MissingFieldsException(reader, "Map entry", missingFields);
			}

			consumer.accept(key, value);
		}
		reader.endArray();
	}

	public static <T> @Nullable T readNullable(@NotNull JsonReader reader, @NotNull JsonReadDelegate<T> delegate) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		return delegate.read(reader);
	}

	private static final String[] DUMMY_STRING_ARRAY = new String[0];

	public static String[] readStringArray(@NotNull JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.STRING) {
			return new String[] { reader.nextString() };
		} else {
			return readArray(reader, JsonReader::nextString).toArray(DUMMY_STRING_ARRAY);
		}
	}

	public static URL readURL(@NotNull JsonReader reader) throws IOException {
		String s = reader.nextString();
		try {
			return new URL(s);
		} catch (MalformedURLException e) {
			throw new MalformedJsonException(reader, "Failed to parse URL \"" + s + "\"");
		}
	}

	public static Path readPath(@NotNull JsonReader reader) throws IOException {
		String s = reader.nextString();
		URI uri;
		try {
			uri = new URI(s);
		} catch (URISyntaxException e) {
			throw new MalformedJsonException(reader, "Failed to parse URI \"" + s + "\"", e);
		}
		try {
			return Paths.get(uri);
		} catch (IllegalArgumentException e) {
			throw new MalformedJsonException(reader, "Invalid path URI \"" + uri + "\"", e);
		}
	}
}