package io.github.leo40git.sltbg.app;

public final class DevMain {
	public static void main(String[] args) {
		System.setProperty("log4j.skipJansi", "false"); // enable Log4J's Jansi support
		BuildInfo.setDevelopment();
		Main.main(args);
	}
}
