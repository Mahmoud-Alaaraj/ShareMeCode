package org.example.collaborativecodeeditor.execution.service;

import org.example.collaborativecodeeditor.execution.dto.CodeMessage;

public interface CodePublisher {
    void sendCode(CodeMessage codeMessage);
}
