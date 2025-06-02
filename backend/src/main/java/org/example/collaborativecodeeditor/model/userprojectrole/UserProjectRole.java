package org.example.collaborativecodeeditor.model.userprojectrole;

import jakarta.persistence.*;
import lombok.Data;
import org.example.collaborativecodeeditor.model.project.Project;
import org.example.collaborativecodeeditor.model.user.User;

@Entity
@Table(name = "user_project_roles")
@Data
public class UserProjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_path", nullable = false)
    private Project project;

    private Role role;
}
