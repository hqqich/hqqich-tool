package io.github.hqqich.tool.kts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class TeeOutputStream extends OutputStream {

    public static final String INFO_LOG_FILE = "logs/info.log";

    private final OutputStream left;
    private final OutputStream right;

    public TeeOutputStream(OutputStream left, OutputStream right) {
        this.left = Objects.requireNonNull(left, "left must not be null");
        this.right = Objects.requireNonNull(right, "right must not be null");
    }

    public static void configureLogging() {
        configureLogging(INFO_LOG_FILE);
    }

    public static void configureLogging(String logFilePath) {
        Objects.requireNonNull(logFilePath, "logFilePath must not be null");

        File logFile = new File(logFilePath);
        File parentFile = logFile.getParentFile();
        if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalStateException("failed to create log directory: " + parentFile);
        }

        try {
            OutputStream fileStream = new FileOutputStream(logFile, true);
            PrintStream outPrintStream = new PrintStream(
                    new TeeOutputStream(System.out, fileStream),
                    true,
                    StandardCharsets.UTF_8.name()
            );
            PrintStream errPrintStream = new PrintStream(
                    new TeeOutputStream(System.err, fileStream),
                    true,
                    StandardCharsets.UTF_8.name()
            );

            System.setOut(outPrintStream);
            System.setErr(errPrintStream);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to configure logging: " + logFilePath, exception);
        }

        System.setProperty("org.slf4j.simpleLogger.logFile", "System.err");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss.SSS");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "true");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
    }

    @Override
    public void write(int b) throws IOException {
        left.write(b);
        right.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        left.write(b, off, len);
        right.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        left.flush();
        right.flush();
    }
}
