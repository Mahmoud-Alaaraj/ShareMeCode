package org.example.collaborativecodeeditor.security.controller;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.security.service.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
public class ProjectRoleController {

    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private ProjectService projectService;

    @PostMapping("/give")
    public void giveRoleOnProject(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID projectUUID, @RequestBody HashMap<String, Object> details) {

        Project project = projectService.getProject(projectUUID);

        String giverEmail = oAuth2User.getAttribute("email");
        String email = details.get("email").toString();
        String projectPath = project.getPath();
        Role role = Role.valueOf(details.get("role").toString().toUpperCase());
        if (!projectRoleService.hasRoleOnProject(projectPath, giverEmail, Role.OWNER)) {
            throw new IllegalArgumentException("You do not have permission to give role on this project");
        }
        projectRoleService.giveRoleOnProject(projectPath, email, role);
    }

    @PostMapping("/remove")
    public void removeRoleOnProject(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> details) {
        String giverEmail = oAuth2User.getAttribute("email");
        String email = details.get("email").toString();
        String projectPath = details.get("projectPath").toString();
        Role role = (Role) details.get("role");
        if (!projectRoleService.hasRoleOnProject(projectPath, giverEmail, Role.OWNER)) {
            throw new IllegalArgumentException("You do not have permission to remove role on this project");
        }
        projectRoleService.removeRoleOnProject(projectPath, email, role);
    }

    @PostMapping("/change")
    public void changeRoleOnProject(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestBody HashMap<String, Object> details) {
        String giverEmail = oAuth2User.getAttribute("email");
        String email = details.get("email").toString();
        String projectPath = details.get("projectPath").toString();
        Role oldRole = (Role) details.get("oldRole");
        Role newRole = (Role) details.get("newRole");
        if (!projectRoleService.hasRoleOnProject(projectPath, giverEmail, Role.OWNER)) {
            throw new IllegalArgumentException("You do not have permission to change role on this project");
        }
        projectRoleService.changeRoleOnProject(projectPath, email, oldRole, newRole);
    }
}
