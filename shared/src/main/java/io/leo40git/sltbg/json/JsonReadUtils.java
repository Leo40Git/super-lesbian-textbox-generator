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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;

public final class JsonReadUtils {
    private JsonReadUtils() {
        throw new UnsupportedOperationException("JsonReadUtils only contains static declarations.");
    }

    @FunctionalInterface
    public interface Delegate<T> {
        @Nullable T read(@NotNull JsonReader reader) throws IOException;
    }

    public static <T> @Nullable T readNullable(@NotNull JsonReader reader, @NotNull Delegate<T> delegate) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return delegate.read(reader);
    }

    public static URL readURL(@NotNull JsonReader reader) throws IOException {
        String locStr = reader.locationString();
        String s = reader.nextString();
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new MalformedJsonException("Failed to parse URL \"" + s + "\"" + locStr, e);
        }
    }

    public static Path readPath(@NotNull JsonReader reader) throws IOException {
        String locStr = reader.locationString();
        String s = reader.nextString();
        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            throw new MalformedJsonException("Failed to parse URI \"" + s + "\"" + locStr, e);
        }
        try {
            return Paths.get(uri);
        } catch (IllegalArgumentException e) {
            throw new MalformedJsonException("Invalid path URI \"" + uri + "\"" + locStr, e);
        }
    }

    public static <T> void readArray(@NotNull JsonReader reader, @NotNull Delegate<T> delegate, @NotNull Consumer<T> consumer) throws IOException {
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

    @Contract("_, _ -> new")
    public static <T> @NotNull List<T> readArray(@NotNull JsonReader reader, @NotNull Delegate<T> delegate) throws IOException {
        List<T> list = new ArrayList<>();
        readArray(reader, delegate, list::add);
        return list;
    }

    @Contract("_, _ -> new")
    public static <T> @NotNull List<T> readUniqueArray(@NotNull JsonReader reader, @NotNull Delegate<T> delegate) throws IOException {
        Set<T> set = new LinkedHashSet<>();
        readArray(reader, delegate, set::add);
        return new ArrayList<>(set);
    }

    public static @NotNull List<String> readStringArray(@NotNull JsonReader reader) throws IOException {
        List<String> list = new ArrayList<>();

        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                list.add(reader.nextString());
            }
            reader.endArray();
        } else {
            list.add(reader.nextString());
        }

        return list;
    }

    @FunctionalInterface
    public interface KeyDeserializer<K> {
        @NotNull K deserialize(@NotNull String name) throws Exception;
    }

    @FunctionalInterface
    public interface KeyAwareDelegate<K, V> {
        @Nullable V read(@NotNull JsonReader reader, @NotNull K key) throws IOException;
    }

    /**
     * Reads a map from JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     * <p>
     * As such, this method can only be used if the type of the key can be serialized as a string.
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
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
                                            @NotNull KeyDeserializer<K> keyDeserializer, @NotNull KeyAwareDelegate<K, V> valueDelegate,
                                            @NotNull BiConsumer<K, V> consumer) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            K key;
            try {
                key = keyDeserializer.deserialize(name);
            } catch (Exception e) {
                throw new MalformedJsonException("Failed to deserialize key from name" + reader.locationString(), e);
            }
            consumer.accept(key, valueDelegate.read(reader, key));
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
     * <p>
     * As such, this method can only be used if the type of the key can be serialized as a string.
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
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
                                            @NotNull KeyDeserializer<K> keyDeserializer, @NotNull KeyAwareDelegate<K, V> valueDelegate,
                                            @NotNull Consumer<V> consumer) throws IOException {
        readSimpleMap(reader, keyDeserializer, valueDelegate, (ignored, value) -> consumer.accept(value));
    }

    /**
     * Reads a map from JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     * <p>
     * As such, this method can only be used if the type of the key can be serialized as a string.
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
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
                                            @NotNull KeyDeserializer<K> keyDeserializer, @NotNull Delegate<V> valueDelegate,
                                            @NotNull BiConsumer<K, V> consumer) throws IOException {
        readSimpleMap(reader, keyDeserializer, (readerx, ignored) -> valueDelegate.read(readerx), consumer);
    }

    /**
     * Reads a map from JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     * <p>
     * This method is a specialization of {@link #readSimpleMap(JsonReader, KeyDeserializer, KeyAwareDelegate, BiConsumer)},
     * for maps with string keys.
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
     *
     * @param reader        the reader
     * @param valueDelegate a delegate to read value objects
     * @param consumer      a consumer to accept every read entry
     * @param <V>           the type of the map's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void readSimpleMap(@NotNull JsonReader reader,
                                         @NotNull KeyAwareDelegate<String, V> valueDelegate,
                                         @NotNull BiConsumer<String, V> consumer)
            throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            consumer.accept(key, valueDelegate.read(reader, key));
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
     * <p>
     * This method is a specialization of {@link #readSimpleMap(JsonReader, KeyDeserializer, KeyAwareDelegate, Consumer)},
     * for maps with string keys.
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
     *
     * @param reader        the reader
     * @param valueDelegate a delegate to read value objects
     * @param consumer      a consumer to accept every read entry
     * @param <V>           the type of the map's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void readSimpleMap(@NotNull JsonReader reader,
                                         @NotNull KeyAwareDelegate<String, V> valueDelegate,
                                         @NotNull Consumer<V> consumer)
            throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            consumer.accept(valueDelegate.read(reader, key));
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
     * <p>
     * This method is a specialization of {@link #readSimpleMap(JsonReader, KeyDeserializer, Delegate, BiConsumer)},
     * for maps with string keys.
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
     *
     * @param reader        the reader
     * @param valueDelegate a delegate to read value objects
     * @param consumer      a consumer to accept every read entry
     * @param <V>           the type of the map's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void readSimpleMap(@NotNull JsonReader reader,
                                         @NotNull Delegate<V> valueDelegate,
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
     * <p>
     * Note that this method does <em>not</em> allow {@code null} keys.
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
                                             @NotNull Delegate<K> keyDelegate, @NotNull Delegate<V> valueDelegate,
                                             @NotNull BiConsumer<K, V> consumer) throws IOException {
        List<String> missingFields = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            String locStr = reader.locationString();

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
                throw new MissingFieldsException("Map entry" + locStr, missingFields);
            }

            consumer.accept(key, value);
        }
        reader.endArray();
    }
}