package org.example.collaborativecodeeditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;

import static org.example.collaborativecodeeditor.codegeneration.ModelAPI.prepareAPIs;

@SpringBootApplication
@RestController
@EnableJpaRepositories
public class CollaborativeCodeEditorApplication {

    public static void main(String[] args) {
        prepareAPIs();
        SpringApplication.run(CollaborativeCodeEditorApplication.class, args);
    }

}
