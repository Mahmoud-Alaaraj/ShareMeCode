package org.example.executionserver.enums;

public enum MessageQueueConstants {
    CODE_KEY("Key1"),
    OUTPUT_KEY("Key2"),
    CODE_QUEUE("codeQueue"),
    EXCHANGE_NAME("exchange1"),
    OUTPUT_QUEUE("outputQueue");

    private final String value;

    MessageQueueConstants(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}
