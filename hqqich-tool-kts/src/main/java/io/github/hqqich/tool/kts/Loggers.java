package io.github.hqqich.tool.kts;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

public final class Loggers {

    private static final String DEFAULT_FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger - %msg";
    private static final String DEFAULT_MAX_FILE_SIZE = "10MB";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static volatile FileLogWriter fileLogWriter;

    private Loggers() {
    }

    public static void enableFile(String filePath) {
        enableFile(filePath, true);
    }

    public static void enableFile(String filePath, boolean append) {
        validateFilePath(filePath);
        fileLogWriter = FileLogWriter.forSingleFile(filePath, DEFAULT_FILE_PATTERN, append);
    }

    public static void enableFile(String filePath, String pattern) {
        enableFile(filePath, pattern, true);
    }

    public static void enableFile(String filePath, String pattern, boolean append) {
        validateFilePath(filePath);
        String actualPattern = pattern == null || pattern.trim().isEmpty() ? DEFAULT_FILE_PATTERN : pattern;
        fileLogWriter = FileLogWriter.forSingleFile(filePath, actualPattern, append);
    }

    public static void enableRollingFile(String filePath) {
        enableRollingFile(filePath, DEFAULT_FILE_PATTERN, DEFAULT_MAX_FILE_SIZE, true);
    }

    public static void enableRollingFile(String filePath, boolean append) {
        enableRollingFile(filePath, DEFAULT_FILE_PATTERN, DEFAULT_MAX_FILE_SIZE, append);
    }

    public static void enableRollingFile(String filePath, String pattern, String maxFileSize, boolean append) {
        validateFilePath(filePath);
        if (maxFileSize == null || maxFileSize.trim().isEmpty()) {
            throw new IllegalArgumentException("maxFileSize must not be blank");
        }
        String actualPattern = pattern == null || pattern.trim().isEmpty() ? DEFAULT_FILE_PATTERN : pattern;
        fileLogWriter = FileLogWriter.forRollingFile(filePath, actualPattern, parseMaxFileSize(maxFileSize), append);
    }

    public static void disableFile() {
        FileLogWriter writer = fileLogWriter;
        fileLogWriter = null;
        if (writer != null) {
            writer.close();
        }
    }

    public static KtsLogger get(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return new KtsLogger(LoggerFactory.getLogger(type));
    }

    public static KtsLogger get(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new KtsLogger(LoggerFactory.getLogger(name));
    }

    public static org.slf4j.Logger getRaw(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return LoggerFactory.getLogger(type);
    }

    public static org.slf4j.Logger getRaw(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return LoggerFactory.getLogger(name);
    }

    static void logToFile(Level level, String loggerName, String message) {
        FileLogWriter writer = fileLogWriter;
        if (writer == null) {
            return;
        }
        writer.write(level, loggerName, message, null);
    }

    static void logToFile(Level level, String loggerName, String message, Throwable throwable) {
        FileLogWriter writer = fileLogWriter;
        if (writer == null) {
            return;
        }
        writer.write(level, loggerName, message, throwable);
    }

    static void logToFile(Level level, String loggerName, String format, Object... arguments) {
        FileLogWriter writer = fileLogWriter;
        if (writer == null) {
            return;
        }
        if (arguments == null || arguments.length == 0) {
            writer.write(level, loggerName, format, null);
            return;
        }
        Throwable throwable = null;
        if (arguments[arguments.length - 1] instanceof Throwable) {
            throwable = (Throwable) arguments[arguments.length - 1];
        }
        String message = MessageFormatter.arrayFormat(format, arguments).getMessage();
        writer.write(level, loggerName, message, throwable);
    }

