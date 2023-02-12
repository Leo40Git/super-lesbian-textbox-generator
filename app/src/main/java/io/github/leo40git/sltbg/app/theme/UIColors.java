/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.theme;

import java.awt.Color;

import javax.swing.UIManager;

import io.github.leo40git.sltbg.app.util.MoreColors;

public final class UIColors {
	private UIColors() {
		throw new UnsupportedOperationException("UIColors only contains static declarations.");
	}

	public static void update() {
		Label.update();
		List.update();
	}

	public static final class Label {
		private static Color disabledForeground;

		private Label() {
			throw new UnsupportedOperationException("Label only contains static declarations.");
		}

		private static void update() {
			disabledForeground = getColor("Label.disabledForeground", "Label.disabledText");
		}

		public static Color getDisabledForeground() {
			return disabledForeground;
		}
	}

	public static final class List {
		private static Color background, foreground;
		private static Color selectionBackground;
		private static Color hoveredOverlay, disabledOverlay;

		private static void update() {
			background = getColor("List.background");
			foreground = getColor("List.foreground");
			selectionBackground = getColor("List.selectionBackground", "List[Selected].textBackground");
			hoveredOverlay = MoreColors.withAlpha(selectionBackground, 127);
			disabledOverlay = MoreColors.withAlpha(foreground, 127);
		}

		public static Color getBackground() {
			return background;
		}

		public static Color getForeground() {
			return foreground;
		}

		public static Color getSelectionBackground() {
			return selectionBackground;
		}

		public static Color getHoveredOverlay() {
			return hoveredOverlay;
		}

		public static Color getDisabledOverlay() {
			return disabledOverlay;
		}
	}

	private static Color getColor(Object key) {
		return UIManager.getColor(key);
	}

	private static Color getColor(String key, String... fallbackKeys) {
		var c = UIManager.getColor(key);
		if (c == null) {
			for (String fallbackKey : fallbackKeys) {
				c = UIManager.getColor(fallbackKey);
				if (c != null) {
					break;
				}
			}
		}
		return c;
	}
}
