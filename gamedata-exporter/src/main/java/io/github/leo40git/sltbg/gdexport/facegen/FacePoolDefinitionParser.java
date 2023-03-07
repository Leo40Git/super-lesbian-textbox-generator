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
    public static @NotNull FacePoolDefinition parse(@NotNull BufferedReader reader) throws IOException {
        var parser = new FacePoolDefinitionParser();
        int lineNumber = 1;

        String line;
        while ((line = reader.readLine()) != null) {
            parser.parseLine(line, lineNumber++);
        }

        return parser.end();
    }

    private static final String DELIMITER = "\t";
    private static final Pattern DELIMITER_PATTERN = Pattern.compile(DELIMITER, Pattern.LITERAL);
    private static final String COMMENT_PREFIX = ";";
    private static final String HEADER = "FACE POOL DEFINITION VERSION 1";

    private static final String CMD_NAME = "NAME";
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

    private static final int SCNID_HEAD = -2;
    private static final int SCNID_ROOT = -1;
    private static final int SCNID_DESC = 0;
    private static final int SCNID_CREDS = 1;
    private static final int SCNID_CATS = 2;
    private static final int SCNID_SHEET = 3;

    private int currentSectionID;
    private String currentSectionName;

    private String name;
    private ArrayList<String> poolDescription, poolCredits;
    private ArrayList<String> targetLines;

    private static final class FaceCategoryDefinitionBuilder {
        public final int lineNumber;
        public final @NotNull String name;
        public final long order;
        public final @Nullable String characterName;
        public @Nullable ArrayList<String> description;

        public FaceCategoryDefinitionBuilder(int lineNumber,
                                             @NotNull String name, long order, @Nullable String characterName) {
            this.lineNumber = lineNumber;
            this.name = name;
            this.order = order;
            this.characterName = characterName;
        }

        @Contract(" -> new")
        public @NotNull FaceCategoryDefinition build() {
            return new FaceCategoryDefinition(lineNumber, name, order, characterName, description);
        }
    }

    private FaceCategoryDefinitionBuilder currentCategoryBuilder;
    private HashMap<String, FaceCategoryDefinition> categories;
    private LinkedHashMap<String, ArrayList<Integer>> undefinedCategoryRefs;

    private static final class FaceDefinitionBuilder {
        public final int lineNumber;
        public final @NotNull String imagePath, category, name;
        public final long order;
        public final @Nullable String characterName;
        public @Nullable ArrayList<String> description;

        public FaceDefinitionBuilder(int lineNumber,
                                     @NotNull String imagePath, @NotNull String category, @NotNull String name, long order,
                                     @Nullable String characterName) {
            this.lineNumber = lineNumber;
            this.imagePath = imagePath;
            this.category = category;
            this.name = name;
            this.order = order;
            this.characterName = characterName;
        }

        @Contract(" -> new")
        public @NotNull FaceDefinition build() {
            return new FaceDefinition(imagePath, category, name, order, characterName, description);
        }
    }

    private String sheetInputPath;
    private HashMap<String, Integer> faceLinesByImagePath, faceLinesByFullName;
    private FaceDefinitionBuilder currentFaceBuilder;
    private boolean canDescribeCurrentFace;

    private FacePoolDefinitionParser() {
        currentSectionID = SCNID_HEAD;
        currentSectionName = null;

        name = null;
        poolDescription = null;
        poolCredits = null;
        targetLines = null;

        currentCategoryBuilder = null;
        categories = null;
        undefinedCategoryRefs = null;

        sheetInputPath = null;
        faceLinesByImagePath = null;
        faceLinesByFullName = null;
        currentFaceBuilder = null;
        canDescribeCurrentFace = false;
    }

    private void parseLine(@NotNull String line, int lineNumber) throws FacePoolDefinitionException {
        if (line.isEmpty() || line.startsWith(COMMENT_PREFIX) || line.isBlank()) {
            return;
        }

        if (currentSectionID == SCNID_HEAD) {
            if (!HEADER.equals(line)) {
                throw FacePoolDefinitionException.atLine("Missing header",
                        lineNumber, "(expected \"%s\", got \"%s\")".formatted(HEADER, line));
            }

            currentSectionID = SCNID_ROOT;
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
            throw FacePoolDefinitionException.atLine("Unexpected line \"%s\"".formatted(line), lineNumber);
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
        throw FacePoolDefinitionException.atLine("Tried to begin nested section", lineNumber);
    }

    private boolean parseLineRoot(@NotNull Scanner scn, int lineNumber) throws FacePoolDefinitionException {
        String cmd = scn.next();
        return switch (cmd) {
            case CMD_BEGIN -> {
                if (!scn.hasNext()) {
                    throw FacePoolDefinitionException.atLine(CMD_BEGIN + " command missing section parameter", lineNumber);
                }

                String newSectionName = scn.next();
                currentSectionID = switch (newSectionName) {
                    case SCN_DESC -> SCNID_DESC;
                    case SCN_CREDS -> SCNID_CREDS;
                    case SCN_CATS -> SCNID_CATS;
                    case SCN_SHEET -> SCNID_SHEET;
                    default ->
                            throw FacePoolDefinitionException.atLine("Unknown section \"%s\"".formatted(newSectionName), lineNumber);
                };
                currentSectionName = newSectionName;

                if (currentSectionID == SCNID_SHEET) {
                    if (!scn.hasNext()) {
                        throw FacePoolDefinitionException.atLine(CMD_BEGIN + " " + SCN_SHEET + " command missing sheet path parameter", lineNumber);
                    }
                    sheetInputPath = scn.next();
                }

                yield true;
            }
            case CMD_NAME -> {
                if (!scn.hasNext()) {
                    throw FacePoolDefinitionException.atLine(CMD_NAME + " command missing name parameter", lineNumber);
                }

                name = scn.next();
                yield true;
            }
            case CMD_END -> throw FacePoolDefinitionException.atLine("No section to end", lineNumber);
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
                    throw FacePoolDefinitionException.atLine(CMD_ADD + " command missing category name parameter", lineNumber);
                }
                String name = scn.next();

                var existingCategory = categories.get(name);
                if (existingCategory != null) {
                    throw FacePoolDefinitionException.atLine("Category \"" + name + "\" defined twice",
                            lineNumber, "(defined previously at line " + existingCategory.lineNumber + ")");
                }

                if (!scn.hasNextLong()) {
                    throw FacePoolDefinitionException.atLine(CMD_ADD + " command missing category order parameter", lineNumber);
                }
                long order = scn.nextLong();

                String characterName = null;
                if (scn.hasNext()) {
                    characterName = scn.next();
                }

                currentCategoryBuilder = new FaceCategoryDefinitionBuilder(lineNumber, name, order, characterName);
                yield true;
            }
            case CMD_DESC, CMD_DESC_S -> {
                if (currentCategoryBuilder == null) {
                    throw FacePoolDefinitionException.atLine("Unexpected " + CMD_DESC + " command",
                            lineNumber, "Must follow " + CMD_ADD + " command, or another " + CMD_DESC + " command");
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

    private void flushFace() {
        if (currentFaceBuilder == null) {
            return;
        }

        var category = categories.get(currentFaceBuilder.category);
        if (category != null) {
            category.addFace(currentFaceBuilder.build());
        } else {
            if (undefinedCategoryRefs == null) {
                undefinedCategoryRefs = new LinkedHashMap<>();
            }
            undefinedCategoryRefs.computeIfAbsent(currentFaceBuilder.category, unused -> new ArrayList<>())
                    .add(currentFaceBuilder.lineNumber);
        }
        assert category != null : "Flushing face with undeclared category \"" + currentFaceBuilder.category + "\" " +
                "(should've been caught at parseLineSheet)";

    }

    private boolean parseLineSheet(@NotNull Scanner scn, int lineNumber) throws FacePoolDefinitionException {
        assert sheetInputPath != null : "sheetInputPath == null?!";

        String cmd = scn.next();
        return switch (cmd) {
            case CMD_BEGIN -> throwNestedSectionException(lineNumber);
            case CMD_END -> {
                flushFace();
                endSection();
                yield true;
            }
            case CMD_ADD, CMD_ADD_S -> {
                flushFace();

                if (!scn.hasNext()) {
                    throw FacePoolDefinitionException.atLine(CMD_ADD + " command missing face image path parameter", lineNumber);
                }
                String imagePath = scn.next();

                if (faceLinesByImagePath == null) {
                    faceLinesByImagePath = new HashMap<>();
                    faceLinesByImagePath.put(imagePath, lineNumber);
                } else {
                    var oldLineNumber = faceLinesByImagePath.put(imagePath, lineNumber);
                    if (oldLineNumber != null) {
                        throw FacePoolDefinitionException.atLine("Face with image path \"" + imagePath + "\" defined twice",
                                lineNumber, "Defined previously at line " + oldLineNumber);
                    }
                }

                if (!scn.hasNext()) {
                    throw FacePoolDefinitionException.atLine(CMD_ADD + " command missing face category parameter", lineNumber);
                }
                String category = scn.next();

                if (!scn.hasNext()) {
                    throw FacePoolDefinitionException.atLine(CMD_ADD + " command missing face name parameter", lineNumber);
                }
                String name = scn.next();

                String fullName = category + Face.PATH_DELIMITER + name;
                if (faceLinesByFullName == null) {
                    faceLinesByFullName = new HashMap<>();
                    faceLinesByFullName.put(fullName, lineNumber);
                } else {
                    var oldLineNumber = faceLinesByFullName.put(fullName, lineNumber);
                    if (oldLineNumber != null) {
                        throw FacePoolDefinitionException.atLine("Face \"" + fullName + "\" defined twice",
                                lineNumber, "Defined previously at line " + oldLineNumber);
                    }
                }

                if (!scn.hasNextLong()) {
                    throw FacePoolDefinitionException.atLine(CMD_ADD + " command missing face order parameter", lineNumber);
                }
                long order = scn.nextLong();

                String characterName = null;
                if (scn.hasNext()) {
                    characterName = scn.next();
                }

                currentFaceBuilder = new FaceDefinitionBuilder(lineNumber, imagePath, category, name, order, characterName);
                canDescribeCurrentFace = true;
                yield true;
            }
            case CMD_DESC, CMD_DESC_S -> {
                if (currentFaceBuilder == null || !canDescribeCurrentFace) {
                    throw FacePoolDefinitionException.atLine(
                            CMD_DESC + " command not following " + CMD_ADD + " command or other " + CMD_DESC + " command", lineNumber);
                }

                if (!scn.hasNext()) {
                    yield true;
                }

                if (currentFaceBuilder.description == null) {
                    currentFaceBuilder.description = new ArrayList<>();
                }

                currentFaceBuilder.description.add(scn.next());
                yield true;
            }
            case CMD_SKIP, CMD_SKIP_S -> {
                // TODO
                throw new UnsupportedOperationException();
            }
            default -> false;
        };
    }

    private @NotNull FacePoolDefinition end() throws FacePoolDefinitionException {
        if (currentSectionID != SCNID_ROOT) {
            throw FacePoolDefinitionException.atEOF("Unterminated %s section".formatted(currentSectionName));
        }

        if (name == null) {
            throw FacePoolDefinitionException.atEOF("Missing name");
        }

        if (categories == null /*|| sheets == null*/) {
            String msg;
            if (categories == null /*&& sheets == null*/) {
                msg = "Missing %s and %s sections".formatted(SCN_CATS, SCN_SHEET);
            } else if (categories == null) {
                msg = "Missing %s section".formatted(SCN_CATS);
            } else /*if (sheets == null)*/ {
                msg = "Missing at least one %s section".formatted(SCN_SHEET);
            }
            throw FacePoolDefinitionException.atEOF(msg);
        }

        if (undefinedCategoryRefs != null) {
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
            throw FacePoolDefinitionException.atEOF("Found references to undefined categories", sb.toString());
        }



        return new FacePoolDefinition(name, categories, poolDescription, poolCredits);
    }
}
