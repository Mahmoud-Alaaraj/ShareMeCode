package org.example.collaborativecodeeditor.model.userprojectrole.repository;

import org.example.collaborativecodeeditor.model.userprojectrole.UserProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRoleRepository extends JpaRepository<UserProjectRole, Long> {
    List<UserProjectRole> findByUserEmail(String userEmail);
    List<UserProjectRole> findByProjectPath(String projectPath);
    List<UserProjectRole> findByUserEmailAndProjectPath(String userEmail, String projectPath);

    List<UserProjectRole> findByUserId(Long id);
}