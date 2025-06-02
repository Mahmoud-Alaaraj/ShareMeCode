package org.example.collaborativecodeeditor.codegeneration.service;

import com.nimbusds.jose.util.Pair;
import org.example.collaborativecodeeditor.codegeneration.ModelAPI;
import org.example.collaborativecodeeditor.codegeneration.ModelResponseNode;
import org.example.collaborativecodeeditor.logger.SimpleLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.collaborativecodeeditor.codegeneration.ModelAPI.models;

@Service
public class CodeGenerationService {

    @Value("${code.generation.key}")
    private String API_KEY;

    private static final SimpleLogger logger = SimpleLogger.getLogger();

    public String getResolvedCode(String language, String code) throws Exception {
        List<ModelResponseNode> results = new ArrayList<>();
        String formattedPrompt = String.format(
                "You are a code generator.\n" +
                        "\n" +
                        "You will be given a code snippet in a specific programming language (%s). Inside this code, there will be one or more embedded prompts denoted using dollar signs, for example: $generate a function to...$. Your task is as follows:\n" +
                        "\n" +
                        "1. Identify all embedded prompts marked with dollar signs.\n" +
                        "2. For each embedded prompt, generate code that satisfies the intent of the prompt using the same programming language and following best practices.\n" +
                        "3. Replace each prompt in the original code with the generated code, and insert any required imports or includes at the top of the file.\n" +
                        "4. Do not modify any part of the original code other than:\n" +
                        "   - Replacing the embedded prompts with the generated code.\n" +
                        "   - Inserting necessary imports or includes for the generated code to function correctly.\n" +
                        "5. If the prompt is embedded within a class, generate the code inside that class. Only define external classes or helpers if necessary, such as defining a Node class to implement BFS. \n" +
                        "6. The final code must be correctly formatted, with proper indentation, consistent bracket placement, and any structural formatting required by the language.\n" +
                        "7. The output must be:\n" +
                        "   - The complete, final code with all prompts replaced.\n" +
                        "   - No explanations, no extra text, and no comments—not even language comments. Output the code and the code only.\n" +
                        "\n" +
                        "Here is the input code:\n" +
                        "\n" +
                        "```\n" +
                        "%s\n" +
                        "```\n",
                language.toLowerCase(),
                code
        );

        for (ModelAPI model : models) {
            String modelResult = generateCode(formattedPrompt, model.getUrl(), model.getModel());
            double resultScore = getAvgResultScore(modelResult, code);
            results.add(new ModelResponseNode(resultScore, modelResult, model.getModel()));
        }
        Collections.sort(results);
        logger.info("================[Scores]================");
        for (ModelResponseNode node : results) {
            logger.info("Model " + node.getModelName() + " response got score = " + node.getScore());
        }
        logger.info("========================================");
        logger.info("================[Result]================");
        for (ModelResponseNode node : results) {
            logger.info("Model " + node.getModelName() + " returned the following response:");
            logger.info(node.getResponse());
            logger.info("========================================");
        }
        return results.getFirst().getResponse();
    }

    private Double getAvgResultScore(String modelResult, String prompt) {
        Double result = 0.0;
        for (ModelAPI model : models) {
            result += getResultScore(modelResult, prompt, model.getUrl(), model.getModel());
        }
        return result / models.size();
    }

    private Double getResultScore(String modelResult, String prompt, String url, String model) {
        String formattedPrompt = String.format("You are a strict code reviewer.\n" +
                        "\n" +
                        "Your task is to evaluate a piece of code in response to a programming prompt. You must assign a score between 0.0 and 100.0 based on the following criteria:\n" +
                        "\n" +
                        "1. Correctness (40 percent) — Does the code meet the functional requirements of the prompt?\n" +
                        "2. Clarity (15 percent) — Is the code easy to read and understand?\n" +
                        "3. Idiomatic Use (15 percent) — Does the code follow best practices and idiomatic patterns for the language?\n" +
                        "4. Error Handling (10 percent) — Does the code properly anticipate and handle errors?\n" +
                        "5. Performance (10 percent) — Is the code reasonably efficient?\n" +
                        "6. Maintainability (10 percent) — Is the code modular, well-structured, and easy to modify?\n" +
                        "\n" +
                        "Code Only — If the response contains any text not a part of the language and not a comment, then give it a penalty and make its score zero\n" +
                        "\n" +
                        "---\n" +
                        "\n" +
                        "Prompt:\n" +
                        "%s\n" +
                        "\n" +
                        "========== code starts from here ==============\n" +
                        "%s\n" +
                        "========== code ends from here ==============\n" +
                        "\n" +
                        "Score (0.0 to 100.0):\n" +
                        "Return only the final score as a float without any explanation or text.\n",
                prompt,
                modelResult
        );
        logger.info("Evaluating the response using " + model + " model");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", formattedPrompt
        )));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map response = restTemplate.postForObject(url, entity, Map.class);

        Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        Matcher matcher = pattern.matcher(((Map)((Map)((List)response.get("choices")).get(0)).get("message"))
                .get("content").toString());

        Double score = 0.0;
        while (matcher.find()) {
            String match = matcher.group();
            score = Double.parseDouble(match);
        }

        logger.info("Got score = " + score);
        return score;
    }

    public String generateCode(String prompt, String url, String model) throws Exception {
        logger.info("Generating code using " + model + " model");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", prompt
        )));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map response = restTemplate.postForObject(url, entity, Map.class);

        String generatedCode = ((Map)((Map)((List)response.get("choices")).get(0)).get("message"))
                .get("content").toString();

        // Clean up markdown code blocks if present
        return generatedCode.replaceAll("```.*\\n?", "");
    }
}