/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.util;

import java.awt.Component;
import java.awt.Container;

import org.jetbrains.annotations.NotNull;

public final class ComponentUtils {
    private ComponentUtils() {
        throw new UnsupportedOperationException("ComponentUtils only contains static declarations.");
    }

    public static void setEnabledRecursive(@NotNull Component component, boolean b) {
        component.setEnabled(b);
        if (component instanceof Container container) {
            for (int i = 0; i < container.getComponentCount(); i++) {
                setEnabledRecursive(container.getComponent(i), b);
            }
        }
    }
}
