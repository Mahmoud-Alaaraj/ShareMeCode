import React from "react";
import LoginPage from "./LoginPage";
import CodeParticlesBackground from "./CodeParticlesBackground";
import "./index.css";

function EntryPage() {
  return (
    <>
      <div className="app">
        <CodeParticlesBackground />
        <LoginPage />
      </div>
    </>
  );
}

export default EntryPage;
