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

public sealed class FacePool permits NamedFacePool {
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
    private @Nullable FaceCategory lastCategory;
    private volatile boolean needsSort;

    private FacePool(@NotNull ArrayList<FaceCategory> categories, @NotNull HashMap<String, FaceCategory> categoriesLookup) {
        this.categories = categories;
        this.categoriesLookup = categoriesLookup;

        lastCategory = null;
        needsSort = false;
    }

    public FacePool() {
        this(new ArrayList<>(), new HashMap<>());
    }

    public FacePool(int initialCapacity) {
        this(new ArrayList<>(initialCapacity), new HashMap<>(initialCapacity));
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
        if (category.getPool() != null) {
            throw new IllegalArgumentException("Category is already part of other pool");
        }

        synchronized (categories) {
            if (categoriesLookup.put(category.getName(), category) != null) {
                throw new IllegalArgumentException("Category with name \"" + category.getName() + "\" already exists in this pool");
            }

            categories.add(category);
            category.onAddedToPool(this);

            if (!category.isOrderSet()) {
                if (lastCategory != null) {
                    category.setOrder(FacePool.getNextOrder(lastCategory.getOrder()));
                } else {
                    category.setOrder(FacePool.DEFAULT_ORDER_BASE);
                }
            }
            lastCategory = category;
        }
    }

    void rename(@NotNull FaceCategory category, @NotNull String newName) {
        synchronized (categories) {
            if (categoriesLookup.containsKey(newName)) {
                throw new IllegalArgumentException("Category with name \"" + newName + "\" already exists in this pool");
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

    private void remove0(@NotNull FaceCategory category) {
        category.onRemovedFromPool();

        boolean doMarkDirty = true;

        if (lastCategory == category) {
            if (!categories.isEmpty()) {
                categories.sort(Comparator.naturalOrder());
                doMarkDirty = needsSort = false;
                lastCategory = categories.get(categories.size() - 1);
            } else {
                lastCategory = null;
            }
        }

        if (doMarkDirty) {
            markDirty();
        }
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
                category.onRemovedFromPool();
            }

            categories.clear();
            categoriesLookup.clear();
            lastCategory = null;
        }

        needsSort = false;
    }
}
