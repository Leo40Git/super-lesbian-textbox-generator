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

import io.leo40git.sltbg.operation.OperationNode;
import io.leo40git.sltbg.operation.OperationNodeStatus;
import io.leo40git.sltbg.swing.util.ComponentUtils;
import org.jetbrains.annotations.NotNull;

public final class ExportWorker extends SwingWorker<Void, Void> {
	private final @NotNull Main.ContentPane contentPane;
	private final @NotNull Path dirGameData, dirOutput;
	private final @NotNull OperationNode rootNode;
	private final @NotNull AtomicBoolean anyFailed;

	public ExportWorker(@NotNull Main.ContentPane contentPane, @NotNull Path dirGameData, @NotNull Path dirOutput, @NotNull OperationNode rootNode) {
		this.contentPane = contentPane;
		this.dirGameData = dirGameData;
		this.dirOutput = dirOutput;
		this.rootNode = rootNode;
		anyFailed = new AtomicBoolean(false);
	}

	@Override
	protected Void doInBackground() {
		var rnd = new Random();
		int futureCount = rnd.nextInt(4, 16);

		var executor = Executors.newWorkStealingPool();
		var futures = new CompletableFuture[futureCount];
		anyFailed.set(false);

		for (int i = 0; i < futureCount; i++) {
			final var child = rootNode.createChild("Operation " + (i + 1), OperationNodeStatus.PENDING);
			final long delay = TimeUnit.SECONDS.toMillis(rnd.nextLong(5, 20));
			futures[i] = CompletableFuture.runAsync(() -> {
				child.setStatus(OperationNodeStatus.IN_PROGRESS);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					child.setFailed(e, true);
					anyFailed.lazySet(true);
					return;
				}
				child.setStatus(OperationNodeStatus.SUCCEEDED);
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
			rootNode.setStatus(OperationNodeStatus.FAILED);
			JOptionPane.showMessageDialog(contentPane,
					"Some operations failed...",
					"Done", JOptionPane.ERROR_MESSAGE);
		} else {
			rootNode.setStatus(OperationNodeStatus.SUCCEEDED);
			JOptionPane.showMessageDialog(contentPane,
					"Finished all operations!",
					"Done", JOptionPane.INFORMATION_MESSAGE);
		}

		ComponentUtils.setEnabledRecursive(contentPane.pnlControl, true);
	}
}
