/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public sealed class FacePalette permits NamedFacePalette {
    public static final long DEFAULT_ORDER_BASE = 1000;

    public static long getNextOrder(long order) {
        order += DEFAULT_ORDER_BASE;
        long rem = Math.abs(order % DEFAULT_ORDER_BASE);
        if (rem > 0) {
            order += DEFAULT_ORDER_BASE - rem;
        }
        return order;
    }

    private final @NotNull ArrayList<FaceCategory> categories;
    private final @NotNull HashMap<String, FaceCategory> categoriesLookup;

    private long lastOrder = DEFAULT_ORDER_BASE;
    private volatile boolean needsSort = false;

    public FacePalette() {
        categories = new ArrayList<>();
        categoriesLookup = new HashMap<>();
    }

    public FacePalette(int initialCapacity) {
        categories = new ArrayList<>(initialCapacity);
        categoriesLookup = new HashMap<>(initialCapacity);
    }

    /**
     * This constructor "takes ownership" of the {@literal categories} list -
     * <em>you should not modify this list after passing it to this constructor!</em>
     */
    public FacePalette(@NotNull ArrayList<FaceCategory> categories) {
        this.categories = categories;
        categoriesLookup = new HashMap<>(categories.size());

        for (var category : categories) {
            if (categoriesLookup.put(category.getName(), category) != null) {
                throw new IllegalArgumentException("categories contains 2 elements with the same name: '%s'"
                        .formatted(category.getName()));
            }
            category.onAddedToPalette(this);
        }
    }

    void markDirty() {
        needsSort = true;
    }

    public void sortIfNeeded() {
        if (needsSort) {
            synchronized (categories) {
                categories.sort(Comparator.naturalOrder());
            }

            needsSort = false;
        }
    }

    public @NotNull @UnmodifiableView List<FaceCategory> getCategories() {
        sortIfNeeded();
        return Collections.unmodifiableList(categories);
    }

    public @Nullable FaceCategory getCategory(@NotNull String name) {
        return categoriesLookup.get(name);
    }

    public @Nullable Face getFace(@NotNull String path) {
        int delIdx = path.indexOf(Face.PATH_DELIMITER);
        if (delIdx < 0) {
            throw new IllegalArgumentException("Path \"%s\" is missing delimiter '%s'".formatted(path, Face.PATH_DELIMITER));
        }

        var category = getCategory(path.substring(0, delIdx));
        if (category == null) {
            return null;
        }

        return category.getFace(path.substring(delIdx + 1));
    }

    public void add(@NotNull FaceCategory category) {
        if (category.getPalette() != null) {
            throw new IllegalArgumentException("Category is already part of other palette");
        }

        synchronized (categories) {
            if (categoriesLookup.put(category.getName(), category) != null) {
                throw new IllegalArgumentException("Category with name \"" + category.getName() + "\" already exists in this palette");
            }

            categories.add(category);
            category.onAddedToPalette(this);

            if (category.isOrderSet()) {
                if (category.getOrder() > lastOrder) {
                    lastOrder = category.getOrder();
                }
            } else {
                category.setOrder(lastOrder = getNextOrder(lastOrder));
            }
        }
    }

    void rename(@NotNull FaceCategory category, @NotNull String newName) {
        synchronized (categories) {
            if (categoriesLookup.containsKey(newName)) {
                throw new IllegalArgumentException("Category with name \"" + newName + "\" already exists in this palette");
            }

            categoriesLookup.remove(category.getName(), category);
            categoriesLookup.put(newName, category);
        }
    }

    public boolean contains(@NotNull String category) {
        synchronized (categories) {
            return categoriesLookup.containsKey(category);
        }
    }

    protected void remove0(@NotNull FaceCategory category) {
        category.onRemovedFromPalette();
        markDirty();
    }

    public boolean remove(@NotNull FaceCategory category) {
        synchronized (categories) {
            if (categories.remove(category)) {
                categoriesLookup.remove(category.getName());
                remove0(category);
                return true;
            } else {
                return false;
            }
        }
    }

    public @Nullable FaceCategory remove(@NotNull String name) {
        final FaceCategory category;

        synchronized (categories) {
            category = categoriesLookup.remove(name);
            if (category == null) {
                return null;
            }

            remove0(category);
        }

        return category;
    }

    public void clear() {
        synchronized (categories) {
            for (var category : categories) {
                category.onRemovedFromPalette();
            }

            categories.clear();
            categoriesLookup.clear();
        }

        needsSort = false;
    }
}
