package org.example.collaborativecodeeditor.storage.service;

import java.util.ArrayList;
import java.util.List;

public class FileNode {
    public String name;
    public String type;
    public List<FileNode> children;

    public FileNode(String name, String type) {
        this.name = name;
        this.type = type;
        if (type.equals("directory")) {
            this.children = new ArrayList<>();
        }
    }
}
