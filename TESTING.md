# Testing Guide for BankApp DevSecOps

## Prerequisites
- Docker and Docker Compose installed
- No need to install MySQL separately (Docker handles it)

## Quick Start - Run the Application

### 1. Start all services (MySQL + Ollama + BankApp)
```bash
docker compose up -d
```

This will:
- Pull MySQL 8.0 image
- Pull Ollama image for AI chatbot
- Build the BankApp image
- Start all services with proper networking

### 2. Check if services are running
```bash
docker compose ps
```

You should see 3 services running:
- `bankapp-mysql` on port 3306
- `bankapp-ollama` on port 11434
- `bankapp` on port 8080

### 3. View logs (if something goes wrong)
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f bankapp
docker compose logs -f mysql
```

### 4. Access the application
Open your browser and go to: **http://localhost:8080**

## Testing the Application

### Manual Testing Steps

1. **Register a new user**
   - Go to http://localhost:8080/register
   - Create an account with username and password

2. **Login**
   - Use your credentials to login
   - You should see the dashboard

3. **Test banking features**
   - Check initial balance
   - Try deposit operation
   - Try withdrawal operation
   - Try transfer to another account
   - View transaction history

4. **Test AI Chatbot** (if implemented)
   - Look for chat interface
   - Ask banking-related questions

5. **Test theme toggle**
   - Switch between dark/light mode
   - Refresh page to verify persistence

### Health Check Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/metrics

# All actuator endpoints
curl http://localhost:8080/actuator
```

## Verify DevSecOps Components

### 1. Check Docker image was built securely
```bash
# View the built image
docker images | grep bankapp

# Inspect the image
docker inspect bankapp
```

### 2. Test security scanning locally (optional)
```bash
# Scan with Trivy (if installed)
trivy image bankapp

# Or use Docker Scout
docker scout cves bankapp
```

### 3. Verify no secrets in code
```bash
# Check for hardcoded secrets
grep -r "password" src/ --exclude-dir=target
grep -r "secret" src/ --exclude-dir=target
```

## Stopping the Application

```bash
# Stop services (keeps data)
docker compose down

# Stop and remove volumes (fresh start next time)
docker compose down -v
```

## Troubleshooting

### MySQL connection issues
```bash
# Check MySQL is healthy
docker compose ps mysql

# Connect to MySQL directly
docker exec -it bankapp-mysql mysql -u bankuser -pTest@123 bankappdb

# View tables
SHOW TABLES;
```

### Application won't start
```bash
# Check logs
docker compose logs bankapp

# Common issues:
# - MySQL not ready: Wait 30s for health check
# - Port 8080 in use: Stop other services on 8080
# - Build failed: Check Java/Maven errors in logs
```

### Rebuild after code changes
```bash
# Rebuild and restart
docker compose up -d --build
```

## Before Creating PR

### Checklist
- [ ] Application starts successfully with `docker compose up -d`
- [ ] Can register and login
- [ ] Banking operations work (deposit, withdraw, transfer)
- [ ] Transaction history displays correctly
- [ ] No hardcoded credentials in code
- [ ] `.env` file is in `.gitignore`
- [ ] All DevSecOps workflows are present in `.github/workflows/`
- [ ] Docker image builds successfully
- [ ] Health check endpoints respond
- [ ] No sensitive data in commit history

### Test the CI/CD workflows locally (optional)
```bash
# Install act (GitHub Actions local runner)
# Then test workflows:
act -l  # List workflows
act pull_request  # Test PR workflows
```

## Database Credentials (for reference)

These are defined in `.env` file (not committed to git):
- Root Password: `Test@123`
- Database: `bankappdb`
- Username: `bankuser`
- Password: `Test@123`

## Next Steps After Testing

1. Commit your DevSecOps changes
2. Push to your branch
3. Create Pull Request
4. CI/CD pipelines will run automatically
5. Review security scan results in PR

