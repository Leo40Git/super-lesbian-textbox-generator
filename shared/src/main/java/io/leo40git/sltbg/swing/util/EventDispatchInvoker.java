/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.util;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;

public final class EventDispatchInvoker {
    private final @NotNull Runnable action;
    private final @NotNull AtomicBoolean pendingInvoke;

    public EventDispatchInvoker(@NotNull Runnable action) {
        this.action = action;
        pendingInvoke = new AtomicBoolean(false);
    }

    public void invokeLater() {
        if (!pendingInvoke.compareAndExchange(false, true)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    action.run();
                } finally {
                    pendingInvoke.set(false);
                }
            });
        }
    }
}
