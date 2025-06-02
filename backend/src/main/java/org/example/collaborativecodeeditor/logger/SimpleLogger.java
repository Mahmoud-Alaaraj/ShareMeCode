package org.example.collaborativecodeeditor.logger;

import java.util.logging.*;

public class SimpleLogger {
    private static final Logger logger = Logger.getLogger(SimpleLogger.class.getName());
    private static SimpleLogger singletonLogger = null;
    private SimpleLogger() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    public static SimpleLogger getLogger() {
        if (singletonLogger == null) {
            singletonLogger = new SimpleLogger();
        }
        return singletonLogger;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }
}