package org.example.collaborativecodeeditor.model.projectversions.repository;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.projectversions.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, String> {
    List<ProjectVersion> findByProject(Project project);

    Optional<ProjectVersion> findByVersionUUID(UUID versionUUID);
}
