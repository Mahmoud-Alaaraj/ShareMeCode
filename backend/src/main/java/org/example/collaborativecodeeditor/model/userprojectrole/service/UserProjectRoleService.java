package org.example.collaborativecodeeditor.model.userprojectrole.service;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.repository.UserRepository;
import org.example.collaborativecodeeditor.model.userprojectrole.UserProjectRole;
import org.example.collaborativecodeeditor.model.userprojectrole.repository.UserProjectRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserProjectRoleService {

    @Autowired
    private UserProjectRoleRepository userProjectRoleRepository;
    @Autowired
    private UserRepository userRepository;

    public List<HashMap<String, Object>> getProjectsOfUser(User user) {
        List<UserProjectRole> projects = userProjectRoleRepository.findByUserId(user.getId());
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (UserProjectRole userProjectRole : projects) {

            String ownerEmail = userProjectRole.getProject().getOwner().getEmail();
            Optional<User> ownerOptional = userRepository.findByEmail(ownerEmail);
            if (ownerOptional.isEmpty()) {
                continue;
            }

            User owner = ownerOptional.get();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("project_name", userProjectRole.getProject().getName());
            hashMap.put("owner_name", owner.getName());
            hashMap.put("project_uuid", userProjectRole.getProject().getProjectUUID());
            hashMap.put("role", userProjectRole.getRole());
            result.add(hashMap);
        }
        return result;
    }

    public List<HashMap<String, Object>> getUsersOfProject(Project project) {
        List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findByProjectPath(project.getPath());
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (UserProjectRole userProjectRole : userProjectRoles) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("user_name", userProjectRole.getUser().getName());
            hashMap.put("user_email", userProjectRole.getUser().getEmail());
            hashMap.put("role", userProjectRole.getRole());
            result.add(hashMap);
        }
        return result;
    }
}
