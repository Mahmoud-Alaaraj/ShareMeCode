package org.example.executionserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CodeMessage {
    private String submitID;
    private String filePath;
    private String language;
    private String inputData;
    private String projectPath;

}