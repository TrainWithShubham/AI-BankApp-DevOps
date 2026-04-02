# AI-BankApp-DevOps

![Docker Build & Push](https://github.com/Sana-2026/AI-BankApp-DevOps/actions/workflows/docker-publish.yml/badge.svg)

A Spring Boot banking application used as a base for learning end-to-end DevOps — from Docker to Kubernetes to GitOps.

## Tech Stack
- **Backend:** Spring Boot 3.4.1, Java 21, Spring Security, JPA/Hibernate
- **Frontend:** Thymeleaf, Bootstrap 5, Glassmorphism UI with dark/light theme
- **Database:** MySQL 8.0
- **AI:** Ollama (self-hosted LLM chatbot, zero cost)
- **DevOps:** Docker, GitHub Actions, Kubernetes, Helm, Terraform, Prometheus, Grafana, ArgoCD

## Branches
| Branch | Description |
|--------|-------------|
| `start` | Modernized app — full backend + frontend (developer handoff) |
| `docker` | Adds Dockerfile, multi-stage build, docker-compose |
| `ai` | Adds AI chatbot powered by Ollama |
| `main` | End-to-end DevOps (WIP) |

See [ROADMAP.md](ROADMAP.md) for the full progression.

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
