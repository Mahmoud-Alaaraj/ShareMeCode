// File: LoginPage.jsx
import React from "react";

export default function LoginPage() {
  const handleLogin = (provider) => {
    window.location.href = `http://localhost:8081/oauth2/authorization/${provider}`;
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1 className="title">Welcome to ShareMeCode</h1>
        <p className="subtitle">Collaborate. Code. Create. Together.</p>

        <div className="buttons">
          <button
            onClick={() => handleLogin("google")}
            className="login-button google"
          >
            <img
              src="https://img.icons8.com/color/16/google-logo.png"
              alt="Google"
            />
            Continue with Google
          </button>
          <button
            onClick={() => handleLogin("github")}
            className="login-button github"
          >
            <img
              src="https://img.icons8.com/ios-glyphs/16/ffffff/github.png"
              alt="GitHub"
            />
            Continue with GitHub
          </button>
        </div>
      </div>
    </div>
  );
}
