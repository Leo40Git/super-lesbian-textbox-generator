/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.text.parse;

import io.github.leo40git.sltbg.app.text.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@FunctionalInterface
public interface ControlElementParser {
	@NotNull Element parse(@NotNull TextScanner scn, @Range(from = 0, to = Integer.MAX_VALUE) int sourceStart);
}
