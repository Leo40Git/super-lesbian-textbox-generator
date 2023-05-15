/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.window;

import org.jetbrains.annotations.Range;

/**
 * Represents an RGB <em>tone</em>, also known as a tint. Each channel of this tone can be additive or subtractive.
 */
public record WindowTone(@Range(from = -255, to = 255) int red,
                         @Range(from = -255, to = 255) int green,
                         @Range(from = -255, to = 255) int blue) {
    /**
     * An empty tone.
     */
    public static final WindowTone EMPTY = new WindowTone(0, 0, 0);

    /**
     * Checks if the tone is <em>empty</em>, I.E. if all channels are set to 0.
     *
     * @return {@code true} if empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return red == 0 && green == 0 && blue == 0;
    }
}
