/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.ui;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.github.leo40git.sltbg.app.BuildInfo;
import io.github.leo40git.sltbg.app.assets.GameAssets;

public final class AppFrame extends JFrame {
	public AppFrame() {
		// TODO actual app!

		setTitle(BuildInfo.name());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new TestPanel());
		pack();
		setResizable(false);
	}

	private static final class TestPanel extends JPanel {
		public TestPanel() {
			super(null);

			var label = new JLabel("WIP!!!");
			label.setFont(GameAssets.getFont().deriveFont(GameAssets.DEFAULT_FONT_SIZE));
			label.setForeground(GameAssets.getPaletteColor(0));

			label.setLocation(16, 12);
			label.setSize(label.getPreferredSize());
			add(label);

			setPreferredSize(new Dimension(GameAssets.TEXTBOX_WIDTH, GameAssets.TEXTBOX_HEIGHT));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			var g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.SrcOver);
			GameAssets.drawTextboxBackground(g, 0, 0);
		}

		@Override
		protected void paintChildren(Graphics g) {
			super.paintChildren(g);

			var g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.SrcOver);
			GameAssets.drawTextboxBorder(g, 0, 0);
			GameAssets.drawTextboxArrow(g, 0, 0);
		}
	}
}
