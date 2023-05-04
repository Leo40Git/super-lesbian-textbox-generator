/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing;

import java.awt.IllegalComponentStateException;
import java.util.Locale;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractIcon implements Icon, Accessible {
    protected @Nullable String description;

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    protected @Nullable AccessibleContext accessibleContext;

    protected @NotNull AccessibleContext createAccessibleContext() {
        return new AccessibleContextImpl();
    }

    @Override
    public @NotNull AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = createAccessibleContext();
        }
        return accessibleContext;
    }

    protected class AccessibleContextImpl extends AccessibleContext implements AccessibleIcon {
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.ICON;
        }

        @Override
        public String getAccessibleIconDescription() {
            return AbstractIcon.this.getDescription();
        }

        @Override
        public void setAccessibleIconDescription(String description) {
            AbstractIcon.this.setDescription(description);
        }

        @Override
        public int getAccessibleIconWidth() {
            return AbstractIcon.this.getIconWidth();
        }

        @Override
        public int getAccessibleIconHeight() {
            return AbstractIcon.this.getIconHeight();
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            return null;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return -1;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        @Override
        public Locale getLocale() throws IllegalComponentStateException {
            return null;
        }
    }
}
