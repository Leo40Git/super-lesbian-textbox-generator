/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport;

import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import io.leo40git.sltbg.status.StatusTreeNode;
import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import io.leo40git.sltbg.swing.util.ComponentUtils;
import org.jetbrains.annotations.NotNull;

public final class ExportWorker extends SwingWorker<Void, Void> {
	private final @NotNull Main.ContentPane contentPane;
	private final @NotNull Path dirGameData, dirOutput;
	private final @NotNull StatusTreeNode treeStatusRoot;
	private final @NotNull AtomicBoolean anyFailed;

	public ExportWorker(@NotNull Main.ContentPane contentPane, @NotNull Path dirGameData, @NotNull Path dirOutput, @NotNull StatusTreeNode treeStatusRoot) {
		this.contentPane = contentPane;
		this.dirGameData = dirGameData;
		this.dirOutput = dirOutput;
		this.treeStatusRoot = treeStatusRoot;
		anyFailed = new AtomicBoolean(false);
	}

	@Override
	protected Void doInBackground() {
		treeStatusRoot.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

		var rnd = new Random();
		int futureCount = rnd.nextInt(4, 16);

		var executor = Executors.newWorkStealingPool();
		var futures = new CompletableFuture[futureCount];
		anyFailed.set(false);

		for (int i = 0; i < futureCount; i++) {
			final var child = treeStatusRoot.addChild(StatusTreeNodeIcon.OPERATION_PENDING, "Operation " + (i + 1));
			final long delay = TimeUnit.SECONDS.toMillis(rnd.nextLong(5, 20));
			futures[i] = CompletableFuture.runAsync(() -> {
				child.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					child.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
					child.addException(e, true);
					anyFailed.lazySet(true);
					return;
				}
				child.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
			}, executor);
		}

		CompletableFuture.allOf(futures).join();

		executor.shutdown();
		boolean terminated;
		try {
			terminated = executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			terminated = false;
		}

		if (!terminated) {
			executor.shutdownNow();
		}

		return null;
	}

	@Override
	protected void done() {
		if (anyFailed.get()) {
			treeStatusRoot.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
			JOptionPane.showMessageDialog(contentPane,
					"Some operations failed...",
					"Done", JOptionPane.ERROR_MESSAGE);
		} else {
			treeStatusRoot.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
			JOptionPane.showMessageDialog(contentPane,
					"Finished all operations!",
					"Done", JOptionPane.INFORMATION_MESSAGE);
		}

		ComponentUtils.setEnabledRecursive(contentPane.pnlControl, true);
	}
}
