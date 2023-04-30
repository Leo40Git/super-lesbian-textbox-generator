/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

// TODO replace with rich version that supports icon names
@Deprecated
public final class IconSet {
    public static final int ICON_SIZE = 24;

    private final @NotNull BufferedImage sheet;
    private final int iconsPerRow, maximumIconIndex;

    public IconSet(@NotNull BufferedImage sheet) {
        if (sheet.getWidth() % ICON_SIZE != 0 || sheet.getHeight() % ICON_SIZE != 0) {
            throw new IllegalArgumentException("sheet's dimensions (%d x %d) are not divisible by icon size (%d)"
                    .formatted(sheet.getWidth(), sheet.getHeight(), ICON_SIZE));
        }

        this.sheet = sheet;

        iconsPerRow = sheet.getWidth() / ICON_SIZE;
        maximumIconIndex = (sheet.getHeight() / ICON_SIZE) * iconsPerRow - 1;
    }

    public @NotNull BufferedImage getSheet() {
        return sheet;
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int getIconsPerRow() {
        return iconsPerRow;
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaximumIconIndex() {
        return maximumIconIndex;
    }

    public boolean isValidIconIndex(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return index <= maximumIconIndex;
    }

    public void drawIcon(@NotNull Graphics g, int x, int y, @Range(from = 0, to = Integer.MAX_VALUE) int index, @Nullable ImageObserver observer) {
        if (index > maximumIconIndex) {
            throw new IndexOutOfBoundsException(index);
        }

        int sx = (index % iconsPerRow) * ICON_SIZE;
        int sy = (index / iconsPerRow) * ICON_SIZE;

        g.drawImage(sheet,
                x, y, x + ICON_SIZE, y + ICON_SIZE,
                sx, sy, sx + ICON_SIZE, sy + ICON_SIZE,
                observer);
    }

    @Contract("_ -> new")
    public @NotNull BufferedImage getIconImage(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        if (index > maximumIconIndex) {
            throw new IndexOutOfBoundsException(index);
        }

        int sx = (index % iconsPerRow) * ICON_SIZE;
        int sy = (index / iconsPerRow) * ICON_SIZE;

        return sheet.getSubimage(sx, sy, ICON_SIZE, ICON_SIZE);
    }
}
