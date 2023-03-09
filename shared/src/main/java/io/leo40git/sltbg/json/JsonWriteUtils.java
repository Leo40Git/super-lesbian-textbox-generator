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
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonWriter;

public final class JsonWriteUtils {
    private JsonWriteUtils() {
        throw new UnsupportedOperationException("JsonWriteUtils only contains static declarations.");
    }

    @FunctionalInterface
    public interface Delegate<T> {
        void write(@NotNull T value, @NotNull JsonWriter writer) throws IOException;
    }

    public static <T> void writeNullable(@NotNull JsonWriter writer, @NotNull Delegate<T> delegate, @Nullable T value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            delegate.write(value, writer);
        }
    }

    public static void writeURL(@NotNull JsonWriter writer, @NotNull URL url) throws IOException {
        writer.value(url.toString());
    }

    public static void writePath(@NotNull JsonWriter writer, @NotNull Path path) throws IOException {
        writer.value(path.toUri().toString());
    }

    public static <T> void writeArray(@NotNull JsonWriter writer, @NotNull Delegate<T> delegate, @NotNull Iterable<T> values) throws IOException {
        var it = values.iterator();
        if (!it.hasNext()) {
            // write empty array
            writer.beginArray().endArray();
            return;
        }

        var first = it.next();
        if (!it.hasNext()) {
            // write one value
            delegate.write(first, writer);
            return;
        }

        writer.beginArray();
        delegate.write(first, writer);
        while (it.hasNext()) {
            delegate.write(it.next(), writer);
        }
        writer.endArray();
    }

    @SafeVarargs
    public static <T> void writeArray(@NotNull JsonWriter writer, @NotNull Delegate<T> delegate, T @NotNull ... values) throws IOException {
        if (values.length == 1) {
            delegate.write(values[0], writer);
        } else {
            writer.beginArray();
            for (var value : values) {
                delegate.write(value, writer);
            }
            writer.endArray();
        }
    }

    public static void writeStringArray(@NotNull JsonWriter writer, @NotNull Iterable<? extends String> values) throws IOException {
        var it = values.iterator();
        if (!it.hasNext()) {
            // write empty array
            writer.beginArray().endArray();
            return;
        }

        var first = it.next();
        if (!it.hasNext()) {
            // write one value
            writer.value(first);
            return;
        }

        writer.beginArray();
        writer.value(first);
        while (it.hasNext()) {
            writer.value(it.next());
        }
        writer.endArray();
    }
    
    public static void writeStringArray(@NotNull JsonWriter writer, String @NotNull ... values) throws IOException {
        if (values.length == 1) {
            writer.value(values[0]);
        } else {
            writer.beginArray();
            for (var value : values) {
                writer.value(value);
            }
            writer.endArray();
        }
    }

    public static void writeBooleanArray(@NotNull JsonWriter writer, @NotNull Iterable<? extends Boolean> values) throws IOException {
        var it = values.iterator();
        if (!it.hasNext()) {
            // write empty array
            writer.beginArray().endArray();
            return;
        }

        var first = it.next();
        if (!it.hasNext()) {
            // write one value
            writer.value(first);
            return;
        }

        writer.beginArray();
        writer.value(first);
        while (it.hasNext()) {
            writer.value(it.next());
        }
        writer.endArray();
    }

    public static void writeBooleanArray(@NotNull JsonWriter writer, Boolean @NotNull ... values) throws IOException {
        if (values.length == 1) {
            writer.value(values[0]);
        } else {
            writer.beginArray();
            for (var value : values) {
                writer.value(value);
            }
            writer.endArray();
        }
    }

    public static void writeNumberArray(@NotNull JsonWriter writer, @NotNull Iterable<? extends Number> values) throws IOException {
        var it = values.iterator();
        if (!it.hasNext()) {
            // write empty array
            writer.beginArray().endArray();
            return;
        }

        var first = it.next();
        if (!it.hasNext()) {
            // write one value
            writer.value(first);
            return;
        }

        writer.beginArray();
        writer.value(first);
        while (it.hasNext()) {
            writer.value(it.next());
        }
        writer.endArray();
    }

    public static void writeNumberArray(@NotNull JsonWriter writer, Number @NotNull ... values) throws IOException {
        if (values.length == 1) {
            writer.value(values[0]);
        } else {
            writer.beginArray();
            for (var value : values) {
                writer.value(value);
            }
            writer.endArray();
        }
    }

    public static void writeNumberArray(@NotNull JsonWriter writer, int @NotNull ... values) throws IOException {
        if (values.length == 1) {
            writer.value(values[0]);
        } else {
            writer.beginArray();
            for (var value : values) {
                writer.value(value);
            }
            writer.endArray();
        }
    }

    public static void writeNumberArray(@NotNull JsonWriter writer, long @NotNull ... values) throws IOException {
        if (values.length == 1) {
            writer.value(values[0]);
        } else {
            writer.beginArray();
            for (var value : values) {
                writer.value(value);
            }
            writer.endArray();
        }
    }

    public static void writeNumberArray(@NotNull JsonWriter writer, double @NotNull ... values) throws IOException {
        if (values.length == 1) {
            writer.value(values[0]);
        } else {
            writer.beginArray();
            for (var value : values) {
                writer.value(value);
            }
            writer.endArray();
        }
    }

    /**
     * Writes an object containing the specified values to JSON.
     * <br>
     * The delegate <em>must</em> handle writing names.
     *
     * @param writer   the writer
     * @param delegate a delegate to write value objects
     * @param values   the values
     * @param <V>      the type of the object's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void writeObject(@NotNull JsonWriter writer,
                                       @NotNull Delegate<V> delegate,
                                       @NotNull Iterable<V> values) throws IOException {
        writer.beginObject();

        for (var value : values) {
            delegate.write(value, writer);
        }

        writer.endObject();
    }

    /**
     * Writes an object containing the specified values to JSON.
     * <br>
     * The delegate <em>must</em> handle writing names.
     *
     * @param writer   the writer
     * @param delegate a delegate to write value objects
     * @param values   the values
     * @param <V>      the type of the object's values
     * @throws IOException if an I/O exception occurs.
     */
    @SafeVarargs
    public static <V> void writeObject(@NotNull JsonWriter writer,
                                       @NotNull Delegate<V> delegate,
                                       V @NotNull ... values) throws IOException {
        writer.beginObject();

        for (var value : values) {
            delegate.write(value, writer);
        }

        writer.endObject();
    }

    @FunctionalInterface
    public interface KeySerializer<K> {
        @NotNull String serialize(@Nullable K key) throws Exception;
    }

    /**
     * Writes a map to JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     * <p>
     * As such, this method can only be used if the type of the key can be serialized as a string.
     *
     * @param writer        the writer
     * @param keySerializer a delegate to convert key objects to strings
     * @param valueDelegate a delegate to write value objects
     * @param map           the map entries to write
     * @param <K>           the type of the map's keys
     * @param <V>           the type of the map's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <K, V> void writeSimpleMap(@NotNull JsonWriter writer,
                                             @NotNull KeySerializer<K> keySerializer, @NotNull Delegate<V> valueDelegate,
                                             @NotNull Map<K, V> map) throws IOException {
        writer.beginObject();
        for (var entry : map.entrySet()) {
            String name;
            try {
                name = keySerializer.serialize(entry.getKey());
            } catch (Exception e) {
                throw new IOException("Failed to serialize key", e);
            }
            writer.name(name);
            valueDelegate.write(entry.getValue(), writer);
        }
        writer.endObject();
    }

    /**
     * Writes a map to JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     * <p>
     * This method is a specialization of {@link #writeSimpleMap(JsonWriter, KeySerializer, Delegate, Map)},
     * for maps with string keys.
     *
     * @param writer        the writer
     * @param valueDelegate a delegate to write value objects
     * @param map           the map to write
     * @param <V>           the type of the map's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void writeSimpleMap(@NotNull JsonWriter writer,
                                          @NotNull Delegate<V> valueDelegate,
                                          @NotNull Map<String, V> map)
            throws IOException {
        writer.beginObject();
        for (var entry : map.entrySet()) {
            writer.name(entry.getKey());
            valueDelegate.write(entry.getValue(), writer);
        }
        writer.endObject();
    }

    /**
     * Writes a map to JSON. This method uses a complex format of an array of entry objects:
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
     * @param writer        the writer
     * @param keyDelegate   a delegate to write key objects
     * @param valueDelegate a delegate to write value objects
     * @param map           the map to write
     * @param <K>           the type of the map's keys
     * @param <V>           the type of the map's values
     * @throws IOException if an I/O exception occurs.
     */
    public static <K, V> void writeComplexMap(@NotNull JsonWriter writer,
                                              @NotNull Delegate<K> keyDelegate, @NotNull Delegate<V> valueDelegate,
                                              @NotNull Map<K, V> map) throws IOException {
        writer.beginArray();
        for (var entry : map.entrySet()) {
            writer.beginObject();
            writer.name("key");
            keyDelegate.write(entry.getKey(), writer);
            writer.name("value");
            valueDelegate.write(entry.getValue(), writer);
            writer.endObject();
        }
        writer.endArray();
    }
}
