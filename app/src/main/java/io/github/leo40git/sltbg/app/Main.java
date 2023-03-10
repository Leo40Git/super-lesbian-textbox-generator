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

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import io.github.leo40git.sltbg.app.assets.AppAssets;
import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.parse.ControlElementRegistry;
import io.github.leo40git.sltbg.app.ui.AppFrame;
import io.github.leo40git.sltbg.app.ui.UIColors;
import io.github.leo40git.sltbg.app.util.DialogUtils;
import io.leo40git.sltbg.swing.util.UnaSwingFixes;
import io.leo40git.sltbg.swing.util.WindowUtils;
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

        logger.info("{} v{} is now initializing...", BuildInfo.APP_NAME, BuildInfo.version().toString());
        if (BuildInfo.isDevelopment()) {
            logger.info(" === DEVELOPMENT MODE! === ");
        }

        try {
            Preferences.init();
        } catch (IOException e) {
            logger.error("Failed to initialize preferences!", e);
            WindowUtils.ensureNoWindowsAlwaysOnTopNoRestore();
            JOptionPane.showMessageDialog(null, "Failed to initialize preferences!\n"
                            + DialogUtils.LOG_FILE_INSTRUCTION + "\n"
                            + "The \"%s\" file may be corrupt. Try deleting it and restarting.".formatted(Preferences.PATH),
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup, "cleanup"));

        UnaSwingFixes.apply();

        boolean useCrossPlatformLAF = false;
        if (Boolean.getBoolean("sltbg.skipSystemLAF")) {
            useCrossPlatformLAF = true;
        } else {
            String cn = UIManager.getSystemLookAndFeelClassName();
            try {
                UIManager.setLookAndFeel(cn);
            } catch (Exception e) {
                logger.error("Failed to set system L&F \"" + cn + "\", falling back to cross-platform L&F", e);
                useCrossPlatformLAF = true;

                try (var ignored = WindowUtils.ensureNoWindowsAlwaysOnTop()) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to set Swing's Look & Feel to the system Look & Feel.\n"
                                    + DialogUtils.LOG_FILE_INSTRUCTION + "\n\n"
                                    + "Using cross-platform Look & Feel instead.\n"
                                    + "(the application will *not* look \"native\"!)\n\n"
                                    + "If this issue persists, consider setting the \"sltbg.skipSystemLAF\" system property to \"true\".",
                            "Failed to set system Look & Feel", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        if (useCrossPlatformLAF) {
            String cn = UIManager.getSystemLookAndFeelClassName();
            try {
                UIManager.setLookAndFeel(cn);
            } catch (Exception e) {
                logger.error("Failed to set cross-platform L&F " + cn, e);

                WindowUtils.ensureNoWindowsAlwaysOnTopNoRestore();
                DialogUtils.showErrorDialog(null,
                        "Failed to set Swing's Look & Feel to the cross platform Look & Feel.", "Failed to set Look & Feel");
                System.exit(1);
                return;
            }
        }

        UIColors.init();

        // TODO splash screen?
        // TODO check for updates

        try {
            AppAssets.load();
        } catch (IOException e) {
            logger.error("Failed to load app assets?!", e);
            WindowUtils.ensureNoWindowsAlwaysOnTopNoRestore();
            DialogUtils.showErrorDialog(null, "Failed to load app assets!", "Failed to launch");
            System.exit(1);
            return;
        }

        try {
            GameAssets.load();
        } catch (IOException e) {
            logger.error("Failed to load game assets!", e);
            WindowUtils.ensureNoWindowsAlwaysOnTopNoRestore();
            DialogUtils.showErrorDialog(null, "Failed to load game assets!", "Failed to launch");
            System.exit(1);
            return;
        }

        ControlElementRegistry.init();

        var frame = new AppFrame();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();
    }

    // (hopefully) called by shutdown hook, so we're moments before the app dies
    private static void cleanup() {
        Preferences.flush();
    }
}
