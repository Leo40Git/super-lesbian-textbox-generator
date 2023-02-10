package io.github.leo40git.sltbg.assext.face;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

public final class FaceListParser {
	public static final String CMD_BEGIN = "BGN";
	public static final String CMD_ADD = "ADD";
	public static final String CMD_SKIP = "SKP";
	public static final String CMD_END = "END";

	private static final Pattern DELIMITER = Pattern.compile("\t", Pattern.LITERAL);

	public static @NotNull List<FaceCollector> parse(@NotNull Path rootPath, @NotNull BufferedReader reader) throws IOException {
		var results = new ArrayList<FaceCollector>();
		var names = new HashMap<String, Set<String>>();
		var paths = new HashSet<String>();

		int lineNum = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			lineNum++;

			if (line.startsWith(";") || line.isBlank()) {
				continue;
			}

			try (var scn = new Scanner(line).useDelimiter(DELIMITER)) {
				String cmd = scn.next();
				if (cmd.equals(CMD_BEGIN)) {
					var sheetPath = rootPath.resolve(requireArg(scn, CMD_BEGIN, "sheet path", lineNum));
					lineNum = parseOne(sheetPath, reader, lineNum, results, names, paths);
				} else {
					throw new FaceListException("Unexpected command %s".formatted(cmd), lineNum);
				}
			}
		}

		return results;
	}

	private static final String[] NO_TAGS = new String[0];

	private static int parseOne(@NotNull Path sheetPath, @NotNull BufferedReader reader, int lineNum,
			@NotNull List<FaceCollector> results, @NotNull Map<String, Set<String>> names, @NotNull Set<String> paths) throws IOException {
		var entries = new ArrayList<FaceListEntry>();

		boolean ended = false;
		String line;
		while (!ended && (line = reader.readLine()) != null) {
			lineNum++;

			if (line.startsWith(";") || line.isBlank()) {
				continue;
			}

			try (var scn = new Scanner(line).useDelimiter(DELIMITER)) {
				String cmd = scn.next();
				switch (cmd) {
					case CMD_ADD -> {
						String category = requireArg(scn, CMD_ADD, "category", lineNum);
						String name = requireArg(scn, CMD_ADD, "name", lineNum);
						var nameSet = names.get(category);
						if (nameSet == null) {
							nameSet = new HashSet<>();
							nameSet.add(name);
							names.put(category, nameSet);
						} else if (!nameSet.add(name)) {
							throw new FaceListException("%s: duplicate name '%s/%s'".formatted(CMD_ADD, category, name), lineNum);
						}

						String path = requireArg(scn, CMD_ADD, "image path", lineNum);
						if (!paths.add(path)) {
							throw new FaceListException("%s: duplicate path '%s'".formatted(CMD_ADD, path), lineNum);
						}

						var tags = NO_TAGS;
						if (scn.hasNext()) {
							String tagTkn = scn.next();
							if (tagTkn.contains(",")) {
								tags = tagTkn.split(",");
							} else {
								tags = new String[] { tagTkn };
							}
						}

						entries.add(new FaceListEntry.Add(category, name, path, tags));
					}
					case CMD_SKIP -> {
						int indexAdvance = 1;
						if (scn.hasNextInt()) {
							indexAdvance = scn.nextInt();
							if (indexAdvance < 1) {
								throw new FaceListException("%s command has invalid advance argument (must be 1 or higher, but was %d)"
										.formatted(CMD_SKIP, indexAdvance), lineNum);
							}
						}
						entries.add(new FaceListEntry.Skip(indexAdvance));
					}
					case CMD_END -> ended = true;
					default -> throw new FaceListException("Unexpected command %s".formatted(cmd), lineNum);
				}
			}
		}

		if (!ended) {
			throw new FaceListException("Hit EOF before %s command".formatted(CMD_END), lineNum);
		}

		results.add(new FaceCollector(sheetPath, entries));
		return lineNum;
	}

	private static @NotNull String requireArg(@NotNull Scanner scn, @NotNull String cmd, @NotNull String argDesc, int lineNum) throws FaceListException {
		if (!scn.hasNext()) {
			throw new FaceListException("%s command missing %s argument".formatted(cmd, argDesc), lineNum);
		}
		return scn.next();
	}
}
