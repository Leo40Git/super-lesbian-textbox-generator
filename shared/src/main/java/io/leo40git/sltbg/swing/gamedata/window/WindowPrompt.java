/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.window;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import io.leo40git.sltbg.gamedata.window.WindowVersion;

final class WindowPrompt {
    // this animated prompt is drawn on the bottom of the window horizontally centered
    //  when there's no more text to display, and the next input will begin the next textbox
    // TODO document animation speed

    public static final int FRAME_COUNT = 4;

    private static final int SRC_FRAME_SIZE = 16;
    private static final int SRC_START_X = 96;
    private static final int SRC_START_Y = 64;

    private WindowVersion version;
    private BufferedImage skinImage;

    private int frameSize;
    private final int[][] frameOrigins;

    public WindowPrompt(WindowVersion version, BufferedImage skinImage) {
        this.version = version;
        this.skinImage = skinImage;

        frameOrigins = new int[FRAME_COUNT][2];
        initFrames();
    }

    private void initFrames() {
        frameSize = version.scale(SRC_FRAME_SIZE);

        final int startX = version.scale(SRC_START_X);
        int y = version.scale(SRC_START_Y);

        int x = startX;
        for (int i = 0; i < FRAME_COUNT; i++) {
            int[] frameOrigin = frameOrigins[i];
            frameOrigin[0] = x;
            frameOrigin[1] = y;

            x += frameSize;
            if (x >= skinImage.getWidth()) {
                x = startX;
                y += frameSize;
            }
        }
    }

    public void setSkin(WindowVersion version, BufferedImage skinImage) {
        this.version = version;
        this.skinImage = skinImage;
        initFrames();
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void paintFrame(Graphics g, int i, int x, int y, ImageObserver observer) {
        int[] frameOrigin = frameOrigins[i];
        g.drawImage(skinImage,
                x, y, x + frameSize, y + frameSize,
                frameOrigin[0], frameOrigin[1], frameOrigin[0] + frameSize, frameOrigin[1] + frameSize,
                observer);
    }
}
