package dev.danzou.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Java implementation of Kotlin Advent Utils to allow for use in Scala
 */
final public class Utils {
    private Utils() {
    }

    private final static String BASE_PATH = "inputs";
    private final static String FILE_PREFIX = "day";
    private final static String FILE_SUFFIX = ".in";

    static public List<String> readInput() throws IOException {
        return readInput(
                BASE_PATH + "/" + FILE_PREFIX + getExecutingDayNumber() + FILE_SUFFIX
        );
    }

    static public List<String> readInput(String name) throws IOException {
        return Files.readAllLines(Path.of(name));
    }

    static public @NotNull
    List<String> readInputLines() throws IOException {
        return readInput();
    }

    static public @NotNull
    String readInputString() throws IOException {
        return String.join("\n", readInputLines());
    }

    static public int getExecutingDayNumber() {
        try {
            throw new Exception();
        } catch (Exception e) {
            //noinspection OptionalGetWithoutIsPresent
            var fileName = Arrays.stream(e.getStackTrace())
                    .filter(stackTraceElement -> {
                        assert stackTraceElement.getFileName() != null;
                        return stackTraceElement.getFileName().startsWith("Day") ||
                                stackTraceElement.getFileName().startsWith("day");
                    })
                    .findFirst()
                    .get()
                    .getFileName();
            assert fileName != null;
            return Integer.parseInt(fileName.substring(3, fileName.indexOf('.')));
        }
    }
}
