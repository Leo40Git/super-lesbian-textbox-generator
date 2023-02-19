/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.ui.components;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

public final class ExtendedScrollPane extends JScrollPane {
	private JViewport rowFooter, columnFooter;

	public ExtendedScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
		super(view, vsbPolicy, hsbPolicy);
		setLayout(new ExtendedScrollPaneLayout());
	}

	public ExtendedScrollPane(Component view) {
		this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public ExtendedScrollPane(int vsbPolicy, int hsbPolicy) {
		this(null, vsbPolicy, hsbPolicy);
	}

	public ExtendedScrollPane() {
		this(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public JViewport getRowFooter() {
		return rowFooter;
	}

	public void setRowFooter(JViewport rowFooter) {
		JViewport old = getRowFooter();
		this.rowFooter = rowFooter;
		if (rowFooter != null) {
			add(rowFooter, ExtendedScrollPaneLayout.ROW_FOOTER);
		}
		else if (old != null) {
			remove(old);
		}
		firePropertyChange("rowFooter", old, rowFooter);
		revalidate();
		repaint();
	}

	public void setRowFooterView(Component view) {
		if (getRowFooter() == null) {
			setRowFooter(createViewport());
		}
		getRowFooter().setView(view);
	}

	public JViewport getColumnFooter() {
		return columnFooter;
	}

	public void setColumnFooter(JViewport columnFooter) {
		JViewport old = getColumnFooter();
		this.columnFooter = columnFooter;
		if (columnFooter != null) {
			add(columnFooter, ExtendedScrollPaneLayout.COLUMN_FOOTER);
		}
		else if (old != null) {
			remove(old);
		}
		firePropertyChange("columnFooter", old, columnFooter);

		revalidate();
		repaint();
	}

	public void setColumnFooterView(Component view) {
		if (getColumnFooter() == null) {
			setColumnFooter(createViewport());
		}
		getColumnFooter().setView(view);
	}
}
