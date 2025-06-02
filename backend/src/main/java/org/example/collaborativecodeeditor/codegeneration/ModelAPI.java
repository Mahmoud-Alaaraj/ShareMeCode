package org.example.collaborativecodeeditor.codegeneration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelAPI {

    private String url;
    private String model;

    public ModelAPI(String url, String model) {
        this.url = url;
        this.model = model;
    }

    private static List<List<String>> modelsInfo = List.of(
            List.of(
                    "https://router.huggingface.co/novita/v3/openai/chat/completions",
                    "mistralai/mistral-7b-instruct"
            ),
            List.of(
                    "https://router.huggingface.co/nebius/v1/chat/completions",
                    "Qwen/Qwen2.5-Coder-32B-Instruct-fast"
            ),
            List.of(
                    "https://router.huggingface.co/nebius/v1/chat/completions",
                    "microsoft/phi-4"
            )
    );

    public static List<ModelAPI> models = new ArrayList<>();

    public static void prepareAPIs() {
        for (List<String> modelInfo : modelsInfo) {
            ModelAPI model = new ModelAPI(modelInfo.get(0), modelInfo.get(1));
            models.add(model);
        }
    }
}
