package org.example.collaborativecodeeditor.codegeneration;

import lombok.Data;

@Data
public class ModelResponseNode implements Comparable<ModelResponseNode> {
    private Double score;
    private String response;
    private String modelName;

    public ModelResponseNode() {
    }

    public ModelResponseNode(Double score, String response, String modelName) {
        this.score = score;
        this.response = response;
        this.modelName = modelName;
    }

    @Override
    public int compareTo(ModelResponseNode other) {
        if (Double.compare(other.getScore(), this.getScore()) != 0) {
            return Double.compare(other.getScore(), this.getScore());
        }
        return Integer.compare(this.getResponse().length(), other.getResponse().length());
    }
}
