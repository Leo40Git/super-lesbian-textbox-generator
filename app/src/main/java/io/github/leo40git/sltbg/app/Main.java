/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
	private static Logger logger;

	public static Logger logger() {
		return logger;
	}

	public static void main(String[] args) {
		try {
			BuildInfo.load();
		} catch (Exception e) {
			System.err.println("Failed to load build info! This build is probably hosed!");
			e.printStackTrace();
			System.exit(1);
			return;
		}

		try {
			logger = LogManager.getLogger("main");
		} catch (Exception e) {
			System.err.println("Failed to initialize logger!");
			e.printStackTrace();
			System.exit(1);
			return;
		}

		logger.info("{} v{} is now initializing...", BuildInfo.name(), BuildInfo.version().toString());
		if (BuildInfo.isDevelopment()) {
			logger.info(" === DEVELOPMENT MODE! === ");
		}
	}
}
