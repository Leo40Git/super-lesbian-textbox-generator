/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.operation;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jetbrains.annotations.NotNull;

public interface OperationNode {
	@NotNull String getName();
	@NotNull OperationNodeStatus getStatus();
	void setStatus(@NotNull OperationNodeStatus status);

	@NotNull OperationNode createChild(@NotNull String name, @NotNull OperationNodeStatus status);
	default @NotNull OperationNode createChild(@NotNull String name) {
		return createChild(name, OperationNodeStatus.INITIAL);
	}

	void addDetails(@NotNull String text);

	default void setFailed(Throwable exception, boolean full) {
		setStatus(OperationNodeStatus.FAILED);
		if (exception != null) {
			if (full) {
				var sw = new StringWriter();
				try (var writer = new PrintWriter(sw)) {
					exception.printStackTrace(writer);
				}
				addDetails(sw.toString());
			} else {
				addDetails(exception.toString());
			}
		}
	}
}
