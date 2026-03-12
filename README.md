# AI BankApp — DevSecOps Banking Application

A modern, secure banking application built with Spring Boot 3.4, featuring a complete DevSecOps CI/CD pipeline with automated security scanning, Docker containerization, and cloud deployment.

![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![DevSecOps](https://img.shields.io/badge/DevSecOps-Pipeline-purple)

## Features

### Banking Features
- **User Authentication** — Spring Security with BCrypt password hashing
- **Account Management** — View balance, deposit, withdraw, transfer funds
- **Transaction History** — Complete audit trail with timestamps
- **AI Assistant** — Integrated chatbot powered by Ollama (optional)

### UI/UX
- **Modern Design** — Glassmorphism UI with gradient accents
- **Dark/Light Theme** — Persistent theme toggle
- **Responsive** — Mobile-friendly Bootstrap 5 interface
- **Accessibility** — WCAG-compliant design patterns

### DevSecOps Features
- **Automated CI/CD** — GitHub Actions pipeline
- **Security Scanning** — Trivy, OWASP Dependency Check, Secret scanning
- **Code Quality** — Hadolint (Dockerfile linting)
- **Container Security** — Multi-stage Docker builds
- **Cloud Deployment** — Automated EC2 deployment
- **Monitoring Ready** — Prometheus metrics via Spring Actuator

## Tech Stack

| Layer          | Technology                                    |
|----------------|-----------------------------------------------|
| Backend        | Spring Boot 3.4.5, Java 21 (Virtual Threads) |
| Database       | MySQL 8.0                                     |
| Security       | Spring Security, BCrypt                       |
| Frontend       | Thymeleaf, Bootstrap 5, Custom CSS            |
| AI (Optional)  | Ollama (TinyLlama)                            |
| Containerization | Docker, Docker Compose                      |
| CI/CD          | GitHub Actions                                |
| Security Scans | SAST (Semgrep), DAST (OWASP ZAP), Trivy, OWASP Dependency Check, Gitleaks |
| Deployment     | AWS EC2                                       |
| Monitoring     | Spring Actuator, Prometheus                   |

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)

### Run with Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/AI-BankApp-DevOps.git
cd AI-BankApp-DevOps

# Create .env file from example
cp .env.example .env

# Edit .env with your credentials
nano .env

# Start all services (MySQL + BankApp)
docker compose up -d

# View logs
docker compose logs -f bankapp

# Access the application
open http://localhost:8080
```

### Run Locally (Development)

```bash
# Start MySQL (or use Docker)
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=Testing@123 \
  -e MYSQL_DATABASE=aibankappdb \
  -p 3306:3306 mysql:8.0

# Build and run the application
./mvnw clean package -DskipTests
java -jar target/bankapp-*.jar

# Or run with Maven
./mvnw spring-boot:run
```

Access at **http://localhost:8080**

## Docker

### Build Docker Image

```bash
# Standard build
docker build -t bankapp .

# Multi-stage build (smaller image)
docker build -f Dockerfile.multistage -t bankapp:latest .
```

### Docker Compose Services

| Service  | Port  | Description                    |
|----------|-------|--------------------------------|
| bankapp  | 8080  | Spring Boot application        |
| mysql    | 3306  | MySQL 8.0 database             |
| ollama   | 11434 | AI model server (optional)     |

**💡 Cost Optimization Tip:**  
For production deployment without AI features, comment out the `ollama` service in `docker-compose.yml`. This allows you to run on smaller instances like `t2.micro` (Free tier eligible) instead of requiring `c7i.large` or larger instances.

```bash
# Start all services (including Ollama)
docker compose up -d

# Start without Ollama (cost-effective)
docker compose up -d bankapp mysql

# Stop services
docker compose down

# Stop and remove volumes
docker compose down -v

# View logs
docker compose logs -f [service-name]
```

## DevSecOps Pipeline

The project includes a complete CI/CD pipeline with security scanning:

### Pipeline Stages

1. **Code Quality**
   - Dockerfile linting (Hadolint)
   - Code formatting checks

2. **Security Scanning**
   - Secret detection (Gitleaks)
   - Dependency vulnerability scan (OWASP Dependency Check)

3. **SAST - Static Application Security Testing**
   - Semgrep scanner with Java, OWASP Top 10, and secrets rules
   - Scans source code for security vulnerabilities before build

4. **Build & Push**
   - Multi-stage Docker build
   - Push to Docker Hub with tags: `latest`, `branch-name`, `commit-sha`

5. **Container Scanning**
   - Trivy image scan for CVEs
   - Fail on CRITICAL/HIGH vulnerabilities

6. **Deployment**
   - Automated deployment to AWS EC2
   - Docker Compose orchestration
   - Health checks

7. **DAST - Dynamic Application Security Testing**
   - OWASP ZAP baseline scan on live application
   - Scans running app for runtime vulnerabilities
   - Report uploaded as artifact

### GitHub Actions Workflow

```yaml
# Triggered on push to main/devsecops branch or manual dispatch
Code Quality → Security Scans → SAST → Build → Image Scan → Deploy → DAST
```

**Complete Pipeline Flow:**
```
1. Code Quality (Hadolint)
2. Secrets Scan (Gitleaks)
3. Dependency Scan (OWASP)
4. Docker Lint
   ↓
5. SAST (Semgrep) ← Runs only after all above pass
   ↓
6. Build & Push (Docker)
   ↓
7. Image Scan (Trivy)
   ↓
8. Deploy (EC2)
   ↓
9. DAST (OWASP ZAP) ← Scans live application
```

### Required GitHub Secrets

Configure these in your repository settings (Settings → Secrets and variables → Actions):

**Docker Hub:**
- `DOCKERHUB_TOKEN` — Docker Hub access token

**AWS EC2:**
- `EC2_SSH_HOST` — EC2 public IP or DNS
- `EC2_SSH_USER` — SSH username (usually `ubuntu`)
- `EC2_SSH_PRIVATE_KEY` — Private key (.pem file content)

**Database:**
- `DB_USERNAME` — Database username
- `DB_PASSWORD` — Database password
- `DB_ROOT_PASSWORD` — MySQL root password

**GitHub Variables:**
- `DOCKERHUB_USER` — Your Docker Hub username

## Environment Variables

| Variable           | Default      | Description                    |
|--------------------|--------------|--------------------------------|
| `MYSQL_HOST`       | localhost    | Database host                  |
| `MYSQL_PORT`       | 3306         | Database port                  |
| `MYSQL_DATABASE`   | aibankappdb  | Database name                  |
| `MYSQL_USER`       | root         | Database username              |
| `MYSQL_PASSWORD`   | Testing@123  | Database password              |
| `OLLAMA_URL`       | http://localhost:11434 | AI model server URL |
| `DOCKERHUB_USER`   | -            | Docker Hub username            |
| `DOCKER_TAG`       | latest       | Docker image tag               |

## Project Structure

```
.
├── .github/workflows/       # CI/CD pipeline definitions
│   ├── devsecops-pipeline.yml
│   ├── docker-build-push.yml
│   ├── image-scan.yml
│   └── deploy-to-server.yml
├── src/
│   ├── main/
│   │   ├── java/com/example/bankapp/
│   │   │   ├── config/          # Security configuration
│   │   │   ├── controller/      # REST & Web controllers
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data access layer
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── templates/       # Thymeleaf views
│   │       ├── static/          # CSS, JS assets
│   │       └── application.properties
│   └── test/                    # Unit tests
├── Dockerfile                   # Production Docker image
├── Dockerfile.multistage        # Optimized multi-stage build
├── docker-compose.yml           # Local development setup
├── pom.xml                      # Maven dependencies
└── README.md
```

## Security Features

### Application Security
- **Authentication** — Form-based login with Spring Security
- **Password Hashing** — BCrypt with configurable strength
- **CSRF Protection** — Enabled for all state-changing operations
- **SQL Injection Prevention** — JPA/Hibernate parameterized queries
- **XSS Protection** — Thymeleaf auto-escaping

### DevSecOps Security Scanning
- **SAST (Static Analysis)** — Semgrep scans source code for vulnerabilities (Java, OWASP Top 10, secrets)
- **DAST (Dynamic Analysis)** — OWASP ZAP scans live application for runtime vulnerabilities
- **SCA (Software Composition Analysis)** — OWASP Dependency Check scans Maven dependencies for known CVEs
- **Container Scanning** — Trivy scans Docker images for OS and library vulnerabilities
- **Secret Detection** — Gitleaks scans Git history to prevent credential leaks
- **Infrastructure as Code** — Hadolint validates Dockerfile best practices

## Monitoring & Observability

Spring Boot Actuator endpoints are exposed for monitoring:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Application info
curl http://localhost:8080/actuator/info
```

## Deployment

### Resource Requirements

**Based on Real-World Testing:**

| Configuration | Instance Type | RAM | vCPU | Monthly Cost | Use Case |
|--------------|---------------|-----|------|--------------|----------|
| **Production (No AI)** | t2.micro / t3.micro | 1GB | 1-2 | ~$8-10 | Banking app only (MySQL + BankApp) |
| **With AI Chatbot** | c7i.large / t3.large | 8GB | 2 | ~$60-80 | Full features with Ollama |

**Why the difference?**
- **Without Ollama:** Only 2 lightweight containers (MySQL + Spring Boot)
- **With Ollama:** AI model requires 4-6GB RAM + significant CPU for inference
- **Tested:** t2.micro works perfectly for core banking features
- **Warning:** Running Ollama on instances smaller than 8GB RAM causes OOM (Out of Memory) crashes

**Recommendation:** Start with t2.micro (free tier), add Ollama later if needed.

### AWS EC2 Deployment

1. **Launch EC2 Instance**

   **Without Ollama (Recommended for cost optimization):**
   - Instance Type: `t2.micro` or `t3.micro` (1GB RAM)
   - Only 2 containers: MySQL + BankApp
   - Cost: ~$8-10/month (Free tier eligible)
   - Perfect for production banking app without AI features
   
   **With Ollama AI Chatbot:**
   - Instance Type: `c7i.large` or `t3.large` minimum (2 vCPU, 8GB RAM)
   - 3 containers: MySQL + BankApp + Ollama
   - Cost: ~$60-80/month
   - Ollama requires significant CPU/RAM for model inference
   - Note: Smaller instances will cause OOM (Out of Memory) errors
   
   **Common Configuration:**
   - OS: Ubuntu 22.04 or later
   - Storage: 20GB minimum (30GB recommended with Ollama)
   - Open ports: 22 (SSH), 8080 (HTTP)
   - Security Group: Allow inbound on ports 22, 8080

2. **Configure GitHub Secrets**
   - Add all required secrets (see above)

3. **Push to Main Branch**
   - Pipeline automatically deploys to EC2
   - Access at `http://YOUR-EC2-IP:8080`

### Manual Deployment

```bash
# SSH to EC2
ssh -i your-key.pem ubuntu@your-ec2-ip

# Install Docker and Docker Compose (if not already installed)
sudo apt update
sudo apt install -y docker.io docker-compose
sudo usermod -aG docker $USER
# Log out and back in for group changes to take effect

# Create deployment directory
mkdir -p ~/devops
cd ~/devops

# Create .env file
cat > .env << EOF
DOCKERHUB_USER=your-username
DOCKER_TAG=latest
DB_USERNAME=bankuser
DB_PASSWORD=Test@123
DB_ROOT_PASSWORD=Test@123
EOF

# Copy docker-compose.yml to server (or download from repo)
wget https://raw.githubusercontent.com/Nandan29300/AI-BankApp-DevOps/devsecops/docker-compose.yml

# Start services (MySQL will automatically create 'bankappdb' database)
docker compose up -d

# Check logs
docker compose logs -f

# Verify database was created
docker exec -it devops-mysql-1 mysql -uroot -pTest@123 -e "SHOW DATABASES;"
```

**Note:** The database `bankappdb` is automatically created by Docker Compose using the `MYSQL_DATABASE` environment variable. No manual database creation needed!

## Development

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

### Local Development with Hot Reload

```bash
# Run with Spring Boot DevTools
./mvnw spring-boot:run

# Application auto-restarts on code changes
```

### Database Migrations

The application uses Hibernate's `ddl-auto=update` for automatic schema management. For production, consider using Flyway or Liquibase.

## Troubleshooting

### Application won't start
- Check MySQL is running: `docker ps`
- Verify database credentials in `.env`
- Check logs: `docker compose logs bankapp`

### Connection refused on port 8080
- Ensure security group allows inbound traffic on port 8080
- Check if application started: `docker logs bankapp`
- Verify port binding: `sudo netstat -tlnp | grep 8080`

### Pipeline fails on security scan
- Review Trivy/OWASP reports in GitHub Actions
- Update vulnerable dependencies in `pom.xml`
- Add exceptions to `.trivyignore` if needed (with justification)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Acknowledgments

- Spring Boot team for the excellent framework
- OWASP for security tools and best practices
- Aqua Security for Trivy scanner
- Bootstrap team for the UI framework

---

**Built with ❤️ for learning DevSecOps practices**
