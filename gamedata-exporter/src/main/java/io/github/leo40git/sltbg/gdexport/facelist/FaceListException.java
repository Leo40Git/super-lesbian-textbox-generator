/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facelist;

import java.io.IOException;

public final class FaceListException extends IOException {
	private final int lineNumber;

	public FaceListException(String message, int lineNumber) {
		super(message);
		this.lineNumber = lineNumber;
	}

	public FaceListException(String message, int lineNumber, Throwable cause) {
		super(message, cause);
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String getLocalizedMessage() {
		return super.getLocalizedMessage() + " at line " + lineNumber;
	}
}