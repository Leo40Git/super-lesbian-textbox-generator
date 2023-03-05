/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.ui;

import static io.github.leo40git.sltbg.app.Main.logger;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.leo40git.sltbg.app.BuildInfo;
import io.github.leo40git.sltbg.app.assets.GameAssets;
import io.github.leo40git.sltbg.app.text.TextParser;
import io.github.leo40git.sltbg.app.text.TextRenderer;
import io.github.leo40git.sltbg.app.text.element.ErrorElement;
import io.github.leo40git.sltbg.app.util.DialogUtils;
import io.github.leo40git.sltbg.app.util.StringUtils;

public final class MicroFrame extends JFrame {
    public MicroFrame() {
        setTitle(BuildInfo.APP_NAME + " MICRO");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new ContentPane());
        pack();
    }

    private static final class ContentPane extends JPanel implements ChangeListener, ActionListener {
        private final JCheckBox cbFaceEnabled;
        private final JComboBox<GameAssets.Face> cbFace;
        private final JTextArea txtContent;
        private final JButton btnInfo, btnGenerate;

        public ContentPane() {
            super(new BorderLayout());

            var cbFaceModel = new DefaultComboBoxModel<GameAssets.Face>();
            GameAssets.getAllFaces().values().stream().map(Map::values)
                    .forEach(cbFaceModel::addAll);

            cbFaceEnabled = new JCheckBox("Add face?", true);
            cbFaceEnabled.addChangeListener(this);
            cbFaceEnabled.setAlignmentX(LEFT_ALIGNMENT);

            cbFace = new JComboBox<>();
            cbFace.setModel(cbFaceModel);
            cbFace.setRenderer(new FaceListCellRenderer());
            cbFace.setSelectedIndex(0);
            cbFace.setAlignmentX(LEFT_ALIGNMENT);

            txtContent = new JTextArea(15, 25);

            btnInfo = new JButton("?");
            btnInfo.addActionListener(this);

            btnGenerate = new JButton("Generate!");
            btnGenerate.addActionListener(this);

            var boxFace = new Box(BoxLayout.PAGE_AXIS);
            boxFace.add(cbFaceEnabled);
            boxFace.add(cbFace);

            var pnlButtons = new JPanel(new BorderLayout());
            pnlButtons.add(btnInfo, BorderLayout.BEFORE_LINE_BEGINS);
            pnlButtons.add(btnGenerate, BorderLayout.CENTER);

            add(boxFace, BorderLayout.BEFORE_FIRST_LINE);
            add(new JScrollPane(txtContent), BorderLayout.CENTER);
            add(pnlButtons, BorderLayout.AFTER_LAST_LINE);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == cbFaceEnabled) {
                cbFace.setEnabled(cbFaceEnabled.isSelected());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnInfo) {
                JOptionPane.showMessageDialog(this,
                        "CONTROL CHARACTERS:\n"
                                + "\\c[palette_index] OR \\c[#RRGGBB] - change text color\n"
                                + "\\+, \\-, \\= - change text size (up, down, reset)\n"
                                + "\\sb, \\si, \\su, \\ss, \\sr - change text style "
                                + "(toggle bold, toggle italics, toggle underline, toggle strikethrough, reset)\n"
                                + "\\i[index] - insert icon (see iconset.png)\n"
                                + "\n\n"
                                + "running SLTBG micro pre-release (v" + BuildInfo.version() + ")\n"
                                + "hopefully this will be enough until my burnout is gone, sorry ^^;",
                        BuildInfo.APP_NAME, JOptionPane.PLAIN_MESSAGE);
            } else if (e.getSource() == btnGenerate) {
                var errSB = new StringBuilder();
                var elems = TextParser.parse(StringUtils.simplifyLineSeparators(txtContent.getText()), false);
                for (var elem : elems) {
                    if (elem instanceof ErrorElement err) {
                        // TODO track actual line number as part of position (can't use LineBreakElements since you can escape newlines)
                        errSB.append("%n- %s at position %d".formatted(err.getMessage(), err.getSourceStart()));
                    }
                }

                if (!errSB.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "The following errors were encountered while parsing the text:" + errSB,
                            "Errors while parsing!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                File sel = DialogUtils.fileSaveDialog(this, "Save textbox image",
                        new FileNameExtensionFilter("PNG image files", "png"));
                if (sel == null) {
                    return;
                }

                if (sel.exists()) {
                    final int confirm = JOptionPane.showConfirmDialog(this,
                            "File \"" + sel.getName() + "\" already exists?\nOverwrite it?",
                            "Overwrite existing file?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                    try {
                        Files.delete(sel.toPath());
                    } catch (IOException ex) {
                        logger().error("Error while deleting file!", ex);
                        DialogUtils.showErrorDialog(this,
                                "Could not delete file.",
                                "Could not overwrite file.");
                        return;
                    }
                }

                final boolean drawFace = cbFaceEnabled.isSelected();
                var image = new BufferedImage(GameAssets.TEXTBOX_WIDTH, GameAssets.TEXTBOX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                var g = image.createGraphics();
                g.setComposite(AlphaComposite.SrcOver);
                GameAssets.drawTextboxBackground(g, 0, 0);
                if (drawFace) {
                    g.drawImage(((GameAssets.Face) Objects.requireNonNull(cbFace.getSelectedItem())).image(), 12, 12, null);
                    TextRenderer.render(g, 16 + GameAssets.FACE_SIZE + 12, 12, elems);
                } else {
                    TextRenderer.render(g, 16, 12, elems);
                }
                GameAssets.drawTextboxBorder(g, 0, 0);
                GameAssets.drawTextboxArrow(g, 0, 0);
                g.dispose();

                try {
                    ImageIO.write(image, "png", sel);
                } catch (IOException ex) {
                    logger().error("Error while saving image!", ex);
                    DialogUtils.showErrorDialog(this,
                            "An exception occurred while saving the image:\n" + ex,
                            "Couldn't save image!");
                    return;
                }
                JOptionPane.showMessageDialog(this,
                        "Successfully saved the image to:\n" + sel.getAbsolutePath(),
                        "Success!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private static final class FaceListCellRenderer extends JLabel implements ListCellRenderer<GameAssets.Face> {
        public FaceListCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GameAssets.Face> list, GameAssets.Face value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            if (isSelected) {
                setBackground(UIColors.List.getSelectionBackground());
                setForeground(list.getSelectionForeground()); // TODO should be in UIColors.List
            } else {
                setBackground(index % 2 == 0 ? UIColors.List.getBackground() : UIColors.List.getAlternateBackground());
                setForeground(UIColors.List.getForeground());
            }
            setIcon(value.icon());
            setText(value.toString());
            return this;
        }
    }
}
