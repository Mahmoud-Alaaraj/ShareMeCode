package org.example.collaborativecodeeditor.execution.service;

import org.example.collaborativecodeeditor.execution.dto.CodeMessage;
import org.example.collaborativecodeeditor.execution.dto.OutputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ExecutionService {
    private final Map<String, CompletableFuture<String>> futures = new ConcurrentHashMap<>();
    @Autowired
    private CodePublisher defaultCodePublisher;

    public String submitCode(CodeMessage codeMessage) throws Exception {
        String submitId = UUID.randomUUID().toString();
        CompletableFuture<String> future = new CompletableFuture<>();
        futures.put(submitId, future);
        codeMessage.setSubmitID(submitId);
        defaultCodePublisher.sendCode(codeMessage);

        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            futures.remove(submitId);
            return "Execution timed out";
        }
    }

    public void handleResult(OutputMessage outputMessage){
        CompletableFuture<String> future = futures.remove(outputMessage.getSubmitID());
        if (future != null) {
            future.complete(outputMessage.getOutput());
        }
    }

}
