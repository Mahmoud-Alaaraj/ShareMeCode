package org.example.collaborativecodeeditor.versioning.service;

import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.repository.ProjectRepository;
import org.example.collaborativecodeeditor.model.projectversions.ProjectVersion;
import org.example.collaborativecodeeditor.model.projectversions.repository.ProjectVersionRepository;
import org.example.collaborativecodeeditor.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VersionService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ProjectVersionRepository projectVersionRepository;

    public void createNewVersion(String versionName, UUID projectUUID) throws IOException {
        Optional<Project> projectOptional = projectRepository.findByProjectUUID(projectUUID);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        ProjectVersion projectVersion = new ProjectVersion();

        projectVersion.setProject(project);
        if (versionName.equals(":default")) {
            projectVersion.setVersionName(projectVersion.getVersionUUID().toString());
        }
        else {
            projectVersion.setVersionName(versionName);
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
        String formattedDate = now.format(formatter);

        projectVersion.setVersionTime(formattedDate);

        Path projectHistoryPath = Path.of(project.getHistoryPath(), projectVersion.getVersionUUID().toString());
        storageService.copyDirectoryContent(project.getPath(), projectHistoryPath.toString());

        projectVersionRepository.save(projectVersion);
    }

    public void revertCommit(ProjectVersion version, Project project) throws IOException {
        Path projectHistoryPath = Path.of(project.getHistoryPath(), version.getVersionUUID().toString());
        storageService.copyDirectoryContent(projectHistoryPath.toString(), project.getPath());
    }

    public List<HashMap<String, Object>> getVersionsOfProject(Project project) throws IOException {
        List<HashMap<String, Object>> results = new ArrayList<>();

        List<ProjectVersion> projectVersions = projectVersionRepository.findByProject(project);
        for (ProjectVersion projectVersion : projectVersions) {
            HashMap<String, Object> version = new HashMap<>();
            version.put("commitUUID", projectVersion.getVersionUUID());
            version.put("commit_name", projectVersion.getVersionName());
            version.put("commit_time", projectVersion.getVersionTime());
            results.add(version);
        }

        return results;
    }

}
