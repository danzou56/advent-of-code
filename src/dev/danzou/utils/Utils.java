package dev.danzou.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    static public int getExecutingDayNumber() {
        try {
            throw new Exception();
        } catch (Exception e) {
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
