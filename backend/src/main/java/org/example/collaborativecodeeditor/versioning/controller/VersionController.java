package org.example.collaborativecodeeditor.versioning.controller;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.model.projectversions.ProjectVersion;
import org.example.collaborativecodeeditor.model.projectversions.service.ProjectVersionService;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.service.UserService;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.security.service.ProjectRoleService;
import org.example.collaborativecodeeditor.storage.service.StorageService;
import org.example.collaborativecodeeditor.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/versionControl")
public class VersionController {

    @Autowired
    private VersionService versionService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectVersionService projectVersionService;

    @PostMapping("/commit")
    public ResponseEntity<String> commitVersion(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("versionName") String versionName, @RequestParam("projectUUID") UUID projectUUID) throws IOException {

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        versionService.createNewVersion(versionName, projectUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getCommits")
    public ResponseEntity<List<HashMap<String, Object>>> getCommits(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID projectUUID) throws IOException {

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(versionService.getVersionsOfProject(project));
    }

    @PostMapping("/revert")
    public ResponseEntity<String> revertVersion(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID projectUUID, @RequestParam("commitUUID") UUID commitUUID) throws IOException {

        Project project = projectService.getProject(projectUUID);

        String email = oAuth2User.getAttribute("email");

        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), email, List.of(Role.OWNER, Role.EDITOR))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProjectVersion projectVersion = projectVersionService.getProjectVersion(commitUUID);

        versionService.revertCommit(projectVersion, project);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forkProject")
    public ResponseEntity<String> createProject(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID forkedProjectUUID) throws IOException {

        User user = userService.getUser(oAuth2User.getAttribute("email"));
        Project forkedProject = projectService.getProject(forkedProjectUUID);
        String forkName = forkedProject.getName() + "-fork";

        Project project = storageService.createProject(user, forkName);

        storageService.copyDirectoryContent(forkedProject.getPath(), project.getPath());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}