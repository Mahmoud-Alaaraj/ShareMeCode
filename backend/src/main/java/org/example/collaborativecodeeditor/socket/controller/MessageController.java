package org.example.collaborativecodeeditor.socket.controller;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.socket.Message;
import org.example.collaborativecodeeditor.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Controller
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ProjectService projectService;

    public MessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/communication")
    public void sendToChannel(Message message) throws IOException {
        String projectUUID = message.getProjectUUID();
        String filePath = message.getFilePath();

        Project project = projectService.getProject(UUID.fromString(projectUUID));

        String projectPath = project.getPath();
        Path fullFilePath = Path.of(projectPath).resolve(filePath);
        storageService.setFileContent(fullFilePath, message.getContent());
        messagingTemplate.convertAndSend("/topic/" + projectUUID + "/" + filePath, message);
    }
}