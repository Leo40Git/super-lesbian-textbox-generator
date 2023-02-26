/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facelist;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

public final class FaceListParser {
	private FaceListParser() {
		throw new UnsupportedOperationException("FaceListParser only contains static declarations.");
	}

	public static final String COMMENT_PREFIX = ";";

	public static final String CMD_BEGIN = "BEGIN";
	public static final String CMD_ADD = "ADD";
	public static final String CMD_ADD_S = "+";
	public static final String CMD_SKIP = "SKIP";
	public static final String CMD_SKIP_S = ">";
	public static final String CMD_END = "END";

	public static final String SEC_CATS = "CATEGORIES";
	public static final String SEC_FILE = "FILE";

	private static final Pattern DELIMITER = Pattern.compile("\t", Pattern.LITERAL);

	public static @NotNull FaceList parse(@NotNull BufferedReader reader, int lineNumber) throws IOException {
		var faceList = new FaceList();
		boolean isDeclaringCategories = false;
		FaceSheetSplitter currentSplitter = null;

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(COMMENT_PREFIX) || line.isBlank()) {
				lineNumber++;
				continue;
			}

			var scn = new Scanner(line)
					.useDelimiter(DELIMITER)
					.useLocale(Locale.ROOT);
			if (!scn.hasNext()) {
				lineNumber++;
				continue;
			}

			String cmd = scn.next();
			switch (cmd) {
				case CMD_BEGIN -> {
					if (!scn.hasNext()) {
						throw new FaceListException("%s command missing section parameter".formatted(cmd), lineNumber);
					} else if (isDeclaringCategories || currentSplitter != null) {
						throw new FaceListException("Tried to begin nested section", lineNumber);
					} else {
						String sec = scn.next();
						switch (sec) {
							case SEC_CATS -> isDeclaringCategories = true;
							case SEC_FILE -> {
								if (!scn.hasNext()) {
									throw new FaceListException("%s %s command missing input path parameter".formatted(cmd, sec), lineNumber);
								}
								currentSplitter = new FaceSheetSplitter(scn.next());
							}
							default -> throw new FaceListException("Tried to begin unknown section \"%s\"".formatted(sec), lineNumber);
						}
					}
				}
				case CMD_END -> {
					if (isDeclaringCategories) {
						isDeclaringCategories = false;
					} else if (currentSplitter != null) {
						faceList.addSheetSplitter(currentSplitter);
						currentSplitter = null;
					} else {
						throw new FaceListException("Tried to end nonexistent section", lineNumber);
					}
				}
				case CMD_ADD, CMD_ADD_S -> {
					if (isDeclaringCategories) {
						if (!scn.hasNext()) {
							throw new FaceListException("%s command missing category name parameter".formatted(CMD_ADD), lineNumber);
						}
						String name = scn.next();

						if (!scn.hasNextLong()) {
							throw new FaceListException("%s command missing category order parameter".formatted(CMD_ADD), lineNumber);
						}
						long order = scn.nextLong();

						String characterName = null;
						if (scn.hasNext()) {
							characterName = scn.next();
						}

						faceList.addPendingCategory(new PendingFaceCategory(name, order, characterName));
					} else if (currentSplitter != null) {
						if (!scn.hasNext()) {
							throw new FaceListException("%s command missing face image path parameter".formatted(CMD_ADD), lineNumber);
						}
						String imagePath = scn.next();

						if (!scn.hasNext()) {
							throw new FaceListException("%s command missing face category parameter".formatted(CMD_ADD), lineNumber);
						}
						String category = scn.next();

						if (!scn.hasNext()) {
							throw new FaceListException("%s command missing face name parameter".formatted(CMD_ADD), lineNumber);
						}
						String name = scn.next();

						if (!scn.hasNextLong()) {
							throw new FaceListException("%s command missing face order parameter".formatted(CMD_ADD), lineNumber);
						}
						long order = scn.nextLong();

						String characterName = null;
						if (scn.hasNext()) {
							characterName = scn.next();
						}

						currentSplitter.addEntry(new FaceSheetSplitter.AddEntry(imagePath, category, name, order, characterName));
					} else {
						throw new FaceListException("Invalid %s command outside of section".formatted(CMD_ADD), lineNumber);
					}
				}
				case CMD_SKIP, CMD_SKIP_S -> {
					if (currentSplitter == null) {
						throw new FaceListException("Invalid %s command outside of %s section".formatted(CMD_SKIP, SEC_FILE), lineNumber);
					}

					int advance = 1;
					if (scn.hasNextInt()) {
						advance = scn.nextInt();
					}
					currentSplitter.addEntry(new FaceSheetSplitter.SkipEntry(advance));
				}
			}

			lineNumber++;
		}

		if (currentSplitter != null) {
			faceList.addSheetSplitter(currentSplitter);
		}

		return faceList;
	}

	public static @NotNull FaceList parse(@NotNull BufferedReader reader) throws IOException {
		return parse(reader, 1);
	}
}
