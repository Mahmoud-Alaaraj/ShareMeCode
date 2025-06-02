package org.example.executionserver.service;

import org.example.executionserver.dto.CodeMessage;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;
@Service
public class CodeExecutionService {

    public String runProjectInDocker(CodeMessage codeMessage)  throws IOException, InterruptedException {

        String image, compileAndRunCmd;
        String fileName = extractFileName(codeMessage.getFilePath());
        String className = extractClassName(fileName);

        switch (codeMessage.getLanguage().toLowerCase()) {
            case "java" -> {
                image = "openjdk:17";
                compileAndRunCmd = String.format("cd /app && javac %s && java %s", codeMessage.getFilePath(), className);
            }
            case "python" -> {
                image = "python:3.9";
                compileAndRunCmd = String.format("python /app/%s", codeMessage.getFilePath());
            }
            case "cpp" -> {
                image = "gcc:latest";
                compileAndRunCmd = String.format("g++ /app/%s -o /app/a.out && /app/a.out", codeMessage.getFilePath());
            }
            default -> throw new IllegalArgumentException("Unsupported language: " + codeMessage.getLanguage());
        }

        String containerName = "code-runner-" + UUID.randomUUID();

        ProcessBuilder runContainer = new ProcessBuilder(
                "docker", "run", "--name", containerName, "-dit", image, "bash"
        );
        runContainer.redirectErrorStream(true);
        runContainer.start().waitFor();

        ProcessBuilder copyProject = new ProcessBuilder(
                "docker", "cp", codeMessage.getProjectPath(), containerName + ":/app"
        );
        copyProject.redirectErrorStream(true);
        copyProject.start().waitFor();

        ProcessBuilder execCode = new ProcessBuilder(
                "docker", "exec", "-i", containerName, "bash", "-c", compileAndRunCmd
        );
        execCode.redirectErrorStream(true);
        Process process = execCode.start();

        try (BufferedWriter inputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            inputWriter.write(codeMessage.getInputData());
            inputWriter.flush();
        }

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        output.append("Exit code: ").append(exitCode).append("\n");

        new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor();

        return output.toString();
    }

    private String extractFileName(String filePath) {
        String fileName;
        if (filePath.contains("/")) {
            int lastSlashIndex = filePath.lastIndexOf("/");
            fileName = filePath.substring(lastSlashIndex + 1);
        } else {
            fileName = filePath;
        }
        return fileName;
    }

    private String extractClassName(String fileName) {
        String className;
        if (fileName.lastIndexOf(".") != -1) {
            className = fileName.substring(0, fileName.lastIndexOf("."));
        }
        else {
            className = fileName;
        }
        return className;
    }
}
