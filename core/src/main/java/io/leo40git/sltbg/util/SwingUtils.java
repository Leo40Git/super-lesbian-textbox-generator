/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.util;

import java.awt.Window;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;

import org.jetbrains.annotations.NotNull;

public final class SwingUtils {
	private SwingUtils() {
		throw new UnsupportedOperationException("SwingUtils only contains static declarations.");
	}

	/**
	 * Original source is
	 * <a href="https://git.sleeping.town/unascribed/unsup/src/commit/2b59ada1fc14415bc5618768d5865326221146d2/src/main/java/com/unascribed/sup/Util.java#L118">here</a>.
	 * @author unascribed
	 */
	public static void fixSwing() {
		// enable a bunch of nice things that are off by default for legacy compat
		// use OpenGL or Direct3D where supported
		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.d3d", "true");
		// force font antialiasing
		//System.setProperty("awt.useSystemAAFontSettings", "on"); // causes some text to look bold on Win11?
		System.setProperty("swing.aatext", "true");
		System.setProperty("swing.useSystemFontSettings", "true");
		// only call invalidate as needed
		System.setProperty("java.awt.smartInvalidate", "true");
		// disable Metal's abuse of bold fonts
		System.setProperty("swing.boldMetal", "false");
		// always create native windows for popup menus (allows animations to play, etc)
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		// no ImageIO, I don't want you to write tons of tiny files to the disk, to be quite honest
		ImageIO.setUseCache(false);
	}

	public static void ensureNoAlwaysOnTopWindows() {
		for (var window : Window.getWindows()) {
			try {
				window.setAlwaysOnTop(false);
			} catch (Exception ignored) {}
		}
	}

	public static @NotNull Set<Window> saveAndResetAlwaysOnTopWindows() {
		var windowsThatNeedAOTSet = new HashSet<Window>();
		for (var window : Window.getWindows()) {
			try {
				if (window.isAlwaysOnTop()) {
					window.setAlwaysOnTop(false);
					windowsThatNeedAOTSet.add(window);
				}
			} catch (Exception ignored) {}
		}
		return windowsThatNeedAOTSet;
	}

	public static void restoreAlwaysOnTopWindows(@NotNull Set<Window> windowsThatNeedAOTSet) {
		for (var window : windowsThatNeedAOTSet) {
			try {
				window.setAlwaysOnTop(true);
			} catch (Exception ignored) {}
		}
		windowsThatNeedAOTSet.clear();
	}
}
