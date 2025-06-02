package org.example.collaborativecodeeditor.logger;
import java.util.logging.*;
public class SimpleFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        return record.getMessage() + System.lineSeparator();
    }
}