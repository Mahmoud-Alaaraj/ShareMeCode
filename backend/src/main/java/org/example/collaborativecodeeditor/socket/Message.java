package org.example.collaborativecodeeditor.socket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private String content;
    private String projectUUID;
    private String filePath;

    public Message() {
    }

    public Message(String content) {
        this.content = content;
    }
}