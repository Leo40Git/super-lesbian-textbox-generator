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
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import io.github.leo40git.sltbg.app.BuildInfo;
import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.TextParser;
import io.github.leo40git.sltbg.app.text.TextRenderer;
import io.github.leo40git.sltbg.app.text.element.Element;

public final class AppFrame extends JFrame {
	public AppFrame() {
		// TODO actual app!

		setTitle(BuildInfo.APP_NAME);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new TestPanel());
		pack();
		setResizable(false);
	}

	private static final class TestPanel extends JPanel {
		private static final String SOURCE = "\\c[14]Melody\n\\c[0]\\i[98] Bunny stew is\n\\++\\sb\\c[#BB2929]delicious!";
		private final List<Element> parsed;

		public TestPanel() {
			super(null);

			parsed = TextParser.parse(SOURCE, false);

			setPreferredSize(new Dimension(GameAssets.TEXTBOX_WIDTH, GameAssets.TEXTBOX_HEIGHT));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			var g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.SrcOver);
			GameAssets.drawTextboxBackground(g2d, 0, 0);
		}

		@Override
		protected void paintChildren(Graphics g) {
			super.paintChildren(g);

			var g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.SrcOver);
			//noinspection DataFlowIssue
			g2d.drawImage(GameAssets.getFace("Melody", "Happy").image(), 12, 12, null);
			TextRenderer.render(g2d, 16 + GameAssets.FACE_SIZE + 12, 12, parsed);
			GameAssets.drawTextboxBorder(g2d, 0, 0);
			GameAssets.drawTextboxArrow(g2d, 0, 0);
		}
	}
}
