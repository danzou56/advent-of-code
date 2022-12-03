package dev.danzou.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java implementation of Kotlin Advent Utils to allow for use in Scala
 */
final public class Utils {
    private final static String FILE_PREFIX = "day";

    private Utils() {
    }

    static public List<String> readFileLines(String name) {
        try {
            return Files.readAllLines(Path.of(name));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("occurred while reading " + name + "; returning empty list instead");
            return Collections.emptyList();
        }
    }

    static public @NotNull
    List<Long> readOutputLines() {
        var fileName = "outputs/" + FILE_PREFIX + getExecutingDayNumber() + ".out";
        var lines = readFileLines(fileName);
        if (lines.size() != 2) {
            System.out.println("Expected 2 lines in file " + fileName + " but there were actually " + lines.size());
            return Arrays.asList(null, null);
        }
        else return lines.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    static public @NotNull
    List<String> readInputLines() {
        return readFileLines(
                "inputs/" + FILE_PREFIX + getExecutingDayNumber() + ".in"
        );
    }

    static public @NotNull
    String readInputString() {
        return String.join("\n", readInputLines());
    }

    /**
     * Returns day number that is currently executing. A pretty big hack.
     * @throws java.util.NoSuchElementException if no stack trace file name starts with "Day" or
     * "day"
     * @return executing day number
     */
    static public int getExecutingDayNumber() {
        try {
            throw new Exception();
        } catch (Exception e) {
            //noinspection OptionalGetWithoutIsPresent
            var fileName = Arrays.stream(e.getStackTrace())
                    .filter(stackTraceElement -> {
                        assert stackTraceElement.getFileName() != null;
                        return stackTraceElement.getFileName().toLowerCase().startsWith("day");
                    })
                    .findFirst()
                    .get()
                    .getFileName();
            assert fileName != null;
            return Integer.parseInt(fileName.substring(3, fileName.indexOf('.')));
        }
    }
}
