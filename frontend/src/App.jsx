import React from "react";
import EntryPage from "./EntryPage.jsx";
import CodeEditor from "./CodeEditor.jsx";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import "./index.css";
import ShowProjects from "./ShowProjects.jsx";

function App() {
  return (
    <>
      <Router>
        <Routes>
          <Route path="/" element={<EntryPage />} />
          <Route path="/dashboard" element={<ShowProjects />} />
          <Route path="/editor" element={<CodeEditor />} />
        </Routes>
      </Router>
    </>
  );
}

export default App;
