/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.util;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class CollectionUtils {
    private CollectionUtils() {
        throw new UnsupportedOperationException("CollectionUtils only contains static declarations.");
    }

    public static <T> @NotNull @Unmodifiable List<T> copyOf(@NotNull List<T> list) {
        if (list.isEmpty()) {
            return List.of();
        } else {
            return List.copyOf(list);
        }
    }

    public static <T> @NotNull @Unmodifiable List<T> copyOrEmpty(@Nullable List<T> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        } else {
            return List.copyOf(list);
        }
    }

    public static <K, V> @NotNull @Unmodifiable Map<K, V> copyOf(@NotNull Map<K, V> map) {
        if (map.isEmpty()) {
            return Map.of();
        } else {
            return Map.copyOf(map);
        }
    }

    public static <K, V> @NotNull @Unmodifiable Map<K, V> copyOrEmpty(@Nullable Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return Map.of();
        } else {
            return Map.copyOf(map);
        }
    }
}
