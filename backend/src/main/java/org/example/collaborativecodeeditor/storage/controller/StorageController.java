package org.example.collaborativecodeeditor.storage.controller;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.service.UserService;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.security.service.ProjectRoleService;
import org.example.collaborativecodeeditor.storage.service.FileNode;
import org.example.collaborativecodeeditor.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.example.collaborativecodeeditor.storage.service.Compressor.zipDirectory;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    public StorageController() {
    }

    @PostMapping("/createProject/{projectName}")
    public ResponseEntity<String> createProject(@AuthenticationPrincipal OAuth2User oAuth2User, @PathVariable String projectName) throws IOException {
        User user = userService.getUser(oAuth2User.getAttribute("email"));
        storageService.createProject(user, projectName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/deleteProject/{projectName}")
    public ResponseEntity<String> deleteProject(@AuthenticationPrincipal OAuth2User oAuth2User, @PathVariable String projectName) throws IOException {
        User user = userService.getUser(oAuth2User.getAttribute("email"));
        storageService.deleteProject(user, projectName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/createDirectory")
    public ResponseEntity<String> createDirectory(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> directoryDetails) throws IOException {

        UUID projectUUID = UUID.fromString(directoryDetails.get("projectUUID").toString());

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path projectPath = Paths.get(project.getPath());
        Path fullPath = projectPath.resolve(directoryDetails.get("relativePath").toString());
        String directoryName = directoryDetails.get("directoryName").toString();

        storageService.createDirectory(fullPath, directoryName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/deleteComponent")
    public ResponseEntity<String> deleteComponent(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> details) throws IOException {

        UUID projectUUID = UUID.fromString(details.get("projectUUID").toString());

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path projectPath = Paths.get(project.getPath());
        Path fullPath = projectPath.resolve(details.get("relativePath").toString());
        String componentName = details.get("componentName").toString();

        storageService.deleteStorageComponent(fullPath, componentName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/renameComponent")
    public ResponseEntity<String> renameComponent(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> pathDetails) throws IOException {

        UUID projectUUID = UUID.fromString(pathDetails.get("projectUUID").toString());

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path projectPath = Paths.get(project.getPath());
        Path fullPath = projectPath.resolve(pathDetails.get("relativePath").toString());
        String componentName = pathDetails.get("componentName").toString();
        fullPath = fullPath.resolve(componentName);
        String newName = pathDetails.get("newName").toString();
        storageService.rename(fullPath.toString(), newName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/createFile")
    public ResponseEntity<String> createFile(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> fileDetails) throws IOException {

        UUID projectUUID = UUID.fromString(fileDetails.get("projectUUID").toString());

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path projectPath = Paths.get(project.getPath());
        Path fullPath = projectPath.resolve(fileDetails.get("relativePath").toString());
        String fileName = fileDetails.get("fileName").toString();

        storageService.createFile(fullPath, fileName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/getFile")
    public ResponseEntity<String> getFileContent(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID projectUUID, @RequestParam("filePath") String filePath) throws IOException {

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR, Role.VIEWER))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rootPath;
        String fileName;
        if (filePath.contains("/")) {
            int lastSlashIndex = filePath.lastIndexOf("/");
            rootPath = filePath.substring(0, lastSlashIndex);
            fileName = filePath.substring(lastSlashIndex + 1);
        } else {
            rootPath = "";
            fileName = filePath;
        }

        Path projectPath = Path.of(project.getPath());
        Path fullPath = projectPath.resolve(rootPath);
        return ResponseEntity.ok().body(storageService.getFileContent(fullPath, fileName));
    }

    @GetMapping("/projectStructure/{projectUUID}")
    public ResponseEntity<FileNode> projectStructure(@AuthenticationPrincipal OAuth2User oAuth2User, @PathVariable("projectUUID") UUID projectUUID) throws IOException {

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR, Role.VIEWER))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FileNode result = storageService.getDirStructure(Path.of(project.getPath()));
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/download-zip")
    public ResponseEntity<byte[]> downloadZippedFolder(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID projectUUID) throws IOException {

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR, Role.VIEWER))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Files.exists(Path.of(project.getPath())) || !Files.isDirectory(Path.of(project.getPath()))) {
            throw new FileNotFoundException("Project not found: " + project.getName());
        }

        ByteArrayOutputStream zipped = zipDirectory(Path.of(project.getPath()));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + project.getName() + ".zip\"")
                .body(zipped.toByteArray());
    }

}
