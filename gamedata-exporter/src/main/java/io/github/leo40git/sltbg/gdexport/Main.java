/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;

import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import io.leo40git.sltbg.swing.components.ConfirmFileChooser;
import io.leo40git.sltbg.swing.status.SwingStatusTreeModel;
import io.leo40git.sltbg.swing.util.ComponentUtils;
import io.leo40git.sltbg.swing.util.UnaSwingFixes;

public final class Main extends JFrame {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("The GameData Exporter cannot run in a headless environment.");
            System.exit(1);
            return;
        }

        UnaSwingFixes.apply();

        var frame = new Main();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();
    }

    private Main() {
        super("GameData Exporter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new ContentPane());
        pack();
    }

    public static final class ContentPane extends JPanel implements ActionListener, FocusListener {
        private final ConfirmFileChooser fcBrowseFolder;

        public final JPanel pnlControl;
        private final JTextField tfGameDataFolder, tfOutputFolder;
        private final JButton btnBrowseGameDataFolder, btnBrowseOutputFolder;
        private final JButton btnStart;

        private final SwingStatusTreeModel treeStatusModel;

        private Path dirGameData, dirOutput;
        private String tfGameDataFolder_TextOnFocus, tfOutputFolder_TextOnFocus;

        public ContentPane() {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            fcBrowseFolder = new ConfirmFileChooser();
            fcBrowseFolder.setFileSelectionMode(ConfirmFileChooser.DIRECTORIES_ONLY);
            fcBrowseFolder.setAlternateDirectoryWarningShown(true);

            tfGameDataFolder = new JTextField();
            tfGameDataFolder.addFocusListener(this);
            btnBrowseGameDataFolder = new JButton("Browse...");
            btnBrowseGameDataFolder.addActionListener(this);

            tfOutputFolder = new JTextField();
            tfOutputFolder.addFocusListener(this);
            btnBrowseOutputFolder = new JButton("Browse...");
            btnBrowseOutputFolder.addActionListener(this);

            btnStart = new JButton("Go");
            btnStart.addActionListener(this);
            btnStart.setEnabled(false);
            btnStart.setAlignmentX(LEFT_ALIGNMENT);

            var treeStatus = new JTree();
            treeStatusModel = new SwingStatusTreeModel(treeStatus, StatusTreeNodeIcon.OPERATION_INITIAL, "Operations");

            var lblGameDataFolder = new JLabel("Game Data Folder:");

            var lblOutputFolder = new JLabel("Output Folder:");

            final var gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            pnlControl = new JPanel(new GridBagLayout());
            gbc.gridx = gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            pnlControl.add(lblGameDataFolder, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;
            pnlControl.add(tfGameDataFolder, gbc);
            gbc.gridx++;
            gbc.weightx = 0;
            gbc.insets.left = 2;
            pnlControl.add(btnBrowseGameDataFolder, gbc);
            gbc.insets.left = 0;
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets.top = 2;
            pnlControl.add(lblOutputFolder, gbc);
            gbc.insets.top = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            pnlControl.add(tfOutputFolder, gbc);
            gbc.gridx++;
            gbc.weightx = 0;
            gbc.insets.left = 2;
            pnlControl.add(btnBrowseOutputFolder, gbc);
            gbc.insets.left = 0;
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.insets.top = gbc.insets.bottom = 4;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            pnlControl.add(btnStart, gbc);

            var pnlStatus = new JPanel(new BorderLayout());
            pnlStatus.setBorder(BorderFactory.createTitledBorder("Status"));
            pnlStatus.add(new JScrollPane(treeStatus), BorderLayout.CENTER);

            add(pnlControl, BorderLayout.PAGE_START);
            add(pnlStatus, BorderLayout.CENTER);

            dirGameData = dirOutput = null;
            tfGameDataFolder_TextOnFocus = tfOutputFolder_TextOnFocus = null;

            setPreferredSize(new Dimension(640, 480));
        }

        private void onDirsUpdated() {
            btnStart.setEnabled(dirGameData != null && dirOutput != null);
        }

        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (src == btnBrowseGameDataFolder) {
                if (fcBrowseFolder.showOpenDialog(this) == ConfirmFileChooser.APPROVE_OPTION) {
                    dirGameData = fcBrowseFolder.getSelectedFile().toPath();
                    tfGameDataFolder.setText(dirGameData.toString());
                    onDirsUpdated();
                }
            } else if (src == btnBrowseOutputFolder) {
                if (fcBrowseFolder.showSaveDialog(this) == ConfirmFileChooser.APPROVE_OPTION) {
                    dirOutput = fcBrowseFolder.getSelectedFile().toPath();
                    tfOutputFolder.setText(dirOutput.toString());
                    onDirsUpdated();
                }
            } else if (src == btnStart) {
                if (dirGameData == null || dirOutput == null) {
                    JOptionPane.showMessageDialog(this, "Please set both the game data and output paths.",
                            "Not ready!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                var treeStatusRoot = treeStatusModel.getStatusRoot();
                treeStatusRoot.removeAllChildren();
                treeStatusRoot.setIcon(StatusTreeNodeIcon.OPERATION_PENDING);

                ComponentUtils.setEnabledRecursive(pnlControl, false);
                // TODO explicitly cancel via close confirmation
                new ExportWorker(this, dirGameData, dirOutput, treeStatusRoot).execute();
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            var src = e.getSource();
            if (src == tfGameDataFolder) {
                tfGameDataFolder_TextOnFocus = tfGameDataFolder.getText();
            } else if (src == tfOutputFolder) {
                tfOutputFolder_TextOnFocus = tfOutputFolder.getText();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            var src = e.getSource();
            if (src == tfGameDataFolder) {
                String text = tfGameDataFolder.getText();
                if (text == null) {
                    dirGameData = null;
                    onDirsUpdated();
                } else if (!text.equals(tfGameDataFolder_TextOnFocus)) {
                    dirGameData = Paths.get(text);
                    onDirsUpdated();
                }
                tfGameDataFolder_TextOnFocus = null;
            } else if (src == tfOutputFolder) {
                String text = tfOutputFolder.getText();
                if (text == null) {
                    dirOutput = null;
                    onDirsUpdated();
                } else if (!text.equals(tfOutputFolder_TextOnFocus)) {
                    dirOutput = Paths.get(text);
                    onDirsUpdated();
                }
                tfOutputFolder_TextOnFocus = null;
            }
        }
    }
}
