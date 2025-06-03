# ShareMeCode

A real-time collaborative, AI-integrated code editing and execution platform built with **Spring Boot**, **React**, **RabbitMQ**, **MySQL**, and **Docker**.

## 🚀 Features

- Real-time collaboration with WebSocket
- Secure login using OAuth 2.0 (Google & GitHub)
- Version control: commit, revert, clone, fork
- Code execution in Docker containers (Java, Python, C++)
- Distributed execution using RabbitMQ
- AI-powered code generation with LLM-based evaluation

---

## 🧱 Prerequisites

- Java 17+
- Node.js 18+ and npm
- Maven 3.6+
- Docker
- Git

---

## 🐬 Run MySQL using Docker

```bash
docker run --name mysql-shareme \
  -e MYSQL_DATABASE=collaborative_code_editor_db \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=root \
  -p 3306:3306 \
  -d mysql:8.0
```

> 📌 Make sure your `application.properties` in Spring Boot matches these credentials.

---

## 🕊️ Run RabbitMQ using Docker

```bash
docker run --name rabbitmq-shareme \
  -p 5672:5672 \
  -p 15672:15672 \
  -d rabbitmq:3-management
```

> You can access the RabbitMQ dashboard at [http://localhost:15672](http://localhost:15672)  
> Default credentials: `guest` / `guest`

---

## 🛠️ Backend Setup (Spring Boot)

```bash
cd backend  # or the correct backend folder
./mvnw clean install
java -jar target/*.jar
```

Make sure the `application.properties` includes the correct database and RabbitMQ configurations.

> You can access the website at [http://localhost:8081](http://localhost:8081)  

---

## 💻 Frontend Setup (React)

```bash
cd frontend  # or the correct frontend folder
npm install
npm run dev
```

---

## ⚙️ Code Execution Services

Each supported language (Java, Python, C++) runs in an isolated Docker container.  
The execution is handled through distributed services using RabbitMQ.

---

## 🧠 AI Code Generation

Use prompts in the format:  
```text
$generate a function that reverses a string in Python$
```

The system queries multiple LLMs and selects the best response using "LLM-as-a-Judge" evaluation.

---

## 🔐 Security & Auth

- OAuth 2.0 Login (Google/GitHub)
- RBAC (Admin, Editor, Viewer roles)
- JWT-based session management
- Spring Security filter chain

---

## 🧪 Testing

- Backend: JUnit with Spring Boot Test
- Frontend: React Testing Library (optional setup)

---

## 🧳 Deployment

You can manually build the backend Docker image like this:

```bash
docker build -t shareme-backend .
```

---

## 👨‍💻 Contributors

- Mahmoud Alaaraj  
- Nabeel Ismaeel  
- Wadea Khader  

**Supervisor:** Dr. Suboh Alkhushayni
