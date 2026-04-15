package io.github.hqqich.tool.kts;

import ch.qos.logback.classic.Logger;

public final class KtsLogger {

    private final Logger logger;

    KtsLogger(Logger logger) {
        this.logger = logger;
    }

    public static KtsLogger of(Class<?> type) {
        return Loggers.get(type);
    }

    public static KtsLogger of(String name) {
        return Loggers.get(name);
    }

    public Logger unwrap() {
        return logger;
    }

    public String getName() {
        return logger.getName();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void trace(String message) {
        logger.trace(message);
    }

    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    public void trace(String message, Throwable throwable) {
        logger.trace(message, throwable);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
