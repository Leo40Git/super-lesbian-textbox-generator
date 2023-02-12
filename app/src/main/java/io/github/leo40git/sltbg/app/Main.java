/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import io.github.leo40git.sltbg.app.assets.AppAssets;
import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.theme.UIColors;
import io.github.leo40git.sltbg.app.theme.UITheme;
import io.github.leo40git.sltbg.app.ui.AppFrame;
import io.github.leo40git.sltbg.app.util.DialogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
	private Main() {
		throw new UnsupportedOperationException("Main only contains static declarations.");
	}

	private static Logger logger;

	public static Logger logger() {
		return logger;
	}

	public static void main(String[] args) {
		try {
			BuildInfo.load();
		} catch (Exception e) {
			System.err.println("Failed to load build info! This build is probably hosed!");
			e.printStackTrace();
			System.exit(1);
			return;
		}

		try {
			logger = LogManager.getLogger("main");
		} catch (Exception e) {
			System.err.println("Failed to initialize logger!");
			e.printStackTrace();
			System.exit(1);
			return;
		}

		logger.info("{} v{} is now initializing...", BuildInfo.name(), BuildInfo.version().toString());
		if (BuildInfo.isDevelopment()) {
			logger.info(" === DEVELOPMENT MODE! === ");
		}

		try {
			Preferences.init();
		} catch (IOException e) {
			logger.error("Failed to initialize preferences!", e);
			DialogUtils.ensureNoAlwaysOnTopWindows();
			JOptionPane.showMessageDialog(null, "Failed to initialize preferences!\n"
					+ DialogUtils.LOG_FILE_INSTRUCTION + "\n"
					+ "The \"%s\" file may be corrupt. Try deleting it and restarting.".formatted(Preferences.PATH),
					"Failed to launch", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup, "cleanup"));

		fixSwing();

		UITheme.init();
		UITheme theme = null;

		if (Preferences.getThemeName() != null) {
			try {
				theme = UITheme.getTheme(Preferences.getThemeName());
			} catch (IllegalArgumentException e) {
				var windowsThatNeedAOTSet = DialogUtils.saveAndResetAlwaysOnTopWindows();
				JOptionPane.showMessageDialog(null,
						"Couldn't find theme with name \"" + Preferences.getThemeName() + "\"!\n"
								+ "Using default theme instead.",
						"Invalid theme name", JOptionPane.ERROR_MESSAGE);
				DialogUtils.restoreAlwaysOnTopWindows(windowsThatNeedAOTSet);
			}
		}

		if (theme == null) {
			theme = Boolean.getBoolean("skipSystemLookAndFeel") ? UITheme.getCrossPlatformTheme() : UITheme.getSystemTheme();
		}

		if (theme.apply()) {
			Preferences.setThemeName(theme.getName());
		} else {
			var windowsThatNeedAOTSet = DialogUtils.saveAndResetAlwaysOnTopWindows();
			JOptionPane.showMessageDialog(null,
					"Failed to set theme \"" + theme.getName() + "\"!\n"
							+ DialogUtils.LOG_FILE_INSTRUCTION + "\n"
							+ "Using default Swing theme instead.",
					"Failed to set theme!", JOptionPane.ERROR_MESSAGE);
			DialogUtils.restoreAlwaysOnTopWindows(windowsThatNeedAOTSet);
		}

		UIColors.update();

		// TODO splash screen?
		// TODO check for updates

		try {
			AppAssets.load();
		} catch (IOException e) {
			logger.error("Failed to load app assets?!", e);
			DialogUtils.ensureNoAlwaysOnTopWindows();
			DialogUtils.showErrorDialog(null, "Failed to load app assets!", "Failed to launch");
			System.exit(1);
			return;
		}

		try {
			GameAssets.load();
		} catch (IOException e) {
			logger.error("Failed to load game assets!", e);
			DialogUtils.ensureNoAlwaysOnTopWindows();
			DialogUtils.showErrorDialog(null, "Failed to load game assets!", "Failed to launch");
			System.exit(1);
			return;
		}

		var frame = new AppFrame();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.requestFocus();
	}

	// (hopefully) called by shutdown hook, so we're moments before the app dies
	private static void cleanup() {
		Preferences.flush();
	}

	/**
	 * Original source is
	 * <a href="https://git.sleeping.town/unascribed/unsup/src/branch/trunk/src/main/java/com/unascribed/sup/Util.java#L77">here</a>.
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
}
