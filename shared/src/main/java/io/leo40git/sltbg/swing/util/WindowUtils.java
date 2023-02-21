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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WindowUtils {
	private WindowUtils() {
		throw new UnsupportedOperationException("WindowUtils only contains static declarations.");
	}

	public static void ensureNoWindowsAlwaysOnTopNoRestore() {
		for (var window : Window.getWindows()) {
			try {
				window.setAlwaysOnTop(false);
			} catch (Exception ignored) {}
		}
	}

	@Contract(" -> new")
	public static @NotNull AlwaysOnTopWindowRestorer ensureNoWindowsAlwaysOnTop() {
		final var windows = new HashSet<Window>();
		for (var window : Window.getWindows()) {
			try {
				if (window.isAlwaysOnTop()) {
					window.setAlwaysOnTop(false);
					windows.add(window);
				}
			} catch (Exception ignored) {}
		}
		return new AlwaysOnTopWindowRestorer(windows);
	}

	public static final class AlwaysOnTopWindowRestorer implements AutoCloseable {
		private HashSet<Window> windows;

		private AlwaysOnTopWindowRestorer(HashSet<Window> windows) {
			this.windows = windows;
		}

		private void addWindow(@NotNull Window window) {
			if (windows == null) {
				throw new IllegalStateException("Already closed!");
			}

			windows.add(window);
		}

		@Override
		public void close() {
			if (windows == null) {
				throw new IllegalStateException("Already closed!");
			}

			for (var window : windows) {
				try {
					window.setAlwaysOnTop(true);
				} catch (Exception ignored) {}
			}

			windows.clear();
			windows = null;
		}
	}
}
