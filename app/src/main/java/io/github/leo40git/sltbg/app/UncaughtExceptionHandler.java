/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app;

import javax.swing.JOptionPane;

import io.github.leo40git.sltbg.app.util.DialogUtils;
import io.leo40git.sltbg.swing.util.WindowUtils;

public final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static final String[] OPTIONS = { "Continue", "Abort" };
	private static final int OPTION_CONTINUE = 0;
	private static final int OPTION_ABORT = 1;

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Main.logger().error("Uncaught exception in thread \"" + t.getName() + "\"", e);
		var windowsThatNeedAOTSet = WindowUtils.saveAndResetAlwaysOnTopWindows();
		int option = DialogUtils.showCustomConfirmDialog(null,
				"An uncaught exception has occurred!\n" + DialogUtils.LOG_FILE_INSTRUCTION,
				"Uncaught Exception!", OPTIONS, JOptionPane.ERROR_MESSAGE);
		switch (option) {
			case OPTION_CONTINUE -> WindowUtils.restoreAlwaysOnTopWindows(windowsThatNeedAOTSet);
			case OPTION_ABORT -> System.exit(1);
		}
	}
}
