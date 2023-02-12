/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.theme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import io.github.leo40git.sltbg.app.Main;
import org.jetbrains.annotations.NotNull;

public sealed class UITheme {
	private static final Map<String, UITheme> THEMES = new LinkedHashMap<>();
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
	private static volatile UITheme currentTheme;
	private static UITheme crossPlatformTheme, systemTheme;

	private static final String METAL_LAF_CLASS = "javax.swing.plaf.metal.MetalLookAndFeel";
	private static final MetalTheme[] METAL_THEMES = { new OceanTheme(), new DefaultMetalTheme() };

	public static void init() {
		if (INITIALIZED.compareAndExchange(false, true)) {
			return;
		}

		final String currentLAF = UIManager.getLookAndFeel().getClass().getName();
		var themes = new HashMap<String, UITheme>();

		for (var info : UIManager.getInstalledLookAndFeels()) {
			if (METAL_LAF_CLASS.equals(info.getClassName())) {
				// add different metal themes
				boolean isCurrentTheme = METAL_LAF_CLASS.equals(currentLAF);
				final var currentMetalTheme = MetalLookAndFeel.getCurrentTheme();
				for (var metalTheme : METAL_THEMES) {
					var theme = new UITheme.Metal(info.getName(), METAL_LAF_CLASS, metalTheme);
					if (isCurrentTheme && currentMetalTheme.getClass() == metalTheme.getClass()) {
						currentTheme = theme;
					}
					themes.put(theme.getName(), theme);
				}
			} else {
				var theme = new UITheme(info.getName(), info.getClassName());
				if (currentLAF.equals(info.getClassName())) {
					currentTheme = theme;
				}
				themes.put(theme.getName(), theme);
			}
		}

		var names = new ArrayList<>(themes.keySet());
		names.sort(Comparator.naturalOrder());
		for (var name : names) {
			THEMES.put(name, themes.get(name));
		}
	}

	public static Collection<UITheme> getAllThemes() {
		return THEMES.values();
	}

	public static @NotNull UITheme getTheme(String name) {
		var theme = THEMES.get(name);
		if (theme == null) {
			throw new IllegalArgumentException("Couldn't find theme \"" + name + "\"!");
		}
		return theme;
	}

	public static @NotNull UITheme getThemeByClassName(String className) {
		final String metalThemeProp = System.getProperty("swing.metalTheme");
		for (var theme : THEMES.values()) {
			if (className.equals(theme.getClassName())) {
				// if we found a Metal theme, make sure it matches the property
				if (theme instanceof Metal metalTheme && !metalTheme.matchesProperty(metalThemeProp)) {
					continue;
				}
				return theme;
			}
		}
		throw new IllegalArgumentException("Couldn't find theme with class name \"" + className + "\"!");
	}

	public static UITheme getCrossPlatformTheme() {
		if (crossPlatformTheme == null) {
			crossPlatformTheme = getThemeByClassName(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		return crossPlatformTheme;
	}

	public static UITheme getSystemTheme() {
		if (systemTheme == null) {
			systemTheme = getThemeByClassName(UIManager.getSystemLookAndFeelClassName());
		}
		return systemTheme;
	}

	public static UITheme getCurrentTheme() {
		return currentTheme;
	}

	protected final String name, className;

	protected UITheme(String name, String className) {
		this.name = name;
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	public boolean apply() {
		try {
			UIManager.setLookAndFeel(className);
			currentTheme = this;
			return true;
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
				IllegalAccessException e) {
			Main.logger().error("Failed to apply theme!", e);
			return false;
		}
	}

	@Override
	public String toString() {
		return name;
	}

	private static final class Metal extends UITheme {
		private final MetalTheme metalTheme;

		public Metal(String name, String className, MetalTheme metalTheme) {
			super(name + " (" + metalTheme.getName() + ")", className);
			this.metalTheme = metalTheme;
		}

		public boolean matchesProperty(String metalThemeProp) {
			if ("steel".equals(metalThemeProp)) {
				return metalTheme.getClass() == DefaultMetalTheme.class;
			} else {
				return metalTheme.getClass() == OceanTheme.class;
			}
		}

		@Override
		public boolean apply() {
			MetalLookAndFeel.setCurrentTheme(metalTheme);
			return super.apply();
		}
	}
}
