package io.github.hqqich.tool.kts;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import java.nio.charset.StandardCharsets;
import org.slf4j.LoggerFactory;

public final class Loggers {

    private static final String DEFAULT_FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n";
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

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(LOGGER_CONTEXT);
        encoder.setPattern(pattern);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(LOGGER_CONTEXT);
        appender.setName(appenderName);
        appender.setFile(filePath);
        appender.setAppend(append);
        appender.setEncoder(encoder);
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
}
