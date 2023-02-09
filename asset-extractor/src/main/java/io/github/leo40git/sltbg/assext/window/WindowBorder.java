package io.github.leo40git.sltbg.assext.window;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WindowBorder {
	// in essence, this is a modified "nine patch" renderer
	//  corner pieces are 16x16
	//  the center/middle pieces are longer: 16x32 for center-left/right pieces; and 32x16 for top/bottom-middle pieces
	// also, there's no center-middle piece (RPG Maker puts some menu-related arrows where it'd be),
	//  so technically this is an "eight patch" renderer

	public static final int PIECE_SIZE = 16;
	public static final int PIECE_MIDDLE_WIDTH = 32, PIECE_CENTER_HEIGHT = 32;

	private final BufferedImage[] pieces = new BufferedImage[PIECE_MAX];
	private static final int PIECE_TL = 0;
	private static final int PIECE_TM = 1;
	private static final int PIECE_TR = 2;
	private static final int PIECE_CL = 3;
	private static final int PIECE_CR = 4;
	private static final int PIECE_BL = 5;
	private static final int PIECE_BM = 6;
	private static final int PIECE_BR = 7;
	private static final int PIECE_MAX = 8;

	public WindowBorder(@NotNull BufferedImage window) {
		final int startX = 64;
		final int middlePadding = 32; // padding between PIECE_CL's end and PIECE_CR's start

		pieces[PIECE_TL] = window.getSubimage(startX, 0, PIECE_SIZE, PIECE_SIZE);
		pieces[PIECE_TM] = window.getSubimage(startX + PIECE_SIZE, 0, PIECE_MIDDLE_WIDTH, PIECE_SIZE);
		pieces[PIECE_TR] = window.getSubimage(startX + PIECE_SIZE + PIECE_MIDDLE_WIDTH, 0, PIECE_SIZE, PIECE_SIZE);

		pieces[PIECE_CL] = window.getSubimage(startX, PIECE_SIZE, PIECE_SIZE, PIECE_CENTER_HEIGHT);
		pieces[PIECE_CR] = window.getSubimage(startX + PIECE_SIZE + middlePadding, PIECE_SIZE, PIECE_SIZE, PIECE_CENTER_HEIGHT);

		pieces[PIECE_BL] = window.getSubimage(startX, PIECE_SIZE + PIECE_CENTER_HEIGHT, PIECE_SIZE, PIECE_SIZE);
		pieces[PIECE_BM] = window.getSubimage(startX + PIECE_SIZE, PIECE_SIZE + PIECE_CENTER_HEIGHT, PIECE_MIDDLE_WIDTH, PIECE_SIZE);
		pieces[PIECE_BR] = window.getSubimage(startX + PIECE_SIZE + PIECE_MIDDLE_WIDTH, PIECE_SIZE + PIECE_CENTER_HEIGHT, PIECE_SIZE, PIECE_SIZE);
	}

	public void draw(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
		// TOP
		g.drawImage(pieces[PIECE_TL], x, y, observer);
		g.drawImage(pieces[PIECE_TM], x + PIECE_SIZE, y, width - PIECE_MIDDLE_WIDTH, PIECE_SIZE, observer);
		g.drawImage(pieces[PIECE_TR], x + width - PIECE_SIZE, y, observer);
		// CENTER
		g.drawImage(pieces[PIECE_CL], x, y + PIECE_SIZE, PIECE_SIZE, height - PIECE_CENTER_HEIGHT, observer);
		g.drawImage(pieces[PIECE_CR], x + width - PIECE_SIZE, y + PIECE_SIZE, PIECE_SIZE, height - PIECE_CENTER_HEIGHT, observer);
		// BOTTOM
		g.drawImage(pieces[PIECE_BL], x, y + height - PIECE_SIZE, observer);
		g.drawImage(pieces[PIECE_BM], x + PIECE_SIZE, y + height - PIECE_SIZE, width - PIECE_MIDDLE_WIDTH, PIECE_SIZE, observer);
		g.drawImage(pieces[PIECE_BR], x + width - PIECE_SIZE, y + height - PIECE_SIZE, observer);
	}
}
