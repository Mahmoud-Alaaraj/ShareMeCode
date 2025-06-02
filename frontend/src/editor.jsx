import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
// import "./editor.css";
import CodeEditor from "./CodeEditor.jsx";
// import CodeEditor from "./CodeEditoro.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <CodeEditor />
  </StrictMode>
);
