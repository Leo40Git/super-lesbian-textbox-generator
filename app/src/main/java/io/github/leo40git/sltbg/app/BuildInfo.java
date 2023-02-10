package io.github.leo40git.sltbg.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.skuzzle.semantic.Version;
import io.github.leo40git.sltbg.app.json.JsonReadUtils;
import io.github.leo40git.sltbg.app.json.MalformedJsonException;
import io.github.leo40git.sltbg.app.json.MissingFieldsException;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;

public final class BuildInfo {
	private static boolean loaded = false;
	private static boolean isDevelopment = false;
	private static String name;
	private static Version version;
	private static @Nullable URL updateJsonUrl, homepageUrl, issuesUrl, sourceUrl;
	private static String[] credits;

	private BuildInfo() { }

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
					case "name" -> name = reader.nextString();
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

		List<String> missingFields = new ArrayList<>();
		if (name == null) {
			missingFields.add("name");
		}
		if (verStr == null) {
			missingFields.add("version");
		}

		if (!missingFields.isEmpty()) {
			throw new MissingFieldsException("Build info", missingFields);
		}

		if ("${version}".equals(verStr)) {
			if (isDevelopment) {
				version = Version.ZERO.withPreRelease("dev");
			} else {
				throw new IOException("Version placeholder wasn't filled in?!");
			}
		} else {
			try {
				version = Version.parseVersion(verStr, true);
			} catch (Version.VersionFormatException e) {
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

	public static String name() {
		assertLoaded();
		return name;
	}

	public static Version version() {
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
