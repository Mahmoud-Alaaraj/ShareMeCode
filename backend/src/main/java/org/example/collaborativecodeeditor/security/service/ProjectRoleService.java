package org.example.collaborativecodeeditor.security.service;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.repository.ProjectRepository;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.repository.UserRepository;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.model.userprojectrole.UserProjectRole;
import org.example.collaborativecodeeditor.model.userprojectrole.repository.UserProjectRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectRoleService {

    @Autowired
    UserProjectRoleRepository userProjectRoleRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    UserRepository userRepository;

    public Boolean hasRoleOnProject(String projectPath, String email, Role role) {
        List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findByUserEmailAndProjectPath(email, projectPath);
        if (userProjectRoles.isEmpty()) {
            return false;
        }
        return userProjectRoles.getLast().getRole().equals(role);
    }

    public Boolean hasAnyRoleOnProject(String projectPath, String email, List<Role> roles) {
        for (Role role : roles) {
            if (hasRoleOnProject(projectPath, email, role)) {
                return true;
            }
        }
        return false;
    }

    public void giveRoleOnProject(String projectPath, String email, Role role) {
        Optional<Project> projectOptional = projectRepository.findById(projectPath);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();
        Project project = projectOptional.get();

        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setUser(user);
        userProjectRole.setProject(project);
        userProjectRole.setRole(role);
        userProjectRoleRepository.save(userProjectRole);
    }

    public void removeRoleOnProject(String projectPath, String email, Role role) {
        List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findByUserEmailAndProjectPath(email, projectPath);
        for (UserProjectRole userProjectRole : userProjectRoles) {
            if (userProjectRole.getRole().equals(role)) {
                userProjectRoleRepository.delete(userProjectRole);
            }
        }
    }

    public void changeRoleOnProject(String projectPath, String email, Role oldRole, Role newRole) {
        removeRoleOnProject(projectPath, email, oldRole);
        giveRoleOnProject(projectPath, email, newRole);
    }

}
