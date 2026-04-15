package io.github.hqqich.tool.kts;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import java.nio.charset.StandardCharsets;
import org.slf4j.LoggerFactory;

public final class Loggers {

    private static final String DEFAULT_FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n";
    private static final String DEFAULT_MAX_FILE_SIZE = "10MB";
    private static final LoggerContext LOGGER_CONTEXT = (LoggerContext) LoggerFactory.getILoggerFactory();

    private Loggers() {
    }

    public static void enableFile(String filePath) {
        enableFile(filePath, DEFAULT_FILE_PATTERN, true);
    }

    public static void enableFile(String filePath, boolean append) {
        enableFile(filePath, DEFAULT_FILE_PATTERN, append);
    }

    public static void enableFile(String filePath, String pattern) {
        enableFile(filePath, pattern, true);
    }

    public static void enableFile(String filePath, String pattern, boolean append) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be blank");
        }
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("pattern must not be blank");
        }

        Logger rootLogger = LOGGER_CONTEXT.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        String appenderName = getFileAppenderName(filePath);
        if (rootLogger.getAppender(appenderName) != null) {
            return;
        }

        PatternLayoutEncoder encoder = createEncoder(pattern);

        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(LOGGER_CONTEXT);
        appender.setName(appenderName);
        appender.setFile(filePath);
        appender.setAppend(append);
        appender.setEncoder(encoder);
        appender.start();

        rootLogger.addAppender(appender);
    }

    public static void enableRollingFile(String filePath) {
        enableRollingFile(filePath, DEFAULT_FILE_PATTERN, DEFAULT_MAX_FILE_SIZE, true);
    }

    public static void enableRollingFile(String filePath, boolean append) {
        enableRollingFile(filePath, DEFAULT_FILE_PATTERN, DEFAULT_MAX_FILE_SIZE, append);
    }

    public static void enableRollingFile(String filePath, String pattern, String maxFileSize, boolean append) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be blank");
        }
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("pattern must not be blank");
        }
        if (maxFileSize == null || maxFileSize.trim().isEmpty()) {
            throw new IllegalArgumentException("maxFileSize must not be blank");
        }

        Logger rootLogger = LOGGER_CONTEXT.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        String appenderName = getRollingAppenderName(filePath);
        if (rootLogger.getAppender(appenderName) != null) {
            return;
        }

        PatternLayoutEncoder encoder = createEncoder(pattern);

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(LOGGER_CONTEXT);
        appender.setName(appenderName);
        appender.setFile(filePath);
        appender.setAppend(append);
        appender.setEncoder(encoder);

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(LOGGER_CONTEXT);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(buildRollingFilePattern(filePath));
        rollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        rollingPolicy.start();

        appender.setRollingPolicy(rollingPolicy);
        appender.start();

        rootLogger.addAppender(appender);
    }

    public static KtsLogger get(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return new KtsLogger(toLogbackLogger(LoggerFactory.getLogger(type)));
    }

    public static KtsLogger get(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new KtsLogger(LOGGER_CONTEXT.getLogger(name));
    }

    public static Logger getRaw(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return toLogbackLogger(LoggerFactory.getLogger(type));
    }

    public static Logger getRaw(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return LOGGER_CONTEXT.getLogger(name);
    }

    private static Logger toLogbackLogger(org.slf4j.Logger logger) {
        if (logger instanceof Logger) {
            return (Logger) logger;
        }
        throw new IllegalStateException("current logger implementation is not logback");
    }

    private static String getFileAppenderName(String filePath) {
        return "KTS_FILE_" + Integer.toHexString(filePath.hashCode());
    }

    private static String getRollingAppenderName(String filePath) {
        return "KTS_ROLLING_FILE_" + Integer.toHexString(filePath.hashCode());
    }

    private static PatternLayoutEncoder createEncoder(String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(LOGGER_CONTEXT);
        encoder.setPattern(pattern);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        return encoder;
    }

    private static String buildRollingFilePattern(String filePath) {
        int slashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > slashIndex) {
            return filePath.substring(0, dotIndex) + ".%d{yyyy-MM-dd}.%i" + filePath.substring(dotIndex);
        }
        return filePath + ".%d{yyyy-MM-dd}.%i.log";
    }
}
