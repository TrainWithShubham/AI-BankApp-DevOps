# AI-BankApp-DevOps

![Docker Build & Push](https://github.com/Sana-2026/AI-BankApp-DevOps/actions/workflows/docker-publish.yml/badge.svg)

A production-ready full-stack banking application built with Spring Boot and MySQL, featuring secure authentication, account management, and transaction processing. Integrated with a complete DevOps pipeline using Docker, GitHub Actions CI/CD, Kubernetes, Helm, Terraform, Prometheus, and ArgoCD for scalable, automated, and observable deployments.

<img width="1355" height="635" alt="dashboard-app" src="https://github.com/user-attachments/assets/78a0e023-bfab-4b2e-907d-d1f746a989ac" />

## 🏗️ Architecture 

A cloud-native AI banking application built with Spring Boot, containerized using Docker, and deployed via GitHub Actions CI/CD. It leverages Kubernetes for scalability, ArgoCD for GitOps, and Prometheus & Grafana for monitoring.

![architecture](https://github.com/user-attachments/assets/3daa07a3-51b6-4918-b87f-63cb48d7a278)

---
## 🧰 Tech Stack

| Category        | Technologies |
|----------------|-------------|
| **Backend**     | Spring Boot 3.4.1, Java 21, Spring Security, JPA/Hibernate |
| **Frontend**    | Thymeleaf, Bootstrap 5, Glassmorphism UI (Dark/Light Theme) |
| **Database**    | MySQL 8.0 |
| **AI**          | Ollama (Self-hosted LLM chatbot) |
| **Containerization** | Docker |
| **CI/CD**       | GitHub Actions |
| **Orchestration** | Kubernetes |
| **Package Management** | Helm |
| **Infrastructure as Code** | Terraform |
| **Monitoring**  | Prometheus, Grafana |
| **GitOps**      | ArgoCD |

---

## Branches
| Branch | Description |
|--------|-------------|
| `start` | Modernized app — full backend + frontend (developer handoff) |
| `docker` | Adds Dockerfile, multi-stage build, docker-compose |
| `ai` | Adds AI chatbot powered by Ollama |
| `main` | End-to-end DevOps (WIP) |


## Quick Start

### Run locally (needs Java 21 + MySQL)
```bash
# Create database
mysql -u root -p -e "CREATE DATABASE bankappdb;"
# Run the app
./mvnw spring-boot:run
```

### Run with Docker (recommended)
```bash
# Switch to docker branch
git checkout docker
# Start everything
docker compose up -d --build
# Visit http://localhost:8080
```

### Run with AI Chatbot
```bash
# Switch to ai branch
git checkout ai
# Start everything (includes Ollama)
docker compose up -d --build
# Pull the AI model (one-time)
docker exec bankapp-ollama ollama pull tinyllama
# Visit http://localhost:8080
```

## Features
- User registration & login with BCrypt passwords
- Deposit, withdraw, transfer between accounts
- Transaction history with color-coded entries
- Dark/light theme toggle (persists across sessions)
- AI chatbot that knows your balance and recent transactions
- Prometheus metrics at `/actuator/prometheus`
- Health check at `/actuator/health`

- ## 🚀 Future Enhancements

* Kubernetes auto-deployment with Helm
* GitOps using ArgoCD
* Terraform-based infrastructure provisioning
* Advanced AI banking features

---

## 👨‍💻 Author

Sana Shaik

---

🔥 Built as part of DevOps Capstone Project

