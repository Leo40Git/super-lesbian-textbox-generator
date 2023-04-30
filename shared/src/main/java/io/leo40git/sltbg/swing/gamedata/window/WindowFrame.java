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

final class WindowFrame {
    // in essence, this is a modified "nine patch" renderer
    //  corner pieces are 16x16
    //  the center/middle pieces are longer: 16x32 for center-left/right pieces; and 32x16 for top/bottom-middle pieces
    // also, there's no center-middle piece (RPG Maker puts some menu-related arrows where it'd be),
    //  so technically this is an "eight patch" renderer

    private static final int PIECE_TL = 0;
    private static final int PIECE_TM = 1;
    private static final int PIECE_TR = 2;
    private static final int PIECE_CL = 3;
    private static final int PIECE_CR = 4;
    private static final int PIECE_BL = 5;
    private static final int PIECE_BM = 6;
    private static final int PIECE_BR = 7;
    private static final int PIECE_MAX = 8;

    private static final int SRC_START_X = 64;
    private static final int SRC_PIECE_SIZE = 16;
    private static final int SRC_MIDDLE_PIECE_WIDTH = 32, SRC_CENTER_PIECE_HEIGHT = 32;
    private static final int SRC_MIDDLE_PADDING = 32;

    private final BufferedImage skinImage;
    private final int pieceSize, pieceMWidth, pieceCHeight;
    private final int[][] pieces;

    public WindowFrame(WindowVersion version, BufferedImage skinImage) {
        this.skinImage = skinImage;

        final int startX = version.scale(SRC_START_X);
        pieceSize = version.scale(SRC_PIECE_SIZE);
        pieceMWidth = version.scale(SRC_MIDDLE_PIECE_WIDTH);
        pieceCHeight = version.scale(SRC_CENTER_PIECE_HEIGHT);
        final int midPad = version.scale(SRC_MIDDLE_PADDING);

        pieces = new int[PIECE_MAX][4];

        pieces[PIECE_TL][0] = startX; pieces[PIECE_TL][1] = 0; pieces[PIECE_TL][2] = pieceSize; pieces[PIECE_TL][3] = pieceSize;
        pieces[PIECE_TM][0] = startX + pieceSize; pieces[PIECE_TM][1] = 0; pieces[PIECE_TM][2] = pieceMWidth; pieces[PIECE_TM][3] = pieceSize;
        pieces[PIECE_TR][0] = startX + pieceSize + pieceMWidth; pieces[PIECE_TR][1] = 0; pieces[PIECE_TR][2] = pieceSize; pieces[PIECE_TR][3] = pieceSize;

        pieces[PIECE_CL][0] = startX; pieces[PIECE_CL][1] = pieceSize; pieces[PIECE_CL][2] = pieceSize; pieces[PIECE_CL][3] = pieceCHeight;
        pieces[PIECE_CR][0] = startX + pieceSize + midPad; pieces[PIECE_CR][1] = pieceSize; pieces[PIECE_CR][2] = pieceSize; pieces[PIECE_CR][3] = pieceCHeight;

        pieces[PIECE_BL][0] = startX; pieces[PIECE_BL][1] = pieceSize + pieceCHeight; pieces[PIECE_BL][2] = pieceSize; pieces[PIECE_BL][3] = pieceSize;
        pieces[PIECE_BM][0] = startX + pieceSize; pieces[PIECE_BM][1] = pieceSize + pieceCHeight; pieces[PIECE_BM][2] = pieceMWidth; pieces[PIECE_BM][3] = pieceSize;
        pieces[PIECE_BR][0] = startX + pieceSize + pieceMWidth; pieces[PIECE_BR][1] = pieceSize + pieceCHeight; pieces[PIECE_BR][2] = pieceSize; pieces[PIECE_BR][3] = pieceSize;
    }

    public void paint(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        final int rightX = x + width - pieceSize;

        drawPiece(g, PIECE_TL, x, y, pieceSize, pieceSize, observer);
        drawPiece(g, PIECE_TM, x + pieceSize, y, width - pieceMWidth, pieceSize, observer);
        drawPiece(g, PIECE_TR, rightX, y, pieceSize, pieceSize, observer);

        final int centerY = y + pieceSize;
        final int centerHeight = height - pieceCHeight;
        drawPiece(g, PIECE_CL, x, centerY, pieceSize, centerHeight, observer);
        drawPiece(g, PIECE_CR, rightX, centerY, pieceSize, centerHeight, observer);

        final int bottomY = height - pieceSize;
        drawPiece(g, PIECE_BL, x, bottomY, pieceSize, pieceSize, observer);
        drawPiece(g, PIECE_BM, x + pieceSize, bottomY, width - pieceMWidth, pieceSize, observer);
        drawPiece(g, PIECE_BR, rightX, bottomY, pieceSize, pieceSize, observer);
    }

    private void drawPiece(Graphics g, int i, int x, int y, int width, int height, ImageObserver observer) {
        final int[] piece = pieces[i];
        g.drawImage(skinImage,
                x, y, x + width, x + height,
                piece[0], piece[1], piece[0] + piece[2], piece[1] + piece[3],
                observer);
    }
}
