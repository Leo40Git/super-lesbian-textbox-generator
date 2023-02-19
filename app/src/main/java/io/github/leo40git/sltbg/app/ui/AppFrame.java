/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.github.leo40git.sltbg.app.BuildInfo;
import io.github.leo40git.sltbg.app.ui.components.ExtendedScrollPane;

public final class AppFrame extends JFrame {
	public AppFrame() {
		// TODO actual app!

		setTitle(BuildInfo.APP_NAME);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new TestPanel());
		//pack();
		//setResizable(false);
	}

	private static final class TestPanel extends JPanel implements ActionListener {
		private final Box box;
		private final JButton btnAdd;

		public TestPanel() {
			super(new BorderLayout());

			box = new Box(BoxLayout.PAGE_AXIS);
			btnAdd = new JButton("add label");
			btnAdd.addActionListener(this);

			var btnColHeader = new JButton("col header");
			var btnColFooter = new JButton("col footer");

			var scroll = new ExtendedScrollPane(box);
			scroll.setColumnHeaderView(btnColHeader);
			scroll.setColumnFooterView(btnColFooter);

			add(scroll, BorderLayout.CENTER);
			add(btnAdd, BorderLayout.PAGE_END);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnAdd) {
				var lbl = new JLabel("test");
				lbl.setAlignmentX(LEFT_ALIGNMENT);
				box.add(lbl);
				box.revalidate();
			}
		}
	}
}
