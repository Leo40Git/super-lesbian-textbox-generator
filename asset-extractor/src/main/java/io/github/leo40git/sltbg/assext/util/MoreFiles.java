package io.github.leo40git.sltbg.assext.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public final class MoreFiles {
	private MoreFiles() {
		throw new UnsupportedOperationException("MoreFiles only contains static declarations.");
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
}
