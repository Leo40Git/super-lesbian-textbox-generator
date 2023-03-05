/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public final class FileUtils {
    private FileUtils() {
        throw new UnsupportedOperationException("MoreFiles only contains static declarations.");
    }

    public static @NotNull String getFileSuffix(@NotNull String fileName) {
        String fileSuffix = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx >= 0) {
            fileSuffix = fileName.substring(dotIdx + 1);
        }
        return fileSuffix;
    }

    public static @NotNull String getFileSuffix(@NotNull Path path) {
        return getFileSuffix(path.getFileName().toString());
    }

    private static final class DirectoryDeleter extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(file);
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Objects.requireNonNull(dir);

            if (exc != null) {
                throw exc;
            }

            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    public static void deleteDirectory(@NotNull Path dir) throws IOException {
        Files.walkFileTree(dir, new DirectoryDeleter());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean deleteDirectoryIfExists(@NotNull Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            deleteDirectory(dir);
            return true;
        }
        return false;
    }

    public static boolean isEmptyDirectory(@NotNull Path dir) throws IOException {
        try (var ds = Files.newDirectoryStream(dir)) {
            return !ds.iterator().hasNext();
        }
    }
}
