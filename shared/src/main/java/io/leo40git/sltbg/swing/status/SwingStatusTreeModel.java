/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.status;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import io.leo40git.sltbg.status.StatusTreeNode;
import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import org.jetbrains.annotations.NotNull;

public final class SwingStatusTreeModel extends DefaultTreeModel {
    private final @NotNull JTree tree;
    private final @NotNull SwingStatusTreeNodeImpl statusRoot;

    private final @NotNull AtomicBoolean updateQueued;
    private final @NotNull ConcurrentLinkedDeque<DefaultMutableTreeNode> pendingExpansions, pendingReloads;
    private final @NotNull HashSet<DefaultMutableTreeNode> nodesToReload;
    private final @NotNull HashSet<TreePath> pathsToExpand;

    public SwingStatusTreeModel(@NotNull JTree tree, @NotNull StatusTreeNodeIcon rootIcon, @NotNull String rootText) {
        super(new DefaultMutableTreeNode());
        this.tree = tree;

        statusRoot = new SwingStatusTreeNodeImpl(this, (DefaultMutableTreeNode) root, rootIcon, rootText);
        updateQueued = new AtomicBoolean(false);
        pendingExpansions = new ConcurrentLinkedDeque<>();
        pendingReloads = new ConcurrentLinkedDeque<>();
        nodesToReload = new HashSet<>();
        pathsToExpand = new HashSet<>();

        tree.setModel(this);
    }

    public SwingStatusTreeModel(@NotNull JTree tree) {
        this(tree, StatusTreeNodeIcon.MESSAGE_INFORMATION, "(root)");
    }

    public @NotNull StatusTreeNode getStatusRoot() {
        return statusRoot;
    }

    void queueUpdate() {
        if (!updateQueued.compareAndExchange(false, true)) {
            SwingUtilities.invokeLater(this::performUpdate);
        }
    }

    void queueExpandNode(@NotNull DefaultMutableTreeNode node) {
        pendingExpansions.add(node);
        queueUpdate();
    }

    void queueReloadNode(@NotNull DefaultMutableTreeNode node) {
        pendingReloads.add(node);
        queueUpdate();
    }

    private void performUpdate() {
        pathsToExpand.clear();

        var nodeToExpand = pendingExpansions.pollFirst();
        while (nodeToExpand != null) {
            pathsToExpand.add(new TreePath(nodeToExpand.getPath()));
            nodeToExpand = pendingExpansions.pollFirst();
        }

        nodesToReload.clear();

        var nodeToReload = pendingReloads.pollFirst();
        while (nodeToReload != null) {
            nodesToReload.add(nodeToReload);
            nodeToReload = pendingReloads.pollFirst();
        }

        if (nodesToReload.size() > 0) {
            for (var node : nodesToReload) {
                var expanded = tree.getExpandedDescendants(new TreePath(node.getPath()));
                if (expanded != null) {
                    while (expanded.hasMoreElements()) {
                        pathsToExpand.add(expanded.nextElement());
                    }
                }
                reload(node);
            }
        }

        nodesToReload.clear();

        if (pathsToExpand.size() > 0) {
            for (var path : pathsToExpand) {
                tree.expandPath(path);
            }
        }

        pathsToExpand.clear();

        tree.revalidate();
        tree.repaint();

        updateQueued.set(false);
    }
}
