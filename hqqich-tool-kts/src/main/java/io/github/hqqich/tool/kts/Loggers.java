package io.github.hqqich.tool.kts;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public final class Loggers {

    private static final LoggerContext LOGGER_CONTEXT = (LoggerContext) LoggerFactory.getILoggerFactory();

    private Loggers() {
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
}
