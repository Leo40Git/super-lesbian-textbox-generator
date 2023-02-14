/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.util;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DialogUtils {
	private DialogUtils() {
		throw new UnsupportedOperationException("DialogUtils only contains static declarations.");
	}

	public static final String LOG_FILE_INSTRUCTION = "See \"app.log\" for more details.";

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

	public static void showErrorDialog(Component parent, String message, String title) {
		JOptionPane.showMessageDialog(parent,
				message + "\n" + LOG_FILE_INSTRUCTION,
				title, JOptionPane.ERROR_MESSAGE);
	}

	public static int showCustomConfirmDialog(Component parent, Object message, String title, String[] options,
			@MagicConstant(intValues = { JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, JOptionPane.INFORMATION_MESSAGE, JOptionPane.WARNING_MESSAGE, JOptionPane.QUESTION_MESSAGE })
			int messageType) {
		return JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION, messageType,
				null, options, null);
	}

	public static final FileNameExtensionFilter FILTER_IMAGE_FILES
			= new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
	public static final FileNameExtensionFilter FILTER_JSON_FILES
			= new FileNameExtensionFilter("JSON files", "json5", "json");

	private static final JFileChooser FC_OPEN = createFileChooser(), FC_SAVE = createFileChooser();
	private static final JFileChooser FC_OPEN_FOLDER = createFolderChooser();

	private static @NotNull JFileChooser createFileChooser() {
		var fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		return fc;
	}

	private static @NotNull JFileChooser createFolderChooser() {
		var fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return fc;
	}

	public static @Nullable File fileOpenDialog(Component parent, String title, FileFilter filter) {
		FC_OPEN.setDialogTitle(title);
		FC_OPEN.setFileFilter(filter);
		final int ret = FC_OPEN.showOpenDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return FC_OPEN.getSelectedFile();
		}
		return null;
	}

	public static @Nullable File fileSaveDialog(Component parent, String title, FileNameExtensionFilter filter) {
		FC_SAVE.setDialogTitle(title);
		FC_SAVE.setFileFilter(filter);
		final int ret = FC_SAVE.showSaveDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File sel = FC_SAVE.getSelectedFile();
			String selName = sel.getName();
			String ext = filter.getExtensions()[0];
			if (!selName.contains(".")
					|| !selName.substring(selName.lastIndexOf(".") + 1).equalsIgnoreCase(ext)) {
				selName += "." + ext;
				sel = new File(sel.getParentFile().getPath() + "/" + selName);
			}
			return sel;
		}
		return null;
	}

	public static @Nullable File folderOpenDialog(Component parent, String title) {
		FC_OPEN_FOLDER.setDialogTitle(title);
		final int ret = FC_OPEN_FOLDER.showOpenDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return FC_OPEN_FOLDER.getSelectedFile();
		}
		return null;
	}

	public static @Nullable String showMultilineInputDialog(Component parent, String message, String title, int messageType,
			@Nullable String defaultValue) {
		final JTextArea textArea = new JTextArea(5, 10);
		textArea.setText(defaultValue);
		textArea.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (textArea.isShowing()) {
						textArea.removeHierarchyListener(this);
						SwingUtilities.invokeLater(textArea::requestFocus);
					}
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(textArea);
		if (JOptionPane.showOptionDialog(parent, new Object[] { message, scrollPane }, title,
				JOptionPane.OK_CANCEL_OPTION, messageType, null, null, textArea) == JOptionPane.OK_OPTION) {
			return textArea.getText();
		} else {
			return null;
		}
	}
}
