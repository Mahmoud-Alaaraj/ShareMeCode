package org.example.collaborativecodeeditor.model.project;

import jakarta.persistence.*;
import lombok.Data;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.userprojectrole.UserProjectRole;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    private String path;

    private String name;

    private UUID projectUUID;

    private String historyPath;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "project")
    private Set<UserProjectRole> userProjectRoles = new HashSet<>();

}