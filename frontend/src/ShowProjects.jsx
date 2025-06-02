import React, { useEffect, useState } from "react";
import axios from "axios";
import "./Dashboard.css";

const ShowProjects = () => {
  const [projects, setProjects] = useState([]);
  const [showPopup, setShowPopup] = useState(false);
  const [newProjectName, setNewProjectName] = useState("");

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = () => {
    axios
      .get("http://localhost:8081/project/getProjects", {
        withCredentials: true,
      })
      .then((res) => setProjects(res.data))
      .catch((err) => console.error("Failed to fetch projects:", err));
  };

  const handleRowClick = (project_uuid, project_role) => {
    const roleMap = {
      OWNER: "admin",
      EDITOR: "editor",
      VIEWER: "viewer",
    };

    const role = roleMap[project_role] || "viewer";
    const sessionUrl = `http://localhost:8081/project/${project_uuid}/${role}`;
    window.location.href = sessionUrl;
  };

  const handleCreateProject = () => {
    if (!newProjectName.trim()) return;
    axios
      .post(
        `http://localhost:8081/storage/createProject/${newProjectName}`,
        {},
        { withCredentials: true }
      )
      .then((res) => {
        console.log("Project created:", res.data);
        setShowPopup(false);
        setNewProjectName("");
        fetchProjects();
      })
      .catch((err) => console.error("Failed to create project:", err));
  };

  return (
    <div className="dashboard-container">
      <h1>Welcome to Your Projects</h1>

      <div className="project-table">
        <div className="project-header">
          <span>Project Name</span>
          <span>Owner</span>
          <span>Your Role</span>
        </div>
        {projects.map((proj, index) => (
          <div
            key={index}
            className="project-row"
            onClick={() => handleRowClick(proj.project_uuid, proj.role)}
          >
            <span>{proj.project_name}</span>
            <span>{proj.owner_name}</span>
            <span>{proj.role}</span>
          </div>
        ))}

        <div
          className="create-project-button"
          onClick={() => setShowPopup(true)}
        >
          <span>+ Create New Project</span>
        </div>
      </div>

      {showPopup && (
        <div className="popup-overlay">
          <div className="popup-box">
            <h3
              style={{
                color: "#333",
                fontSize: "1.6rem",
                fontFamily: "'Segoe UI', sans-serif",
                marginBottom: "1rem",
              }}
            >
              Create New Project
            </h3>
            <input
              type="text"
              placeholder="Project Name"
              value={newProjectName}
              onChange={(e) => setNewProjectName(e.target.value)}
            />
            <div className="popup-actions">
              <button onClick={handleCreateProject}>Create</button>
              <button onClick={() => setShowPopup(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ShowProjects;
