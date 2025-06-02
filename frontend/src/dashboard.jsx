import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "./Dashboard.css";
import ShowProjects from "./ShowProjects.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <ShowProjects />
  </StrictMode>
);
