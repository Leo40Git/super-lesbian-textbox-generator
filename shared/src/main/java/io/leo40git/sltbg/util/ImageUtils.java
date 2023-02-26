/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.util;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;

import org.jetbrains.annotations.NotNull;

public final class ImageUtils {
	private ImageUtils() {
		throw new UnsupportedOperationException("ImageUtils only contains static declarations.");
	}

	public static void writeImage(@NotNull RenderedImage image, @NotNull Path path) throws IOException {
		String fileName = path.getName(path.getNameCount() - 1).toString();

		String fileSuffix = "";
		int dotIdx = fileName.lastIndexOf('.');
		if (dotIdx >= 0) {
			fileSuffix = fileName.substring(dotIdx + 1);
		}

		if (fileSuffix.isEmpty()) {
			throw new IOException("Can't write image to \"" + path + "\": no file extension");
		}

		var writerIt = ImageIO.getImageWritersBySuffix(fileSuffix);

		ImageWriter writer = null;
		var imgType = ImageTypeSpecifier.createFromRenderedImage(image);
		while (writerIt.hasNext()) {
			var candidate = writerIt.next();
			if (!candidate.getOriginatingProvider().canEncodeImage(imgType)) {
				continue;
			}
			writer = candidate;
			break;
		}

		if (writer == null) {
			throw new IOException("Can't write image to \"" + path + "\": couldn't find ImageWriter for file extension \"." + fileSuffix + "\"");
		}

		try (var out = Files.newOutputStream(path)) {
			writer.setOutput(out);
			writer.write(image);
		} finally {
			writer.dispose();
		}
	}
}
