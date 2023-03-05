/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.status;

import javax.swing.tree.DefaultMutableTreeNode;

import io.leo40git.sltbg.status.StatusTreeNode;
import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import org.jetbrains.annotations.NotNull;

final class SwingStatusTreeNodeImpl implements StatusTreeNode {
    private final @NotNull SwingStatusTreeModel model;
    private final @NotNull DefaultMutableTreeNode selfNode;
    private @NotNull StatusTreeNodeIcon icon;
    private @NotNull String text;
    private boolean hasExpanded;

    public SwingStatusTreeNodeImpl(@NotNull SwingStatusTreeModel model, @NotNull DefaultMutableTreeNode selfNode,
                                   @NotNull StatusTreeNodeIcon icon, @NotNull String text) {
        this.model = model;
        this.selfNode = selfNode;
        this.icon = icon;
        this.text = text;
        hasExpanded = false;

        selfNode.setUserObject(this);
    }

    @Override
    public @NotNull StatusTreeNodeIcon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(@NotNull StatusTreeNodeIcon icon) {
        if (this.icon != icon) {
            this.icon = icon;
            model.queueUpdate();
        }
    }

    @Override
    public @NotNull String getText() {
        return text;
    }

    @Override
    public void setText(@NotNull String text) {
        this.text = text;
        model.queueUpdate();
    }

    @Override
    public @NotNull StatusTreeNode addChild(@NotNull StatusTreeNodeIcon icon, @NotNull String text) {
        var childNode = new DefaultMutableTreeNode();
        synchronized (model) {
            selfNode.add(childNode);
        }
        model.queueReloadNode(selfNode);
        if (!hasExpanded) {
            hasExpanded = true;
            model.queueExpandNode(selfNode);
        }
        return new SwingStatusTreeNodeImpl(model, childNode, icon, text);
    }

    @Override
    public void removeAllChildren() {
        synchronized (model) {
            selfNode.removeAllChildren();
        }
        model.queueReloadNode(selfNode);
    }

    @Override
    public String toString() {
        return text + " (icon: " + icon + ")";
    }
}
