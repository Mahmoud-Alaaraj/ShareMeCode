package org.example.collaborativecodeeditor.execution.controller;

import org.example.collaborativecodeeditor.execution.dto.CodeMessage;
import org.example.collaborativecodeeditor.execution.service.ExecutionService;
import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.security.service.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/executor")
public class ExecutionController {

    @Autowired
    private ExecutionService executionService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private ProjectService projectService;

    @PostMapping("/executeFile")
    public ResponseEntity<String> executeFile(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> details) throws Exception {

        String email = oAuth2User.getAttribute("email");
        UUID projectUUID = UUID.fromString(details.get("projectUUID").toString());
        Project project = projectService.getProject(projectUUID);

        if (!projectRoleService.hasRoleOnProject(project.getPath(), email, Role.OWNER) && !projectRoleService.hasRoleOnProject(project.getPath(), email, Role.EDITOR)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path projectPath = Path.of(project.getPath());

        CodeMessage codeMessage = CodeMessage
                .builder()
                .projectPath(projectPath.toString())
                .filePath(details.get("filePath").toString())
                .language(details.get("language").toString())
                .inputData(details.get("inputData").toString())
                .build();

        String output = executionService.submitCode(codeMessage);
        return ResponseEntity.ok().body(output);
    }
}
