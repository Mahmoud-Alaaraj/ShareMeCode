package org.example.collaborativecodeeditor.model.projectversions;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.example.collaborativecodeeditor.model.project.Project;

import java.util.UUID;

@Entity
@Data
public class ProjectVersion {

    @Id
    private UUID versionUUID = UUID.randomUUID();
    private String versionName;
    private String versionTime;
    @ManyToOne
    @JoinColumn(name = "projectuuid")
    private Project project;

}
