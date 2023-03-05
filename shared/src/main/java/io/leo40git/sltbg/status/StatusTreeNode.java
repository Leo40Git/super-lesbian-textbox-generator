/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jetbrains.annotations.NotNull;

public interface StatusTreeNode {
    @NotNull StatusTreeNodeIcon getIcon();

    void setIcon(@NotNull StatusTreeNodeIcon icon);

    @NotNull String getText();

    void setText(@NotNull String text);

    @NotNull StatusTreeNode addChild(@NotNull StatusTreeNodeIcon icon, @NotNull String text);

    default @NotNull StatusTreeNode addChild(@NotNull String text) {
        return addChild(StatusTreeNodeIcon.MESSAGE_INFORMATION, text);
    }

    void removeAllChildren();

    default @NotNull StatusTreeNode addException(@NotNull Throwable ex, boolean withStackTrace) {
        if (withStackTrace) {
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                ex.printStackTrace(pw);
            }
            return addChild(StatusTreeNodeIcon.MESSAGE_ERROR, sw.toString());
        } else {
            return addChild(StatusTreeNodeIcon.MESSAGE_ERROR, ex.toString());
        }
    }

    default @NotNull StatusTreeNode addException(@NotNull Throwable ex) {
        return addException(ex, true);
    }
}
