/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app;

public final class DevMain {
	public static void main(String[] args) {
		System.setProperty("log4j.skipJansi", "false"); // enable Log4J's Jansi support
		BuildInfo.setDevelopment();
		Main.main(args);
	}
}
