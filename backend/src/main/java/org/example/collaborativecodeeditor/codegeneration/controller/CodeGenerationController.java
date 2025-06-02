package org.example.collaborativecodeeditor.codegeneration.controller;

import lombok.RequiredArgsConstructor;
import org.example.collaborativecodeeditor.codegeneration.service.CodeGenerationService;
import org.example.collaborativecodeeditor.logger.SimpleLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/codeGenerator")
@RequiredArgsConstructor
public class CodeGenerationController {

    private static final SimpleLogger logger = SimpleLogger.getLogger();
    private final CodeGenerationService generationService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateCode(@RequestBody Map<String, String> request) {
        logger.info("Generating code...");
        try {
            if (!request.containsKey("code") || !request.containsKey("language")) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Missing required parameters",
                                "required", new String[]{"language", "code"}
                        ));
            }

            String language = request.get("language").toLowerCase();
            String code = request.get("code");

            String resolvedCode = generationService.getResolvedCode(language, code);

            return ResponseEntity.ok()
                    .body(Map.of(
                            "code", resolvedCode,
                            "language", language,
                            "status", "success"
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Invalid request",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Code generation failed",
                            "details", e.getMessage()
                    ));
        }
    }
}