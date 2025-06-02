import React, { useRef, useState, useEffect } from "react";
import Editor from "@monaco-editor/react";
import Modal from "react-modal";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import dayjs from "dayjs";

Modal.setAppElement("#root");

function CodeEditor() {
  const editorRef = useRef(null);
  const [currentLanguage, setCurrentLanguage] = useState("javascript");
  const [input, setInput] = useState("");
  const [output, setOutput] = useState("");
  const [showSidebar, setShowSidebar] = useState(true);
  const [structure, setStructure] = useState(null);
  const [showEditor, setShowEditor] = useState(false);
  const [currentSelectedPath, setCurrentSelectedPath] = useState("");
  const [showFileInput, setShowFileInput] = useState(false);
  const [showDirectoryInput, setShowDirectoryInput] = useState(false);
  const [newFileName, setNewFileName] = useState("");
  const [newDirectoryName, setNewDirectoryName] = useState("");
  const [showRenameInput, setShowRenameInput] = useState(false);
  const [renameNewName, setRenameNewName] = useState("");
  const [isContributorsModalOpen, setIsContributorsModalOpen] = useState(false);
  const [contributors, setContributors] = useState([]);
  const [commits, setCommits] = useState([]);
  const [newContributorEmail, setNewContributorEmail] = useState("");
  const [newContributorRole, setNewContributorRole] = useState("Editor");
  const [isAddContributorModalOpen, setAddContributorModalOpen] =
    useState(false);
  const [
    isVersionControlManagementModalOpen,
    setVersionControlManagementModalOpen,
  ] = useState(false);
  const [isCommitModalOpen, setCommitModalOpen] = useState(false);
  const [isAddCommitModalOpen, setAddCommitModalOpen] = useState(false);
  const [newCommitName, setNewCommitName] = useState("");
  const [newContributor, setNewContributor] = useState({
    user_name: "",
    user_email: "",
    role: "",
  });

  const [code, setCode] = useState("");
  const stompClientRef = useRef(null);
  const subscriptionRef = useRef(null);

  const urlParams = new URLSearchParams(window.location.search);
  const projectUUID = urlParams.get("projectUUID");
  const role = urlParams.get("role");

  const adminPage = role === "admin";
  const editorPage = role === "editor";
  const viewerPage = role === "viewer";

  // Establish client once
  useEffect(() => {
    const socket = new SockJS("http://localhost:8081/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => console.log("Connected to WebSocket"),
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const subscribeToFile = (filePath) => {
    if (!stompClientRef.current || !stompClientRef.current.connected) return;

    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
    }

    const topic = `/topic/${projectUUID}/${filePath}`;
    subscriptionRef.current = stompClientRef.current.subscribe(
      topic,
      (message) => {
        const payload = JSON.parse(message.body);
        if (payload && payload.content) {
          setCode(payload.content);
          console.log(payload.content);
        }
      }
    );
  };

  const revertCommit = async (commitUUID) => {
    try {
      const response = await fetch(
        `http://localhost:8081/versionControl/revert?projectUUID=${projectUUID}&commitUUID=${commitUUID}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to revert");
      }

      closeCommitModal();
    } catch (error) {
      console.error("Error reverting:", error);
      alert("Error reverting");
    }
  };

  const addContributor = async () => {
    try {
      const response = await fetch(
        `http://localhost:8081/roles/give?projectUUID=${projectUUID}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: newContributorEmail,
            role: newContributorRole,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to add contributor");
      }

      fetch(`http://localhost:8081/project/getUsers?projectUUID=${projectUUID}`)
        .then((res) => res.json())
        .then((data) => {
          setContributors(data);
          setIsContributorsModalOpen(true);
        })
        .catch((err) => {
          console.error("Error fetching contributors:", err);
          alert("Failed to load contributors.");
        });

      closeAddContributorModal();
    } catch (error) {
      console.error("Error adding contributor:", error);
      alert("Error adding contributor");
    }
  };

  const handleCommit = async () => {
    try {
      const response = await fetch(
        `http://localhost:8081/versionControl/commit?versionName=${newCommitName}&projectUUID=${projectUUID}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to commit");
      }

      fetch(
        `http://localhost:8081/versionControl/getCommits?projectUUID=${projectUUID}`
      )
        .then((res) => res.json())
        .then((data) => {
          setCommits(data);
          setCommitModalOpen(true);
        })
        .catch((err) => {
          console.error("Error fetching commits:", err);
          alert("Failed to load commits.");
        });

      closeAddCommitModal();
    } catch (error) {
      console.error("Error making the commit:", error);
      alert("Error making the commit");
    }
  };

  const openVersionControlManagementModal = () =>
    setVersionControlManagementModalOpen(true);
  const closeVersionControlManagementModal = () =>
    setVersionControlManagementModalOpen(false);

  const openAddContributorModal = () => setAddContributorModalOpen(true);
  const closeAddContributorModal = () => setAddContributorModalOpen(false);

  const openAddCommitModal = () => setAddCommitModalOpen(true);
  const closeAddCommitModal = () => setAddCommitModalOpen(false);

  const openCommitModal = () => {
    fetch(
      `http://localhost:8081/versionControl/getCommits?projectUUID=${projectUUID}`
    )
      .then((res) => res.json())
      .then((data) => {
        setCommits(data);
        setCommitModalOpen(true);
      })
      .catch((err) => {
        console.error("Error fetching commits:", err);
        alert("Failed to load commits.");
      });
  };

  const closeCommitModal = () => setCommitModalOpen(false);

  const openContributorsModal = () => {
    fetch(`http://localhost:8081/project/getUsers?projectUUID=${projectUUID}`)
      .then((res) => res.json())
      .then((data) => {
        setContributors(data); // assuming the backend returns an array of usernames/emails/names
        setIsContributorsModalOpen(true);
      })
      .catch((err) => {
        console.error("Error fetching contributors:", err);
        alert("Failed to load contributors.");
      });
  };

  const closeContributorsModal = () => setIsContributorsModalOpen(false);

  const fetchStructure = () => {
    fetch(`http://localhost:8081/storage/projectStructure/${projectUUID}`)
      .then((response) => response.json())
      .then((data) => {
        setStructure(data);
      })
      .catch((error) =>
        console.error("Error fetching project structure:", error)
      );
  };

  useEffect(() => {
    fetchStructure();
  }, [projectUUID]);

  function handleEditorDidMount(editor, monaco) {
    editorRef.current = editor;
  }

  const executeFile = async () => {
    setOutput("Running...");
    try {
      const response = await fetch(
        "http://localhost:8081/executor/executeFile",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            projectUUID,
            filePath: currentSelectedPath,
            language: currentLanguage,
            inputData: input,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.text();
      setOutput(result);
    } catch (error) {
      console.error("Error executing file:", error);
      setOutput(`Error: ${error.message}`);
    }
  };

  const handleSelectPath = (fullPath, isFile) => {
    setCurrentSelectedPath(fullPath);

    fetch(`http://localhost:8081/log?value=${encodeURIComponent(fullPath)}`)
      .then((response) => response.json())
      .then((data) => console.log("Path logged successfully:", data))
      .catch((error) => console.error("Error logging path:", error));

    if (isFile && editorRef.current) {
      fetch(
        `http://localhost:8081/storage/getFile?projectUUID=${projectUUID}&filePath=${encodeURIComponent(
          fullPath
        )}`
      )
        .then((res) => res.text())
        .then((text) => {
          editorRef.current.setValue(text);
          console.log("will connect");
          subscribeToFile(fullPath);
        })
        .catch((err) => console.error("Error fetching file content:", err));
    }

    setShowEditor(true);
  };

  const sendMessage = (content) => {
    console.log(content);
    const destination = `/app/communication`;
    stompClientRef.current?.publish({
      destination,
      body: JSON.stringify({
        content,
        projectUUID,
        filePath: currentSelectedPath,
      }),
    });
  };

  const renderStructure = (nodes, parentPath = "") => {
    return nodes.map((node) => {
      const fullPath = parentPath ? `${parentPath}/${node.name}` : node.name;

      return (
        <li key={fullPath}>
          <span
            onClick={() => handleSelectPath(fullPath, node.type === "file")}
            style={{
              cursor: "pointer",
              color: currentSelectedPath === fullPath ? "#ffcc00" : "#d4d4d4",
            }}
          >
            {node.type === "directory" ? "📁" : "📄"} {node.name}
          </span>

          {node.type === "directory" && node.children && (
            <ul style={{ listStyle: "none", paddingLeft: "20px" }}>
              {renderStructure(node.children, fullPath)}
            </ul>
          )}
        </li>
      );
    });
  };

  const handleCreateFile = async () => {
    if (!newFileName) {
      alert("Please enter a file name.");
      return;
    }

    let relativePath = "";

    if (!currentSelectedPath) {
      relativePath = "";
    } else {
      const selectedNodeParts = currentSelectedPath.split("/");
      const selectedNodeName = selectedNodeParts[selectedNodeParts.length - 1];

      const isDirectory = (path, nodes) => {
        for (const node of nodes) {
          if (node.name === path && node.type === "directory") return true;
          if (node.type === "directory" && node.children) {
            const found = isDirectory(path, node.children);
            if (found) return true;
          }
        }
        return false;
      };

      const selectedIsDir = isDirectory(
        selectedNodeName,
        structure?.children || []
      );

      if (selectedIsDir || currentSelectedPath.endsWith("/")) {
        relativePath = currentSelectedPath.replace(/\/$/, "");
      } else {
        const lastSlashIndex = currentSelectedPath.lastIndexOf("/");
        relativePath =
          lastSlashIndex !== -1
            ? currentSelectedPath.substring(0, lastSlashIndex)
            : "";
      }
    }

    await fetch("http://localhost:8081/storage/createFile", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        projectUUID,
        relativePath,
        fileName: newFileName,
      }),
    })
      .then((res) => {
        if (res.status === 201) {
          alert("File created successfully!");
          fetchStructure();
          setNewFileName("");
          setShowFileInput(false);
        } else {
          alert("Failed to create file");
        }
      })
      .catch((err) => {
        console.error("Create file error:", err);
        alert("Error creating file");
      });
  };

  const handleCreateDirectory = async () => {
    if (!newDirectoryName) {
      alert("Please enter a directory name.");
      return;
    }

    let relativePath = "";

    if (!currentSelectedPath) {
      relativePath = "";
    } else {
      const selectedNodeParts = currentSelectedPath.split("/");
      const selectedNodeName = selectedNodeParts[selectedNodeParts.length - 1];

      const isDirectory = (path, nodes) => {
        for (const node of nodes) {
          if (node.name === path && node.type === "directory") return true;
          if (node.type === "directory" && node.children) {
            const found = isDirectory(path, node.children);
            if (found) return true;
          }
        }
        return false;
      };

      const selectedIsDir = isDirectory(
        selectedNodeName,
        structure?.children || []
      );

      if (selectedIsDir || currentSelectedPath.endsWith("/")) {
        relativePath = currentSelectedPath.replace(/\/$/, "");
      } else {
        const lastSlashIndex = currentSelectedPath.lastIndexOf("/");
        relativePath =
          lastSlashIndex !== -1
            ? currentSelectedPath.substring(0, lastSlashIndex)
            : "";
      }
    }

    await fetch("http://localhost:8081/storage/createDirectory", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        projectUUID,
        relativePath,
        directoryName: newDirectoryName,
      }),
    })
      .then((res) => {
        if (res.status === 201) {
          alert("Directory created successfully!");
          fetchStructure();
          setNewDirectoryName("");
          setShowDirectoryInput(false);
        } else {
          alert("Failed to create directory");
        }
      })
      .catch((err) => {
        console.error("Create directory error:", err);
        alert("Error creating directory");
      });
  };

  const handleDeleteComponent = async () => {
    if (!currentSelectedPath) {
      alert("Please select a file or directory to delete.");
      return;
    }

    const componentName = currentSelectedPath.split("/").pop();
    const relativePath = currentSelectedPath.substring(
      0,
      currentSelectedPath.lastIndexOf("/")
    );

    await fetch("http://localhost:8081/storage/deleteComponent", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        projectUUID,
        relativePath,
        componentName,
      }),
    })
      .then((res) => {
        if (res.status === 201) {
          alert("Component deleted successfully!");
          fetchStructure();
          setCurrentSelectedPath("");
          setShowEditor(false);
        } else {
          alert("Failed to delete component");
        }
      })
      .catch((err) => {
        console.error("Delete component error:", err);
        alert("Error deleting component");
      });
  };

  const handleRenameComponent = async () => {
    if (!currentSelectedPath || !renameNewName) {
      alert("Please select a file/directory and enter the new name.");
      return;
    }

    const componentName = currentSelectedPath.split("/").pop();
    const relativePath = currentSelectedPath.substring(
      0,
      currentSelectedPath.lastIndexOf("/")
    );

    await fetch("http://localhost:8081/storage/renameComponent", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        projectUUID,
        relativePath,
        componentName,
        newName: renameNewName,
      }),
    })
      .then((res) => {
        if (res.status === 201) {
          alert("Component renamed successfully!");
          fetchStructure();
          setRenameNewName("");
          setShowRenameInput(false);
          setCurrentSelectedPath("");
          setShowEditor(false);
        } else {
          alert("Failed to rename component");
        }
      })
      .catch((err) => {
        console.error("Rename component error:", err);
        alert("Error renaming component");
      });
  };

  const handleCloneProject = async () => {
    window.open(
      `http://localhost:8081/storage/download-zip?projectUUID=${projectUUID}`,
      "_blank"
    );
  };

  const handleForkProject = async () => {
    try {
      const response = await fetch(
        `http://localhost:8081/versionControl/forkProject?projectUUID=${projectUUID}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fork");
      }
    } catch (error) {
      console.error("Error making the fork:", error);
      alert("Error making the fork");
    }
  };

  const handleGenerate = async () => {
    try {
      const response = await fetch(
        "http://localhost:8081/codeGenerator/generate",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            language: currentLanguage,
            code,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }

      const result = await response.json(); // parse JSON body
      if (result.status === "success" && result.code) {
        setCode(result.code);
      } else {
        console.error("Unexpected response format:", result);
      }
    } catch (error) {
      console.error("Error generating code:", error);
    }
  };

  return (
    <div
      className="d-flex flex-column"
      style={{ height: "100vh", backgroundColor: "#1e1e1e" }}
    >
      <div className="d-flex p-2">
        <button
          className="btn btn-outline-light me-2"
          onClick={() => setShowSidebar(!showSidebar)}
        >
          {showSidebar ? "Hide" : "Show"} Project
        </button>

        {(adminPage || editorPage) && (
          <button
            className="btn btn-success me-2"
            onClick={() => setShowFileInput(!showFileInput)}
          >
            + File
          </button>
        )}

        {(adminPage || editorPage) && (
          <button
            className="btn btn-info me-2"
            onClick={() => setShowDirectoryInput(!showDirectoryInput)}
          >
            + Directory
          </button>
        )}

        {(adminPage || editorPage) && (
          <button
            className="btn btn-danger me-2"
            onClick={handleDeleteComponent}
          >
            Delete
          </button>
        )}

        {(adminPage || editorPage) && (
          <button
            className="btn btn-warning me-auto"
            onClick={() => setShowRenameInput(!showRenameInput)}
          >
            Rename
          </button>
        )}

        {showRenameInput && (
          <div className="d-flex align-items-center me-2">
            <input
              type="text"
              className="form-control"
              placeholder="Enter new name..."
              value={renameNewName}
              onChange={(e) => setRenameNewName(e.target.value)}
              style={{ width: "200px", marginRight: "5px" }}
            />
            <button
              className="btn btn-outline-light"
              onClick={handleRenameComponent}
            >
              Rename
            </button>
          </div>
        )}

        {showFileInput && (
          <div className="d-flex align-items-center me-2">
            <input
              type="text"
              className="form-control"
              placeholder="Enter file name..."
              value={newFileName}
              onChange={(e) => setNewFileName(e.target.value)}
              style={{ width: "200px", marginRight: "5px" }}
            />
            <button
              className="btn btn-outline-light"
              onClick={handleCreateFile}
            >
              Create File
            </button>
          </div>
        )}

        {showDirectoryInput && (
          <div className="d-flex align-items-center me-2">
            <input
              type="text"
              className="form-control"
              placeholder="Enter directory name..."
              value={newDirectoryName}
              onChange={(e) => setNewDirectoryName(e.target.value)}
              style={{ width: "200px", marginRight: "5px" }}
            />
            <button
              className="btn btn-outline-light"
              onClick={handleCreateDirectory}
            >
              Create Directory
            </button>
          </div>
        )}

        <button className="btn btn-outline-light me-2" onClick={handleGenerate}>
          Generate Using AI
        </button>

        {(adminPage || editorPage) && (
          <button className="btn btn-primary me-2" onClick={executeFile}>
            Run
          </button>
        )}

        {(adminPage || editorPage) && (
          <div className="dropdown me-2">
            <button
              className="btn btn-secondary dropdown-toggle"
              type="button"
              data-bs-toggle="dropdown"
            >
              Language
            </button>
            <ul className="dropdown-menu">
              <li>
                <a
                  className="dropdown-item"
                  href="#"
                  onClick={() => setCurrentLanguage("java")}
                >
                  Java
                </a>
              </li>
              <li>
                <a
                  className="dropdown-item"
                  href="#"
                  onClick={() => setCurrentLanguage("cpp")}
                >
                  C++
                </a>
              </li>
              <li>
                <a
                  className="dropdown-item"
                  href="#"
                  onClick={() => setCurrentLanguage("python")}
                >
                  Python
                </a>
              </li>
            </ul>
          </div>
        )}

        <button
          className="btn btn-outline-light me-2"
          onClick={openVersionControlManagementModal}
        >
          Version Control Management
        </button>

        {adminPage && (
          <button
            className="btn btn-outline-light me-2"
            onClick={openContributorsModal}
          >
            Contributors
          </button>
        )}

        <Modal
          isOpen={isVersionControlManagementModalOpen}
          onRequestClose={closeVersionControlManagementModal}
          contentLabel="VersionControlManagement"
          style={{
            overlay: {
              backgroundColor: "rgba(0, 0, 0, 0.6)",
              zIndex: 9996,
            },
            content: {
              backgroundColor: "#2e2e2e",
              color: "#ffffff",
              width: "500px",
              margin: "auto",
              padding: "20px",
              borderRadius: "10px",
              zIndex: 9999,
              inset: "50% auto auto 50%",
              transform: "translate(-50%, -50%)",
            },
          }}
        >
          <h2 style={{ marginBottom: "20px" }}>Version Control Management</h2>

          <ul
            style={{
              paddingLeft: "0",
              listStyle: "none",
              maxHeight: "300px",
              overflowY: "auto",
            }}
          >
            <li>
              <button
                className="btn btn-outline-light mt-3"
                onClick={handleCloneProject}
              >
                Clone Project
              </button>
            </li>
            <li>
              <button
                className="btn btn-outline-light mt-3"
                onClick={handleForkProject}
              >
                Fork Project
              </button>
            </li>
            <li>
              {(adminPage || editorPage) && (
                <button
                  className="btn btn-outline-light mt-3"
                  onClick={openCommitModal}
                >
                  Commits
                </button>
              )}
            </li>
            <li>
              <button
                className="btn btn-outline-light mt-3"
                onClick={closeVersionControlManagementModal}
              >
                Close
              </button>
            </li>
          </ul>
        </Modal>

        <Modal
          isOpen={isCommitModalOpen}
          onRequestClose={closeCommitModal}
          contentLabel="Commits"
          style={{
            overlay: {
              backgroundColor: "rgba(0, 0, 0, 0.6)",
              zIndex: 9997,
            },
            content: {
              backgroundColor: "#2e2e2e",
              color: "#ffffff",
              width: "500px",
              margin: "auto",
              padding: "20px",
              borderRadius: "10px",
              zIndex: 9999,
              inset: "50% auto auto 50%",
              transform: "translate(-50%, -50%)",
            },
          }}
        >
          <h2 style={{ marginBottom: "20px" }}>Commits</h2>

          {commits.length === 0 ? (
            <p>No commits found.</p>
          ) : (
            <ul
              style={{
                paddingLeft: "0",
                listStyle: "none",
                maxHeight: "300px",
                overflowY: "auto",
              }}
            >
              {[...commits]
                .sort((a, b) => {
                  const dateA = dayjs(a.commit_time, "D MMM YYYY, HH:mm");
                  const dateB = dayjs(b.commit_time, "D MMM YYYY, HH:mm");
                  return dateB.valueOf() - dateA.valueOf();
                })
                .map((commit, idx) => (
                  <li
                    key={idx}
                    style={{
                      marginBottom: "10px",
                      padding: "10px 14px",
                      background: "#3a3a3a",
                      borderRadius: "6px",
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      fontSize: "14px",
                    }}
                  >
                    <span style={{ flex: 1 }}>{commit.commit_name}</span>
                    <span style={{ flex: 1 }}>{commit.commit_time}</span>
                    <button
                      style={{
                        marginLeft: "10px",
                        background: "#ff4d4f",
                        color: "white",
                        border: "none",
                        padding: "6px 10px",
                        borderRadius: "4px",
                        cursor: "pointer",
                      }}
                      onClick={() => revertCommit(commit.commitUUID)}
                    >
                      Revert
                    </button>
                  </li>
                ))}
            </ul>
          )}

          <button
            className="btn btn-outline-light mt-3"
            onClick={openAddCommitModal}
          >
            Make New Commit
          </button>

          <button
            className="btn btn-outline-light mt-3"
            onClick={closeCommitModal}
          >
            Close
          </button>
        </Modal>

        <Modal
          isOpen={isAddCommitModalOpen}
          onRequestClose={closeAddCommitModal}
          contentLabel="MakeCommit"
          style={{
            overlay: {
              backgroundColor: "rgba(0, 0, 0, 0.6)",
              zIndex: 9998,
            },
            content: {
              backgroundColor: "#2e2e2e",
              color: "#ffffff",
              width: "400px",
              margin: "auto",
              padding: "20px",
              borderRadius: "10px",
              zIndex: 9999,
              inset: "50% auto auto 50%",
              transform: "translate(-50%, -50%)",
            },
          }}
        >
          <h2>Commit</h2>

          <div>
            <label>Commit Name: </label>
            <input
              type="commitName"
              value={newCommitName}
              onChange={(e) => setNewCommitName(e.target.value)}
              placeholder="Enter Commit Name"
              style={{ width: "100%", marginBottom: "10px", padding: "8px" }}
            />

            <button
              className="btn btn-outline-light mt-3"
              onClick={handleCommit}
            >
              Commit
            </button>
            <button
              className="btn btn-outline-light mt-3"
              onClick={closeAddCommitModal}
            >
              Close
            </button>
          </div>
        </Modal>

        <Modal
          isOpen={isContributorsModalOpen}
          onRequestClose={closeContributorsModal}
          contentLabel="Contributors"
          style={{
            overlay: {
              backgroundColor: "rgba(0, 0, 0, 0.6)",
              zIndex: 9998,
            },
            content: {
              backgroundColor: "#2e2e2e",
              color: "#ffffff",
              width: "500px",
              margin: "auto",
              padding: "20px",
              borderRadius: "10px",
              zIndex: 9999,
              inset: "50% auto auto 50%",
              transform: "translate(-50%, -50%)",
            },
          }}
        >
          <h2 style={{ marginBottom: "20px" }}>Contributors</h2>

          {contributors.length === 0 ? (
            <p>No contributors found.</p>
          ) : (
            <ul
              style={{
                paddingLeft: "0",
                listStyle: "none",
                maxHeight: "300px",
                overflowY: "auto",
              }}
            >
              {contributors.map((user, idx) => (
                <li
                  key={idx}
                  style={{
                    marginBottom: "10px",
                    padding: "10px 14px",
                    background: "#3a3a3a",
                    borderRadius: "6px",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    fontSize: "14px",
                  }}
                >
                  <span style={{ flex: 1 }}>{user.user_name}</span>
                  <span style={{ flex: 1 }}>{user.user_email}</span>
                  <span style={{ flex: 1, textAlign: "right" }}>
                    {user.role}
                  </span>
                </li>
              ))}
            </ul>
          )}

          <button
            className="btn btn-outline-light mt-3"
            onClick={openAddContributorModal}
          >
            Add Contributor
          </button>

          <button
            className="btn btn-outline-light mt-3"
            onClick={closeContributorsModal}
          >
            Close
          </button>
        </Modal>

        <Modal
          isOpen={isAddContributorModalOpen}
          onRequestClose={closeAddContributorModal}
          contentLabel="Add Contributor"
          style={{
            overlay: {
              backgroundColor: "rgba(0, 0, 0, 0.6)",
              zIndex: 9998,
            },
            content: {
              backgroundColor: "#2e2e2e",
              color: "#ffffff",
              width: "400px",
              margin: "auto",
              padding: "20px",
              borderRadius: "10px",
              zIndex: 9999,
              inset: "50% auto auto 50%",
              transform: "translate(-50%, -50%)",
            },
          }}
        >
          <h2>Add Contributor</h2>

          <div>
            <label>Email: </label>
            <input
              type="email"
              value={newContributorEmail}
              onChange={(e) => setNewContributorEmail(e.target.value)}
              placeholder="Enter email"
              style={{ width: "100%", marginBottom: "10px", padding: "8px" }}
            />

            <label>Role: </label>
            <select
              value={newContributorRole}
              onChange={(e) => setNewContributorRole(e.target.value)}
              style={{ width: "100%", marginBottom: "10px", padding: "8px" }}
            >
              <option value="Editor">Editor</option>
              <option value="Viewer">Viewer</option>
            </select>

            <button
              className="btn btn-outline-light mt-3"
              onClick={addContributor}
            >
              Add Contributor
            </button>
            <button
              className="btn btn-outline-light mt-3"
              onClick={closeAddContributorModal}
            >
              Close
            </button>
          </div>
        </Modal>
      </div>

      <div className="d-flex flex-grow-1" style={{ height: "100%" }}>
        {showSidebar && structure && (
          <div
            style={{
              width: "250px",
              backgroundColor: "#252526",
              color: "#d4d4d4",
              borderRight: "1px solid #3c3c3c",
              overflowY: "auto",
              padding: "10px",
              boxSizing: "border-box",
            }}
          >
            <h6 class="text-light mb-3 me-2">Project Files</h6>
            <ul style={{ listStyle: "none", paddingLeft: "10px" }}>
              {renderStructure(structure.children)}
            </ul>
          </div>
        )}

        <div style={{ flexGrow: 1 }}>
          {!showEditor ? (
            <div
              style={{
                height: "100%",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                flexDirection: "column",
                color: "#666",
                fontSize: "18px",
              }}
            >
              <p>No file selected</p>
              <p>Please select a file from the sidebar to start editing.</p>
            </div>
          ) : (
            <Editor
              height="100%"
              theme="vs-dark"
              language={currentLanguage}
              value={code}
              onMount={handleEditorDidMount}
              onChange={(value) => sendMessage(value)}
              options={{ readOnly: viewerPage }}
            />
          )}
        </div>

        <div
          style={{
            width: "500px",
            backgroundColor: "#1e1e1e",
            color: "#d4d4d4",
            display: "flex",
            flexDirection: "column",
            borderLeft: "1px solid #3c3c3c",
          }}
        >
          <div
            style={{
              flex: 1,
              display: "flex",
              flexDirection: "column",
              padding: "10px",
              boxSizing: "border-box",
            }}
          >
            <label htmlFor="inputBox" className="form-label text-light mb-1">
              Input:
            </label>
            <textarea
              id="inputBox"
              className="form-control"
              style={{
                flex: 1,
                resize: "none",
                backgroundColor: "#252526",
                color: "#d4d4d4",
                border: "1px solid #3c3c3c",
              }}
              value={input}
              onChange={(e) => setInput(e.target.value)}
            />
          </div>

          <div
            style={{
              flex: 1,
              display: "flex",
              flexDirection: "column",
              padding: "10px",
              boxSizing: "border-box",
              borderTop: "1px solid #3c3c3c",
            }}
          >
            <label htmlFor="outputBox" className="form-label text-light mb-1">
              Output:
            </label>
            <textarea
              id="outputBox"
              className="form-control"
              style={{
                flex: 1,
                resize: "none",
                backgroundColor: "#252526",
                color: "#d4d4d4",
                border: "1px solid #3c3c3c",
              }}
              value={output}
              readOnly
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export default CodeEditor;
