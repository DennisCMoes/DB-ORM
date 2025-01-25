package org.zenith.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String LOG_DIRECTORY = "logs";
    private static final String ACCESS_LOG_FILE = "access.log";
    private static final String ERROR_LOG_FILE = "error.log";

    public enum LogLevel {
        INFO,
        DEBUG,
        ERROR,
        WARN,
        FATAL
    }

    static {
        File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists()) {
            logDir.mkdir();
        }
    }

    private static void writeLog(LogLevel level, String message, String fileName) {
        PrintWriter writer = null;

        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logMessage = String.format("%s [%s] %s", timestamp, level.name(), message);

            File logFile = new File(LOG_DIRECTORY + File.separator + fileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            writer = new PrintWriter(new FileWriter(logFile, true));
            writer.println(logMessage);
        } catch (IOException ex) {
            System.err.println("Error writing to log file: " + ex.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void info(String message) {
        writeLog(LogLevel.INFO, message, ACCESS_LOG_FILE);
    }

    public static void debug(String message) {
        writeLog(LogLevel.DEBUG, message, ACCESS_LOG_FILE);
    }

    public static void error(String message) {
        writeLog(LogLevel.ERROR, message, ERROR_LOG_FILE);
    }

    public static void warn(String message) {
        writeLog(LogLevel.WARN, message, ERROR_LOG_FILE);
    }

    public static void fatal(String message) {
        writeLog(LogLevel.FATAL, message, ERROR_LOG_FILE);
    }

    public static void logException(Exception ex) {
        writeLog(LogLevel.ERROR, ex.toString(), ERROR_LOG_FILE);
        for (StackTraceElement element : ex.getStackTrace()) {
            writeLog(LogLevel.ERROR, "    at " + element.toString(), ERROR_LOG_FILE);
        }
    }
}