    private static void validateFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be blank");
        }
    }

    private static long parseMaxFileSize(String maxFileSize) {
        String normalized = maxFileSize.trim().toUpperCase();
        if (normalized.endsWith("KB")) {
            return Long.parseLong(normalized.substring(0, normalized.length() - 2).trim()) * 1024L;
        }
        if (normalized.endsWith("MB")) {
            return Long.parseLong(normalized.substring(0, normalized.length() - 2).trim()) * 1024L * 1024L;
        }
        if (normalized.endsWith("GB")) {
            return Long.parseLong(normalized.substring(0, normalized.length() - 2).trim()) * 1024L * 1024L * 1024L;
        }
        return Long.parseLong(normalized);
    }

    private static final class FileLogWriter {

        private final Path filePath;
        private final String pattern;
        private final boolean rolling;
        private final long maxFileSize;
        private final boolean append;
        private final Object lock = new Object();

        private Writer writer;
        private LocalDate currentDate;
        private int currentIndex;
        private Path currentWritingPath;

        private FileLogWriter(Path filePath, String pattern, boolean rolling, long maxFileSize, boolean append) {
            this.filePath = filePath;
            this.pattern = pattern;
            this.rolling = rolling;
            this.maxFileSize = maxFileSize;
            this.append = append;
        }

        static FileLogWriter forSingleFile(String filePath, String pattern, boolean append) {
            return new FileLogWriter(Paths.get(filePath), pattern, false, Long.MAX_VALUE, append);
        }

        static FileLogWriter forRollingFile(String filePath, String pattern, long maxFileSize, boolean append) {
            return new FileLogWriter(Paths.get(filePath), pattern, true, maxFileSize, append);
        }

        void write(Level level, String loggerName, String message, Throwable throwable) {
            synchronized (lock) {
                try {
                    ensureWriter();
                    writer.write(formatLine(level, loggerName, message));
                    if (throwable != null) {
                        writer.write(System.lineSeparator());
                        writer.write(throwableToString(throwable));
                    }
                    writer.write(System.lineSeparator());
                    writer.flush();
                } catch (IOException exception) {
                    throw new IllegalStateException("failed to write log file: " + filePath, exception);
                }
            }
        }

        void close() {
            synchronized (lock) {
                closeCurrentWriter();
            }
        }

        private void ensureWriter() throws IOException {
            if (!rolling) {
                if (writer == null) {
                    openSingleFileWriter();
                }
                return;
            }

            LocalDate today = LocalDate.now();
            if (writer == null || !Objects.equals(currentDate, today) || isCurrentFileFull()) {
                rollover(today);
            }
        }

        private void openSingleFileWriter() throws IOException {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            writer = new OutputStreamWriter(
                    Files.newOutputStream(
                            filePath,
                            StandardOpenOption.CREATE,
                            append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING
                    ),
                    StandardCharsets.UTF_8
            );
            currentWritingPath = filePath;
        }

        private void rollover(LocalDate today) throws IOException {
            closeCurrentWriter();
            currentDate = today;
            currentIndex = findInitialIndex(today);
            currentWritingPath = buildRollingPath(today, currentIndex);
            Path parent = currentWritingPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            writer = new OutputStreamWriter(
                    Files.newOutputStream(
                            currentWritingPath,
                            StandardOpenOption.CREATE,
                            append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING
                    ),
                    StandardCharsets.UTF_8
            );
        }

        private int findInitialIndex(LocalDate today) throws IOException {
            if (!append) {
                return 0;
            }
            int index = 0;
            while (true) {
                Path candidate = buildRollingPath(today, index);
                if (!Files.exists(candidate)) {
                    return index;
                }
                if (Files.size(candidate) < maxFileSize) {
                    return index;
                }
                index++;
            }
        }

        private boolean isCurrentFileFull() throws IOException {
            return currentWritingPath != null && Files.exists(currentWritingPath) && Files.size(currentWritingPath) >= maxFileSize;
        }

        private Path buildRollingPath(LocalDate date, int index) {
            String fileName = filePath.getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            String dateText = date.toString();
            String targetName;
            if (dotIndex > 0) {
                targetName = fileName.substring(0, dotIndex) + "." + dateText + "." + index + fileName.substring(dotIndex);
            } else {
                targetName = fileName + "." + dateText + "." + index + ".log";
            }
            Path parent = filePath.getParent();
            return parent == null ? Paths.get(targetName) : parent.resolve(targetName);
        }

        private void closeCurrentWriter() {
            if (writer == null) {
                return;
            }
            try {
                writer.close();
            } catch (IOException ignored) {
            }
            writer = null;
        }

        private String formatLine(Level level, String loggerName, String message) {
            String actualPattern = pattern == null || pattern.trim().isEmpty() ? DEFAULT_FILE_PATTERN : pattern;
            String text = actualPattern;
            text = text.replace("%d{yyyy-MM-dd HH:mm:ss.SSS}", DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            text = text.replace("%-5level", padLevel(level));
            text = text.replace("%level", level == null ? "INFO" : level.name());
            text = text.replace("%thread", Thread.currentThread().getName());
            text = text.replace("%logger", loggerName);
            text = text.replace("%msg", message);
            text = text.replace("%n", "");
            return text;
        }

        private String throwableToString(Throwable throwable) {
            StringBuilder builder = new StringBuilder();
            builder.append(throwable.toString());
            for (StackTraceElement element : throwable.getStackTrace()) {
                builder.append(System.lineSeparator()).append("\tat ").append(element);
            }
            Throwable cause = throwable.getCause();
            if (cause != null && cause != throwable) {
                builder.append(System.lineSeparator()).append("Caused by: ").append(throwableToString(cause));
            }
            return builder.toString();
        }

        private String padLevel(Level level) {
            String text = level == null ? "INFO" : level.name();
            if (text.length() >= 5) {
                return text;
            }
            StringBuilder builder = new StringBuilder(text);
            while (builder.length() < 5) {
                builder.append(' ');
            }
            return builder.toString();
        }
    }
}
