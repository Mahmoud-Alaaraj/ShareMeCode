// File: CodeParticlesBackground.jsx
import React, { useEffect, useRef } from "react";

const symbols = [
  "{",
  "}",
  "</>",
  "let",
  "const",
  "=>",
  "()",
  "[ ]",
  "C++",
  "Java",
  "Python",
  "Commit",
  "Fork",
  "Revert",
];

const CodeParticlesBackground = () => {
  const canvasRef = useRef();

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);
    const particles = [];
    const particleCount = 80;

    const mouse = { x: width / 2, y: height / 2 };

    window.addEventListener("resize", () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    });

    window.addEventListener("mousemove", (e) => {
      mouse.x = e.clientX;
      mouse.y = e.clientY;
    });

    class Particle {
      constructor() {
        this.reset();
      }

      reset() {
        this.x = Math.random() * width;
        this.y = Math.random() * height;
        this.vx = (Math.random() - 0.5) * 0.5;
        this.vy = (Math.random() - 0.5) * 0.5;
        this.symbol = symbols[Math.floor(Math.random() * symbols.length)];
        this.fontSize = Math.random() * 12 + 12;
        this.opacity = Math.random() * 0.5 + 0.5;
      }

      draw() {
        ctx.fillStyle = `rgba(0, 255, 153, ${this.opacity})`;
        ctx.font = `${this.fontSize}px 'Courier New', monospace`;
        ctx.fillText(this.symbol, this.x, this.y);
      }

      update() {
        const dx = this.x - mouse.x;
        const dy = this.y - mouse.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < 100) {
          const angle = Math.atan2(dy, dx);
          this.vx += Math.cos(angle) * 0.05;
          this.vy += Math.sin(angle) * 0.05;
        }

        this.x += this.vx;
        this.y += this.vy;

        if (this.x < 0 || this.x > width || this.y < 0 || this.y > height) {
          this.reset();
        }
      }
    }

    for (let i = 0; i < particleCount; i++) {
      particles.push(new Particle());
    }

    const animate = () => {
      ctx.clearRect(0, 0, width, height);
      particles.forEach((p) => {
        p.update();
        p.draw();
      });
      requestAnimationFrame(animate);
    };

    animate();
  }, []);

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        zIndex: -1, // <--- VERY IMPORTANT
        width: "100%",
        height: "100%",
        pointerEvents: "none",
        backgroundColor: "#111",
      }}
    />
  );
};

export default CodeParticlesBackground;
