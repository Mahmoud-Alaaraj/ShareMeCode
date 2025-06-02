package org.example.collaborativecodeeditor.model.project.controller;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.security.service.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@Controller
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private ProjectService projectService;

    @GetMapping("/{projectUUID}/{roleForm}")
    public String openProject(@AuthenticationPrincipal OAuth2User oAuth2User, @PathVariable("projectUUID") UUID projectUUID, @PathVariable("roleForm") String roleForm) {

        Project project = projectService.getProject(projectUUID);
        String userEmail = oAuth2User.getAttribute("email");

        HashMap<String, Role> mappingRoles = new HashMap<>();
        mappingRoles.put("admin", Role.OWNER);
        mappingRoles.put("editor", Role.EDITOR);
        mappingRoles.put("viewer", Role.VIEWER);

        if (projectRoleService.hasRoleOnProject(project.getPath(), userEmail, mappingRoles.getOrDefault(roleForm.toLowerCase(), Role.OWNER))) {
            return "redirect:/editor?projectUUID=" + projectUUID + "&role=" + roleForm;
        }
        else {
            throw new IllegalArgumentException("You do not have permission to access this project");
        }
    }
}
