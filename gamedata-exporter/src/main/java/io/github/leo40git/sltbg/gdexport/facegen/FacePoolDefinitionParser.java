/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.leo40git.sltbg.gamedata.Face;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FacePoolDefinitionParser {
	public static @NotNull FacePoolDefinition parse(@NotNull BufferedReader reader, int lineNumber) throws IOException {
		var parser = new FacePoolDefinitionParser();

		String line;
		while ((line = reader.readLine()) != null) {
			parser.parseLine(line, lineNumber++);
		}

		return parser.end(lineNumber);
	}

	public static @NotNull FacePoolDefinition parse(@NotNull BufferedReader reader) throws IOException {
		return parse(reader, 1);
	}

	private static final String DELIMITER = "\t";
	private static final Pattern DELIMITER_PATTERN = Pattern.compile(DELIMITER, Pattern.LITERAL);
	private static final String COMMENT_PREFIX = ";";

	private static final String CMD_BEGIN = "BEGIN";
	private static final String CMD_ADD = "ADD";
	private static final String CMD_ADD_S = "+";
	private static final String CMD_DESC = "DESC";
	private static final String CMD_DESC_S = ".";
	private static final String CMD_SKIP = "SKIP";
	private static final String CMD_SKIP_S = ">";
	private static final String CMD_END = "END";

	private static final String SCN_DESC = "DESCRIPTION";
	private static final String SCN_CREDS = "CREDITS";
	private static final String SCN_CATS = "CATEGORIES";
	private static final String SCN_SHEET = "SHEET";

	private static final int SCNID_ROOT = -1;
	private static final int SCNID_DESC = 0;
	private static final int SCNID_CREDS = 1;
	private static final int SCNID_CATS = 2;
	private static final int SCNID_SHEET = 3;

	private static int getSectionID(@NotNull String section, int lineNumber) throws FacePoolDefinitionException {
		return switch (section) {
			case SCN_DESC -> SCNID_DESC;
			case SCN_CREDS -> SCNID_CREDS;
			case SCN_CATS -> SCNID_CATS;
			case SCN_SHEET -> SCNID_SHEET;
			default -> throw new FacePoolDefinitionException("Unknown section \"%s\"".formatted(section), lineNumber);
		};
	}

	private record FaceRecord(@NotNull String category, @NotNull String name, int lineNumber) {}

	private HashMap<String, FaceCategoryDefinition> categories;
	private HashMap<String, Integer> categoryLines;
	private HashMap<String, FaceRecord> faceRecords;
	private ArrayList<FaceSheet> sheets;
	private ArrayList<String> poolDescription, poolCredits;

	private int currentSectionID;
	private String currentSectionName;

	private ArrayList<String> targetLines;

	private static final class FaceCategoryDefinitionBuilder {
		private final @NotNull String name;
		private final long order;
		private final @Nullable String characterName;
		public @Nullable ArrayList<String> description;

		public FaceCategoryDefinitionBuilder(@NotNull String name, long order, @Nullable String characterName) {
			this.name = name;
			this.order = order;
			this.characterName = characterName;
		}

		@Contract(" -> new")
		public @NotNull FaceCategoryDefinition build() {
			return new FaceCategoryDefinition(name, order, characterName, description);
		}
	}

	private FaceCategoryDefinitionBuilder currentCategoryBuilder;

	private static final class FaceSheetEntryBuilder {
		private final @NotNull String imagePath, category, name;
		private final long order;
		private final @Nullable String characterName;
		public @Nullable ArrayList<String> description;
		public int advance;

		public FaceSheetEntryBuilder(@NotNull String imagePath, @NotNull String category, @NotNull String name, long order,
				@Nullable String characterName) {
			this.imagePath = imagePath;
			this.category = category;
			this.name = name;
			this.order = order;
			this.characterName = characterName;

			advance = 1;
		}

		@Contract(" -> new")
		public @NotNull FaceSheet.Entry build() {
			return new FaceSheet.Entry(imagePath, category, name, order, characterName, description, advance);
		}
	}

	private String sheetInputPath;
	private int sheetOffset;
	private FaceSheetEntryBuilder currentSheetEntryBuilder;
	private boolean canDescribeCurrentSheetEntry;
	private ArrayList<FaceSheet.Entry> pendingSheetEntries;

	private FacePoolDefinitionParser() {
		categories = null;
		categoryLines = null;
		faceRecords = null;
		sheets = null;
		poolDescription = null;
		poolCredits = null;

		currentSectionID = SCNID_ROOT;
		currentSectionName = null;

		targetLines = null;

		currentCategoryBuilder = null;

		sheetInputPath = null;
		sheetOffset = 0;
		currentSheetEntryBuilder = null;
		canDescribeCurrentSheetEntry = false;
		pendingSheetEntries = null;
	}

	private void parseLine(@NotNull String line, int lineNumber) throws FacePoolDefinitionException {
		if (line.isEmpty() || line.startsWith(COMMENT_PREFIX) || line.isBlank()) {
			return;
		}

		var scn = new Scanner(line)
				.useDelimiter(DELIMITER_PATTERN)
				.useLocale(Locale.ROOT);
		if (!scn.hasNext()) {
			return;
		}

		boolean lineOK = switch (currentSectionID) {
			case SCNID_ROOT -> parseLineRoot(scn, lineNumber);
			case SCNID_DESC, SCNID_CREDS -> parseLineDescCreds(scn, lineNumber);
			case SCNID_CATS -> parseLineCats(scn, lineNumber);
			case SCNID_SHEET -> parseLineSheet(scn, lineNumber);
			default -> {
				assert false : "unknown section ID " + currentSectionID + "?!";
				yield false;
			}
		};

		if (!lineOK) {
			throw new FacePoolDefinitionException("Unexpected line \"%s\"".formatted(line), lineNumber);
		}
	}

	private boolean endSection() {
		currentSectionID = SCNID_ROOT;
		currentSectionName = null;

		sheetInputPath = null;
		targetLines = null;
		return true;
	}

	@Contract("_ -> fail")
	private boolean throwNestedSectionException(int lineNumber) throws FacePoolDefinitionException {
		throw new FacePoolDefinitionException("Tried to begin nested section", lineNumber);
	}

	private boolean parseLineRoot(@NotNull Scanner scn, int lineNumber) throws FacePoolDefinitionException {
		String cmd = scn.next();
		return switch (cmd) {
			case CMD_BEGIN -> {
				if (!scn.hasNext()) {
					throw new FacePoolDefinitionException(CMD_BEGIN + " command missing section parameter", lineNumber);
				}

				String newSectionName = scn.next();
				currentSectionID = getSectionID(newSectionName, lineNumber);
				currentSectionName = newSectionName;

				if (currentSectionID == SCNID_SHEET) {
					if (!scn.hasNext()) {
						throw new FacePoolDefinitionException(CMD_BEGIN + " " + SCN_SHEET + " command missing sheet path parameter", lineNumber);
					}
					sheetInputPath = scn.next();
				}

				yield true;
			}
			case CMD_END -> throw new FacePoolDefinitionException("No section to end", lineNumber);
			default -> false;
		};
	}

	private boolean parseLineDescCreds(@NotNull Scanner scn, int lineNumber) throws FacePoolDefinitionException {
		String cmd = scn.next();
		return switch (cmd) {
			case CMD_BEGIN -> throwNestedSectionException(lineNumber);
			case CMD_END -> endSection();
			case CMD_ADD, CMD_ADD_S, CMD_DESC, CMD_DESC_S -> {
				if (!scn.hasNext()) {
					yield true;
				}

				if (targetLines == null) {
					if (currentSectionID == SCNID_CREDS) {
						targetLines = Objects.requireNonNullElseGet(poolCredits, () -> poolCredits = new ArrayList<>());
					} else {
						targetLines = Objects.requireNonNullElseGet(poolDescription, () -> poolDescription = new ArrayList<>());
					}
				}
				targetLines.add(scn.next());
				yield true;
			}
			default -> false;
		};
	}

	private void flushCategory() {
		if (currentCategoryBuilder == null) {
			return;
		}

		if (categories == null) {
			categories = new HashMap<>();
		}

		categories.put(currentCategoryBuilder.name, currentCategoryBuilder.build());
		currentCategoryBuilder = null;
	}

	private boolean parseLineCats(@NotNull Scanner scn, int lineNumber) throws FacePoolDefinitionException {
		String cmd = scn.next();
		return switch (cmd) {
			case CMD_BEGIN -> throwNestedSectionException(lineNumber);
			case CMD_END -> {
				flushCategory();
				endSection();
				yield true;
			}
			case CMD_ADD, CMD_ADD_S -> {
				flushCategory();

				if (!scn.hasNext()) {
					throw new FacePoolDefinitionException(CMD_ADD + " command missing category name parameter", lineNumber);
				}
				String name = scn.next();

				if (categoryLines == null) {
					categoryLines = new HashMap<>();
				}
				var oldLine = categoryLines.put(name, lineNumber);
				if (oldLine != null) {
					throw new FacePoolDefinitionException("Category \"" + name + "\" defined twice",
							"(defined previously at line " + oldLine + ")", lineNumber);
				}

				if (!scn.hasNextLong()) {
					throw new FacePoolDefinitionException(CMD_ADD + " command missing category order parameter", lineNumber);
				}
				long order = scn.nextLong();

				String characterName = null;
				if (scn.hasNext()) {
					characterName = scn.next();
				}

				currentCategoryBuilder = new FaceCategoryDefinitionBuilder(name, order, characterName);
				yield true;
			}
			case CMD_DESC, CMD_DESC_S -> {
				if (currentCategoryBuilder == null) {
					throw new FacePoolDefinitionException(
							CMD_DESC + " command not following " + CMD_ADD + " command or other " + CMD_DESC + " command", lineNumber);
				}

				if (!scn.hasNext()) {
					yield true;
				}

				if (currentCategoryBuilder.description == null) {
					currentCategoryBuilder.description = new ArrayList<>();
				}

				currentCategoryBuilder.description.add(scn.next());
				yield true;
			}
			default -> false;
		};
	}

	private void flushSheetEntry() {
		if (currentSheetEntryBuilder == null) {
			return;
		}

		if (pendingSheetEntries == null) {
			pendingSheetEntries = new ArrayList<>();
		}

		pendingSheetEntries.add(currentSheetEntryBuilder.build());
		currentSheetEntryBuilder = null;
	}

	private void flushSheet() {
		flushSheetEntry();

		if (pendingSheetEntries == null || pendingSheetEntries.isEmpty()) {
			return;
		}

		if (sheets == null) {
			sheets = new ArrayList<>();
		}
		sheets.add(new FaceSheet(sheetInputPath, sheetOffset, pendingSheetEntries));
		pendingSheetEntries.clear();
		sheetOffset = 0;
	}

	private boolean parseLineSheet(@NotNull Scanner scn, int lineNumber) throws FacePoolDefinitionException {
		assert sheetInputPath != null : "sheetInputPath == null?!";

		String cmd = scn.next();
		return switch (cmd) {
			case CMD_BEGIN -> throwNestedSectionException(lineNumber);
			case CMD_END -> {
				flushSheet();
				endSection();
				yield true;
			}
			case CMD_ADD, CMD_ADD_S -> {
				flushSheetEntry();

				if (!scn.hasNext()) {
					throw new FacePoolDefinitionException(CMD_ADD + " command missing face image path parameter", lineNumber);
				}
				String imagePath = scn.next();

				if (!scn.hasNext()) {
					throw new FacePoolDefinitionException(CMD_ADD + " command missing face category parameter", lineNumber);
				}
				String category = scn.next();

				if (!scn.hasNext()) {
					throw new FacePoolDefinitionException(CMD_ADD + " command missing face name parameter", lineNumber);
				}
				String name = scn.next();

				if (faceRecords == null) {
					faceRecords = new HashMap<>();
				}
				String fullName = category + Face.PATH_DELIMITER + name;
				var oldRecord = faceRecords.put(fullName, new FaceRecord(category, name, lineNumber));
				if (oldRecord != null) {
					throw new FacePoolDefinitionException("Face \"" + fullName + "\" defined twice",
							"(defined previously at line " + oldRecord.lineNumber() + ")", lineNumber);
				}

				if (!scn.hasNextLong()) {
					throw new FacePoolDefinitionException(CMD_ADD + " command missing face order parameter", lineNumber);
				}
				long order = scn.nextLong();

				String characterName = null;
				if (scn.hasNext()) {
					characterName = scn.next();
				}

				currentSheetEntryBuilder = new FaceSheetEntryBuilder(imagePath, category, name, order, characterName);
				canDescribeCurrentSheetEntry = true;
				yield true;
			}
			case CMD_DESC, CMD_DESC_S -> {
				if (currentSheetEntryBuilder == null || !canDescribeCurrentSheetEntry) {
					throw new FacePoolDefinitionException(
							CMD_DESC + " command not following " + CMD_ADD + " command or other " + CMD_DESC + " command", lineNumber);
				}

				if (!scn.hasNext()) {
					yield true;
				}

				if (currentSheetEntryBuilder.description == null) {
					currentSheetEntryBuilder.description = new ArrayList<>();
				}

				currentSheetEntryBuilder.description.add(scn.next());
				yield true;
			}
			case CMD_SKIP, CMD_SKIP_S -> {
				int advance = 1;
				if (scn.hasNextInt()) {
					advance = scn.nextInt();
				}

				canDescribeCurrentSheetEntry = false;
				if (currentSheetEntryBuilder == null) {
					sheetOffset += advance;
				} else {
					currentSheetEntryBuilder.advance += advance;
				}
				yield true;
			}
			default -> false;
		};
	}

	private @NotNull FacePoolDefinition end(int lineNumber) throws FacePoolDefinitionException {
		if (currentSectionID != SCNID_ROOT) {
			throw new FacePoolDefinitionException("Unterminated %s section".formatted(currentSectionName), lineNumber);
		}

		if (categories == null || sheets == null) {
			String msg;
			if (categories == null && sheets == null) {
				msg = "Missing %s and %s sections".formatted(SCN_CATS, SCN_SHEET);
			} else if (categories == null) {
				msg = "Missing %s section".formatted(SCN_CATS);
			} else /*if (sheets == null)*/ {
				msg = "Missing at least one %s section".formatted(SCN_SHEET);
			}
			throw new FacePoolDefinitionException(msg, lineNumber);
		}

		if (faceRecords != null) {
			LinkedHashMap<String, ArrayList<Integer>> undefinedCategoryRefs = null;
			for (var faceRecord : faceRecords.values()) {
				if (!categories.containsKey(faceRecord.category())) {
					if (undefinedCategoryRefs == null) {
						undefinedCategoryRefs = new LinkedHashMap<>();
					}
					undefinedCategoryRefs.computeIfAbsent(faceRecord.category(), ignored -> new ArrayList<>())
							.add(faceRecord.lineNumber());
				}
			}

			if (undefinedCategoryRefs != null && !undefinedCategoryRefs.isEmpty()) {
				var sb = new StringBuilder();
				boolean needLine = false;
				for (var entry : undefinedCategoryRefs.entrySet()) {
					if (entry.getValue().isEmpty()) {
						continue;
					}

					if (needLine) {
						sb.append(System.lineSeparator());
					}
					needLine = true;

					sb.append(" - \"").append(entry.getKey()).append("\" at line");
					if (entry.getValue().size() == 1) {
						sb.append(' ').append(entry.getValue().get(0));
					} else {
						sb.append("s ").append(entry.getValue().stream().map(Object::toString).collect(Collectors.joining(", ")));
					}
				}
				throw new FacePoolDefinitionException("Found references to undefined categories", sb.toString(), lineNumber);
			}
		}

		return new FacePoolDefinition(categories, sheets, poolDescription, poolCredits);
	}
}
