/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.ui;

import java.awt.Color;

import javax.swing.UIManager;

import io.leo40git.sltbg.swing.util.ColorUtils;

public final class UIColors {
	private UIColors() {
		throw new UnsupportedOperationException("UIColors only contains static declarations.");
	}

	public static void init() {
		Label.init();
		List.init();
	}

	public static final class Label {
		private static Color disabledForeground;

		private Label() {
			throw new UnsupportedOperationException("Label only contains static declarations.");
		}

		private static void init() {
			disabledForeground = getColor("Label.disabledForeground", "Label.disabledText");
		}

		public static Color getDisabledForeground() {
			return disabledForeground;
		}
	}

	public static final class List {
		private static Color background, foreground;
		private static Color selectionBackground;
		private static Color hoveredOverlay, disabledOverlay, alternateOverlay;
		private static Color hoveredBackground, disabledBackground, alternateBackground;

		private static void init() {
			background = getColor("List.background");
			foreground = getColor("List.foreground");
			selectionBackground = getColor("List.selectionBackground", "List[Selected].textBackground");

			hoveredOverlay = ColorUtils.withAlpha(selectionBackground, 127);
			disabledOverlay = ColorUtils.withAlpha(foreground, 191);
			alternateOverlay = ColorUtils.withAlpha(foreground, 63);

			hoveredBackground = ColorUtils.preMultiply(background, hoveredOverlay);
			disabledBackground = ColorUtils.preMultiply(background, disabledOverlay);
			alternateBackground = ColorUtils.preMultiply(background, alternateOverlay);
		}

		public static Color getBackground() {
			return background;
		}

		public static Color getAlternateBackground() {
			return alternateBackground;
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

		public static Color getHoveredBackground() {
			return hoveredBackground;
		}

		public static Color getDisabledBackground() {
			return disabledBackground;
		}

		public static Color getAlternateOverlay() {
			return alternateOverlay;
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
