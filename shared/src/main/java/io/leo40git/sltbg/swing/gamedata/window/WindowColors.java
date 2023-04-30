/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.window;

import java.awt.Color;
import java.awt.image.BufferedImage;

final class WindowColors {
    // this is simple: there are 32 colored squares on the Window sheet,
    //  these directly map to the available 32 preset colors

    public static final int COUNT = 32;
    private final Color[] colors;

    private static final int SRC_START_X = 64;
    private static final int SRC_START_Y = 96;
    private static final int SRC_SIZE = 8;

    public WindowColors(WindowVersion version, BufferedImage skinImage) {
        colors = new Color[COUNT];

        final int size = version.scale(SRC_SIZE);
        final int startX = version.scale(SRC_START_X);
        int y = version.scale(SRC_START_Y);

        int x = startX;
        for (int i = 0; i < COUNT; i++) {
            colors[i] = new Color(skinImage.getRGB(x, y), false);
            x += size;
            if (x >= skinImage.getWidth()) {
                x = startX;
                y += size;
            }
        }
    }

    public Color get(int i) {
        return colors[i];
    }
}
