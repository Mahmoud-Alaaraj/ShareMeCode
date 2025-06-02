package org.example.collaborativecodeeditor.model.projectversions.service;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.repository.ProjectRepository;
import org.example.collaborativecodeeditor.model.projectversions.ProjectVersion;
import org.example.collaborativecodeeditor.model.projectversions.repository.ProjectVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectVersionService {

    @Autowired
    private ProjectVersionRepository projectVersionRepository;

    public ProjectVersion getProjectVersion(UUID projectUUID) {

        Optional<ProjectVersion> projectVersionOptional = projectVersionRepository.findByVersionUUID(projectUUID);

        if (projectVersionOptional.isEmpty()) {
            throw new IllegalArgumentException("Project version not found");
        }

        return projectVersionOptional.get();
    }

}
