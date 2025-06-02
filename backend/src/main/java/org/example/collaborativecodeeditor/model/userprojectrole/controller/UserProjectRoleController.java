package org.example.collaborativecodeeditor.model.userprojectrole.controller;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.service.ProjectService;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.service.UserService;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.model.userprojectrole.service.UserProjectRoleService;
import org.example.collaborativecodeeditor.security.service.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/project")
public class UserProjectRoleController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProjectRoleService userProjectRoleService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRoleService projectRoleService;

    @GetMapping("/getProjects")
    public List<HashMap<String, Object>> getProjects(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        User user = userService.getUser(email);
        return userProjectRoleService.getProjectsOfUser(user);
    }

    @GetMapping("/getUsers")
    public List<HashMap<String, Object>> getUsers(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("projectUUID") UUID projectUUID) {

        User user = userService.getUser(oAuth2User.getAttribute("email"));
        Project project = projectService.getProject(projectUUID);
        if (!projectRoleService.hasAnyRoleOnProject(project.getPath(), user.getEmail(), List.of(Role.OWNER, Role.EDITOR, Role.VIEWER))) {
            throw new IllegalArgumentException("You do not have permission to access this feature");
        }
        return userProjectRoleService.getUsersOfProject(project);
    }

}
