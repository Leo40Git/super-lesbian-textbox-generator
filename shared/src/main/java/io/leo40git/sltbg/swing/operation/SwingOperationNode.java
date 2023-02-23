/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.operation;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import io.leo40git.sltbg.operation.OperationNode;
import io.leo40git.sltbg.operation.OperationNodeStatus;
import io.leo40git.sltbg.swing.util.EventDispatchInvoker;
import org.jetbrains.annotations.NotNull;

public class SwingOperationNode implements OperationNode {
	private final @NotNull JTree tree;
	private final @NotNull DefaultTreeModel model;
	private final @NotNull EventDispatchInvoker treeUpdater;
	private final @NotNull DefaultMutableTreeNode realSelf;
	private final @NotNull String name;
	private @NotNull OperationNodeStatus status;
	private boolean expandedSelf;
	private int subopsCount, detailsCount;

	private SwingOperationNode(@NotNull JTree tree, @NotNull DefaultTreeModel model, @NotNull EventDispatchInvoker treeUpdater,
			@NotNull DefaultMutableTreeNode realSelf, @NotNull String name, @NotNull OperationNodeStatus status) {
		this.tree = tree;
		this.model = model;
		this.treeUpdater = treeUpdater;
		this.realSelf = realSelf;
		this.name = name;
		this.status = status;
		expandedSelf = false;
		subopsCount = detailsCount = 0;
	}

	private SwingOperationNode(@NotNull JTree tree, @NotNull DefaultTreeModel model, @NotNull DefaultMutableTreeNode realSelf, @NotNull String name, @NotNull OperationNodeStatus status) {
		this.tree = tree;
		this.treeUpdater = new EventDispatchInvoker(() -> {
			tree.revalidate();
			tree.repaint();
		});
		this.model = model;
		this.realSelf = realSelf;
		this.name = name;
		this.status = status;
		expandedSelf = false;
		subopsCount = detailsCount = 0;
	}

	public static @NotNull SwingOperationNode setup(@NotNull JTree tree, @NotNull String rootName, @NotNull OperationNodeStatus rootStatus) {
		var realSelf = new DefaultMutableTreeNode();
		var model = new DefaultTreeModel(realSelf);
		tree.setModel(model);
		var uo = new SwingOperationNode(tree, model, realSelf, rootName, rootStatus);
		realSelf.setUserObject(uo);
		return uo;
	}

	public static @NotNull SwingOperationNode setup(@NotNull JTree tree, @NotNull String rootName) {
		return setup(tree, rootName, OperationNodeStatus.INITIAL);
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	public @NotNull OperationNodeStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(@NotNull OperationNodeStatus status) {
		synchronized (treeUpdater) {
			this.status = status;
			treeUpdater.invokeLater();
		}
	}

	@Override
	public @NotNull OperationNode createChild(@NotNull String name, @NotNull OperationNodeStatus status) {
		synchronized (treeUpdater) {
			var node = new DefaultMutableTreeNode();
			var opNode = new SwingOperationNode(tree, model, treeUpdater, node, name, status);
			node.setUserObject(opNode);

			if (realSelf.isLeaf()) {
				realSelf.add(node);
				subopsCount = detailsCount = 0;
			} else if (detailsCount <= 0) {
				realSelf.add(node);
				subopsCount++;
			} else {
				realSelf.insert(node, subopsCount++);
			}

			fireNodeStructureChanged(true);

			return opNode;
		}
	}

	@Override
	public void addDetails(@NotNull String text) {
		synchronized (treeUpdater) {
			var node = new DefaultMutableTreeNode(text);

			if (realSelf.isLeaf()) {
				realSelf.add(node);
				subopsCount = detailsCount = 0;
			} else if (subopsCount <= 0) {
				realSelf.add(node);
				detailsCount++;
			} else {
				realSelf.insert(node, subopsCount + detailsCount++);
			}

			fireNodeStructureChanged(true);
		}
	}



	public void removeAllChildren() {
		realSelf.removeAllChildren();
		expandedSelf = false;
		fireNodeStructureChanged(false);
	}

	private final AtomicBoolean nodeStructureChangePending = new AtomicBoolean(false);
	private final AtomicBoolean expandSelfPending = new AtomicBoolean(false);

	private void fireNodeStructureChanged(boolean expandSelf) {
		if (expandSelf && !expandedSelf) {
			expandedSelf = true;
			expandSelfPending.set(true);
		} else {
			expandSelfPending.set(false);
		}

		if (!nodeStructureChangePending.compareAndExchange(false, true)) {
			SwingUtilities.invokeLater(this::fireNodeStructureChanged0);
		}
	}

	private void fireNodeStructureChanged0() {
		final var selfPath = new TreePath(realSelf.getPath());

		if (expandSelfPending.compareAndExchange(true, false)) {
			tree.expandPath(selfPath);
		}

		var expandedEn = tree.getExpandedDescendants(selfPath);
		ArrayList<TreePath> expanded = null;
		if (expandedEn != null) {
			expanded = new ArrayList<>();
			while (expandedEn.hasMoreElements()) {
				expanded.add(expandedEn.nextElement());
			}
		}
		model.nodeStructureChanged(realSelf);
		if (expanded != null) {
			for (var path : expanded) {
				tree.expandPath(path);
			}
		}

		tree.revalidate();
		tree.repaint();

		nodeStructureChangePending.set(false);
	}

	@Override
	public String toString() {
		return name + " (status: " + status.toString().toLowerCase(Locale.ROOT) + ")";
	}
}
