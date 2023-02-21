/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.MalformedJsonException;
import io.leo40git.sltbg.json.MissingFieldsException;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;

public final class BuildInfo {
	private BuildInfo() {
		throw new UnsupportedOperationException("BuildInfo only contains static declarations.");
	}

	public static final String APP_NAME = "Super Lesbian Textbox Generator";

	private static boolean loaded = false;
	private static boolean isDevelopment = false;
	private static Semver version;
	private static @Nullable URL updateJsonUrl, homepageUrl, issuesUrl, sourceUrl;
	private static String[] credits;

	static void setDevelopment() {
		if (loaded) {
			throw new RuntimeException("Tried to set development mode after BuildInfo was loaded");
		}

		isDevelopment = true;
	}

	public static void load() throws IOException {
		if (loaded) {
			throw new RuntimeException("BuildInfo loaded twice?!");
		}

		String verStr = null;
		try (InputStream in = openJsonStream();
				InputStreamReader isr = new InputStreamReader(in);
				JsonReader reader = JsonReader.json(isr)) {
			reader.beginObject();
			while (reader.hasNext()) {
				String field = reader.nextName();
				switch (field) {
					case "version" -> verStr = reader.nextString();
					case "urls" -> readURLs(reader);
					case "credits" -> credits = JsonReadUtils.readStringArray(reader);
					default -> throw new MalformedJsonException(reader, "Unknown field " + field);
				}
			}
			reader.endObject();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Failed to parse JSON", e);
		}

		if (verStr == null) {
			throw new MissingFieldsException("Build info", "version");
		}

		if ("${version}".equals(verStr)) {
			if (isDevelopment) {
				version = new Semver("0.0.0-DEV");
			} else {
				throw new IOException("Version placeholder wasn't filled in?!");
			}
		} else {
			try {
				version = new Semver(verStr);
			} catch (SemverException e) {
				throw new IOException("Version is invalid", e);
			}
		}

		loaded = true;
	}

	private static InputStream openJsonStream() throws IOException {
		var in = BuildInfo.class.getResourceAsStream("/build_info.json");
		if (in == null) {
			throw new FileNotFoundException("/build_info.json");
		}
		return in;
	}

	private static void readURLs(JsonReader reader) throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case "update_json" -> updateJsonUrl = readNullableURL(reader);
				case "homepage" -> homepageUrl = readNullableURL(reader);
				case "issues" -> issuesUrl = readNullableURL(reader);
				case "source" -> sourceUrl = readNullableURL(reader);
				default -> throw new MalformedJsonException(reader, "Unknown field " + field);
			}
		}
		reader.endObject();
	}

	private static @Nullable URL readNullableURL(JsonReader reader) throws IOException {
		return JsonReadUtils.readNullable(reader, JsonReadUtils::readURL);
	}

	private static void assertLoaded() {
		if (!loaded) {
			throw new IllegalStateException("Build info hasn't been loaded!");
		}
	}

	public static boolean isDevelopment() {
		assertLoaded();
		return isDevelopment;
	}

	public static Semver version() {
		assertLoaded();
		return version;
	}

	public static @Nullable URL updateJsonUrl() {
		assertLoaded();
		return updateJsonUrl;
	}

	public static @Nullable URL homepageUrl() {
		assertLoaded();
		return homepageUrl;
	}

	public static @Nullable URL issuesUrl() {
		assertLoaded();
		return issuesUrl;
	}

	public static @Nullable URL sourceUrl() {
		assertLoaded();
		return sourceUrl;
	}

	public static String[] credits() {
		assertLoaded();
		return credits.clone();
	}
}
