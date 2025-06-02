package org.example.collaborativecodeeditor.storage.service;

import org.apache.commons.io.FileUtils;
import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.project.repository.ProjectRepository;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.userprojectrole.Role;
import org.example.collaborativecodeeditor.model.userprojectrole.UserProjectRole;
import org.example.collaborativecodeeditor.model.userprojectrole.repository.UserProjectRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${file.storage.root}")
    private String basePath;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserProjectRoleRepository userProjectRoleRepository;

    public Project createProject(User user, String projectName) throws IOException {
        Path userRootPath = Paths.get(user.getRootPath());
        createDirectory(userRootPath, projectName);

        Path userHistoryRootPath = Paths.get(user.getHistoryRootPath());
        createDirectory(userHistoryRootPath, projectName);

        Path projectPath = Paths.get(userRootPath.toString(), projectName);
        Path projectHistoryPath = Paths.get(userHistoryRootPath.toString(), projectName);
        Project project = new Project();
        project.setPath(projectPath.toString());
        project.setName(projectName);
        project.setProjectUUID(UUID.randomUUID());
        project.setOwner(user);
        project.setHistoryPath(projectHistoryPath.toString());
        projectRepository.save(project);

        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setUser(user);
        userProjectRole.setProject(project);
        userProjectRole.setRole(Role.OWNER);

        userProjectRoleRepository.save(userProjectRole);

        return project;
    }

    public void deleteProject(User user, String projectName) throws IOException {
        Path userRootPath = Paths.get(user.getRootPath());

        Path projectPath = Paths.get(userRootPath.toString(), projectName);

        Optional<Project> optionalProject = projectRepository.findByPath(projectPath.toString());
        if (optionalProject.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }

        projectRepository.delete(optionalProject.get());

        List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findByProjectPath(userRootPath.toString());
        userProjectRoleRepository.deleteAll(userProjectRoles);

        deleteStorageComponent(userRootPath, projectName);
    }

    public void createDirectory(Path rootPath, String newDirName) throws IOException {
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Root path is not a directory");
        }
        Path newPath = rootPath.resolve(newDirName);
        Files.createDirectories(newPath);
    }

    public void deleteStorageComponent(Path rootPath, String componentName) throws IOException {
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Root path is not a directory");
        }
        Path newPath = rootPath.resolve(componentName);
        Files.deleteIfExists(newPath);
    }

    public void createFile(Path rootPath, String fileName) throws IOException {
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Root path is not a directory");
        }
        Path newPath = rootPath.resolve(fileName);
        Files.createFile(newPath);
    }

    public String getFileContent(Path rootPath, String fileName) throws IOException {
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Root path is not a directory");
        }
        Path newPath = rootPath.resolve(fileName);
        return Files.readString(newPath);
    }

    public void setFileContent(Path fullFilePath, String content) throws IOException {
        if (!Files.isRegularFile(fullFilePath)) {
            throw new IllegalArgumentException("Root path is not a file");
        }
        Files.writeString(fullFilePath, content, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void rename(String originalPathStr, String newName) throws IOException {
        Path originalPath = Paths.get(originalPathStr);

        if (!Files.exists(originalPath)) {
            throw new NoSuchFileException("Path does not exist: " + originalPath);
        }

        Path parentDir = originalPath.getParent();
        Path newPath = parentDir.resolve(newName);

        if (Files.exists(newPath)) {
            throw new FileAlreadyExistsException("Target already exists: " + newPath);
        }

        Files.move(originalPath, newPath);
    }

    public FileNode getDirStructure(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            FileNode dirNode = new FileNode(path.getFileName().toString(), "directory");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    dirNode.children.add(getDirStructure(entry));
                }
            }
            return dirNode;
        } else {
            return new FileNode(path.getFileName().toString(), "file");
        }
    }

    public void copyDirectoryContent(String sourcePath, String targetPath) throws IOException {
        if (!Files.isDirectory(Path.of(sourcePath))) {
            throw new IllegalArgumentException("Source path is not a directory");
        }

        FileUtils.copyDirectory(Path.of(sourcePath).toFile(), Path.of(targetPath).toFile());
    }

    public String getBasePath() {
        return basePath;
    }
}
