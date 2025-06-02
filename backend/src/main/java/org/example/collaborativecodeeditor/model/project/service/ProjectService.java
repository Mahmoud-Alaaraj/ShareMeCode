package org.example.collaborativecodeeditor.model.project.service;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public Project getProject(UUID projectUUID) {

        Optional<Project> projectOptional = projectRepository.findByProjectUUID(projectUUID);

        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }

        return projectOptional.get();
    }

}
