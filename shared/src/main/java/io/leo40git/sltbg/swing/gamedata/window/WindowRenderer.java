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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class WindowRenderer {
    public static final int PROMPT_FRAME_COUNT = WindowPrompt.FRAME_COUNT;
    public static final int COLOR_COUNT = WindowColors.COUNT;

    private static final int SRC_IMAGE_SIZE = 128;

    private final @NotNull WindowVersion version;
    private final @NotNull WindowTone tone;
    private final @NotNull BufferedImage skinImage;

    private final WindowBackground background;
    private final WindowFrame frame;
    private final WindowPrompt prompt;
    private final WindowColors colors;

    public WindowRenderer(@NotNull WindowVersion version, @NotNull WindowTone tone, float opacity, @NotNull BufferedImage skinImage) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("opacity is out of bounds: must be between 0 and 1 (inclusive), but was %g"
                    .formatted(opacity));
        }

        int imageSize = version.scale(SRC_IMAGE_SIZE);
        if (skinImage.getWidth() != imageSize || skinImage.getHeight() != imageSize) {
            throw new IllegalArgumentException("skinImage has incorrect dimensions: expected %d x %1$d, got %d x %d"
                    .formatted(imageSize, skinImage.getWidth(), skinImage.getHeight()));
        }

        this.version = version;
        this.tone = tone;
        this.skinImage = skinImage;

        background = new WindowBackground(version, tone, opacity, skinImage);
        frame = new WindowFrame(version, skinImage);
        prompt = new WindowPrompt(version, skinImage);
        colors = new WindowColors(version, skinImage);
    }

    public @NotNull WindowVersion getVersion() {
        return version;
    }

    public @NotNull WindowTone getTone() {
        return tone;
    }

    public @NotNull BufferedImage getSkinImage() {
        return skinImage;
    }

    public int getMargin() {
        return version.getMargin();
    }

    public void paintBackground(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
        background.paint(g, x, y, width, height, observer);
    }

    public void paintBackgroundWithMargin(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
        int margin = version.getMargin();
        background.paint(g, x + margin, y + margin, width - margin * 2, height - margin * 2, observer);
    }

    public void paintFrame(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
        frame.paint(g, x, y, width, height, observer);
    }

    public int getPadding() {
        return version.getPadding();
    }

    public void paintPrompt(@NotNull Graphics g, @Range(from = 0, to = PROMPT_FRAME_COUNT - 1) int frame,
                            int x, int y, @Nullable ImageObserver observer) {
        prompt.paintFrame(g, frame, x, y, observer);
    }

    public int getPromptSize() {
        return prompt.getFrameSize();
    }

    public void paintPromptInFrame(@NotNull Graphics g, @Range(from = 0, to = PROMPT_FRAME_COUNT - 1) int frame,
                                   int x, int y, int width, int height, @Nullable ImageObserver observer) {
        int size = prompt.getFrameSize();
        x += width / 2 - size / 2;
        y += height - size;
        prompt.paintFrame(g, frame, x, y, observer);
    }

    public @NotNull Color getColor(@Range(from = 0, to = COLOR_COUNT - 1) int index) {
        return colors.get(index);
    }
}
