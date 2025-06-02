package org.example.collaborativecodeeditor.model.user;

import jakarta.persistence.*;
import lombok.Data;
import org.example.collaborativecodeeditor.model.userprojectrole.UserProjectRole;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String email;
    private String name;
    private String rootPath;
    private String historyRootPath;

    @OneToMany(mappedBy = "user")
    private Set<UserProjectRole> userProjectRoles = new HashSet<>();
}
