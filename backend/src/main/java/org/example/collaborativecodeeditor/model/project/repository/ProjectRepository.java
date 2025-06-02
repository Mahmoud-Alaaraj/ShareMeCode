package org.example.collaborativecodeeditor.model.project.repository;

import org.example.collaborativecodeeditor.model.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findByPath(String path);
    Optional<Project> findByProjectUUID(UUID projectUUID);
    Optional<Project> findByName(String name);
}
