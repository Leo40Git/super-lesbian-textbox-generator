/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.components;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import io.leo40git.sltbg.util.FileUtils;
import org.jetbrains.annotations.Contract;

public class ConfirmFileChooser extends JFileChooser {
	/**
	 * Enables {@linkplain #setAlternateDirectoryWarningShown(boolean) the alternate directory warning}.
	 */
	public static final String ALTERNATE_DIRECTORY_WARNING_SHOWN_CHANGED_PROPERTY = "AlternateDirectoryWarningShownChangedProperty";

	private boolean alternateDirectoryWarningShown;

	/**
	 * Constructs a <code>ConfirmFileChooser</code> pointing to the user's
	 * default directory.
	 * <p>
	 * This default depends on the operating system.<br />
	 * It is typically the "My Documents" folder on Windows, and the
	 * user's home directory on Unix.
	 */
	public ConfirmFileChooser() {
		super.setMultiSelectionEnabled(false);
		alternateDirectoryWarningShown = false;
	}

	/**
	 * Returns true if the alternate directory warning should be shown.
	 * @return true if the alternate directory warning should be shown
	 * @see #setAlternateDirectoryWarningShown(boolean)
	 */
	public boolean isAlternateDirectoryWarningShown() {
		return alternateDirectoryWarningShown;
	}

	/**
	 * Sets the file chooser to enable the alternate directory warning.
	 * <p>
	 * Normally, the file chooser warns that files inside non-empty directories <em>may</em> be replaced or removed.
	 * <br />
	 * If the alternate directory warning is enabled,
	 * this warning will state that files <em>will</em> be replaced or removed.
	 * @param b true if the alternate directory warning should be shown
	 * @see #isAlternateDirectoryWarningShown()
	 */
	public void setAlternateDirectoryWarningShown(boolean b) {
		if (alternateDirectoryWarningShown != b) {
			boolean oldValue = alternateDirectoryWarningShown;
			alternateDirectoryWarningShown = b;
			firePropertyChange(ALTERNATE_DIRECTORY_WARNING_SHOWN_CHANGED_PROPERTY, oldValue, b);
		}
	}

	/**
	 * <code>ConfirmFileChooser</code> does not support multiple file selection.
	 * @param b should always be false
	 * @throws UnsupportedOperationException if <code>b</code> is true.
	 */
	@Override
	@Contract("true -> fail")
	public void setMultiSelectionEnabled(boolean b) {
		if (b) {
			throw new UnsupportedOperationException("ConfirmFileChooser does not support multiple selections.");
		}
		
		super.setMultiSelectionEnabled(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void approveSelection() {
		if (getDialogType() == SAVE_DIALOG) {
			var file = getSelectedFile();
			if (file != null && file.exists()) {
				if (file.isDirectory()) {
					String message;

					try {
						if (FileUtils.isEmptyDirectory(file.toPath())) {
							message = file.getName() + " isn't empty.";
						} else {
							super.approveSelection();
							return;
						}
					} catch (IOException ignored) {
						message = "Couldn't check if " + file.getName() + " is empty.";
					}

					message += isAlternateDirectoryWarningShown()
							? "\nFiles in this directory will be replaced or removed if you continue."
							: "\nFiles in this directory may be replaced or removed if you continue.";

					if (JOptionPane.showConfirmDialog(this,
							message + "\nContinue?",
							"Confirm Save", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
						return;
					}
				} else {
					if (JOptionPane.showConfirmDialog(this,
							file.getName() + " already exists.\nDo you want to replace it?",
							"Confirm Save", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
						return;
					}
				}
			}
		}

		super.approveSelection();
	}
}
