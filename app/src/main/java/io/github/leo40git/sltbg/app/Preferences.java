/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class Preferences {
    private Preferences() {
        throw new UnsupportedOperationException("Preferences only contains static declarations.");
    }

    public static final int CURRENT_VERSION = 1;
    public static final Path PATH = Paths.get("preferences.json5").toAbsolutePath();

    public static final class Fields {
        private Fields() {
            throw new UnsupportedOperationException("Fields only contains static declarations.");
        }

        public static final String VERSION = "version";
        public static final String AUTO_UPDATE_CHECK_ENABLED = "auto_update_check_enabled";
        public static final String SHOULD_COPY_CURRENT_FACE = "copy_current_face";
    }

    private static boolean initialized = false;
    private static boolean autoUpdateCheckEnabled;
    private static boolean shouldCopyCurrentFace;

    private static void read(int version, @NotNull JsonReader reader) throws IOException {
        // in the future, the "version" property will determine how to read preferences from the object
        //  to allow for backwards compatibility with older preferences files, in case a field name gets changed
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case Fields.AUTO_UPDATE_CHECK_ENABLED -> autoUpdateCheckEnabled = reader.nextBoolean();
                case Fields.SHOULD_COPY_CURRENT_FACE -> shouldCopyCurrentFace = reader.nextBoolean();
                default -> {
                    Main.logger().warn("Preferences: unknown field '{}', ignoring", field);
                    reader.skipValue();
                }
            }
        }
    }

    private static void write(@NotNull JsonWriter writer) throws IOException {
        writer.name(Fields.VERSION);
        writer.value(CURRENT_VERSION);
        writer.name(Fields.AUTO_UPDATE_CHECK_ENABLED);
        writer.value(autoUpdateCheckEnabled);
        writer.name(Fields.SHOULD_COPY_CURRENT_FACE);
        writer.value(shouldCopyCurrentFace);
    }

    public static void init() throws IOException {
        reset();
        initialized = false;

        if (Files.exists(PATH)) {
            boolean gotVersion = false, needToReopen = false;
            int version = 0;
            try (JsonReader reader = JsonReader.json5(PATH)) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String field = reader.nextName();
                    if ("version".equals(field)) {
                        version = reader.nextInt();
                        gotVersion = true;
                        break;
                    }
                    // version wasn't first entry, need to reopen file after we find it
                    needToReopen = true;
                }

                if (!gotVersion) {
                    throw new IOException("Missing version field!");
                }

                if (!needToReopen) {
                    read(version, reader);
                    reader.endObject();
                }
            }

            if (needToReopen) {
                try (JsonReader reader = JsonReader.json5(PATH)) {
                    reader.beginObject();
                    read(version, reader);
                    reader.endObject();
                }
            }
        } else {
            flush();
        }

        initialized = true;
    }

    public static void reset() {
        autoUpdateCheckEnabled = true;
        shouldCopyCurrentFace = true;

        initialized = true;
    }

    public static void flush() {
        if (!initialized) {
            return;
        }

        try (JsonWriter writer = JsonWriter.json5(PATH)) {
            writer.beginObject();
            write(writer);
            writer.endObject();
        } catch (Exception e) {
            Main.logger().error("Failed to flush preferences!", e);
        }
    }

    public static boolean isAutoUpdateCheckEnabled() {
        return autoUpdateCheckEnabled;
    }

    public static void setAutoUpdateCheckEnabled(boolean autoUpdateCheckEnabled) {
        Preferences.autoUpdateCheckEnabled = autoUpdateCheckEnabled;
    }

    public static boolean shouldCopyCurrentFace() {
        return shouldCopyCurrentFace;
    }

    public static void setShouldCopyCurrentFace(boolean shouldCopyCurrentFace) {
        Preferences.shouldCopyCurrentFace = shouldCopyCurrentFace;
    }
}
