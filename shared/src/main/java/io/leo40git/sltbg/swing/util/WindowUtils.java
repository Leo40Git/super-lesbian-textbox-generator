/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.util;

import java.awt.Window;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public final class WindowUtils {
	private WindowUtils() {
		throw new UnsupportedOperationException("WindowUtils only contains static declarations.");
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
