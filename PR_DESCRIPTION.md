# PR Title
```
feat: Add DevSecOps Pipeline + Automated Ollama Deployment (Method 2)
```

# PR Description

## Overview
This PR adds a complete DevSecOps CI/CD pipeline on top of the existing Docker setup, implementing comprehensive security scanning, automated deployment, enhanced monitoring, and **automated Ollama model pulling with production-grade deployment architecture**.

## Base Branch
Building on top of the `docker` branch which already includes:
- Docker & Docker Compose setup
- AI chatbot with Ollama

## What This PR Adds

### DevSecOps Pipeline
1. **11 Automated Workflows** - Complete CI/CD pipeline
2. **SAST** - Semgrep for static code analysis
3. **DAST** - OWASP ZAP for dynamic security testing
4. **SCA** - OWASP Dependency Check for vulnerabilities
5. **Container Security** - Trivy image scanning
6. **Secret Detection** - Gitleaks for credential leaks
7. **Code Quality** - Hadolint for Dockerfile linting

### Infrastructure & Deployment
1. **Automated AWS EC2 Deployment** - GitHub Actions → EC2
2. **Multi-stage Dockerfile** - Optimized production builds
3. **Environment Management** - `.env.example` template
4. **Monitoring** - Spring Actuator with Prometheus metrics
5. **🆕 Automated Ollama Setup** - Zero-touch AI model deployment
6. **🆕 Method 2 Architecture** - Separate Ollama EC2 for production scalability

### Enhancements
1. **UI Polish** - Stats cards, improved dashboard layout
2. **Documentation** - Comprehensive README with setup guides
3. **🆕 Deployment Flexibility** - Two deployment methods with easy switching

## DevSecOps Pipeline Flow

```text
1️⃣ Code Quality
↓
2️⃣ Secrets Scan
↓
3️⃣ Dependency Scan
↓
4️⃣ Dockerfile Lint
↓
5️⃣ SAST (Semgrep)
↓
6️⃣ Build Docker Image
↓
7️⃣ Image Scan (Trivy)
↓
8️⃣ Push to Docker Registry
↓
9️⃣ Deploy to EC2
↓
🔟 DAST (OWASP ZAP)
```

## 🆕 Ollama Automation - Two Deployment Methods

### Method 1: All-in-One (Local/Development)
**Architecture:**
```
┌─────────────────────────────┐
│      One EC2 Instance       │
│  - MySQL container          │
│  - Ollama container         │
│  - BankApp container        │
└─────────────────────────────┘
```
- Uses `docker-compose.yml`
- `ollama-pull-model` service auto-pulls tinyllama
- Perfect for development and testing

### Method 2: Separate Ollama Tier (Production - DEFAULT)
**Architecture:**
```
┌─────────────────────────┐     ┌─────────────────┐
│   App EC2               │     │  Ollama EC2     │
│  - MySQL container      │     │                 │
│  - BankApp container    │────▶│  Native Ollama  │
│                         │     │  + tinyllama    │
└─────────────────────────┘     └─────────────────┘
```
- Uses `app-tier.yml`
- Dedicated Ollama EC2 with automated setup
- Better resource isolation and scalability
- **Pipeline configured for this by default**

## 🔧 Key Files Added

### DevSecOps Pipeline
1. `.github/workflows/` - 11 CI/CD workflow files
2. `PIPELINE_FLOW.md` - Pipeline documentation
3. `.trivyignore`, `trivy.yaml` - Security scan config
4. `.zap/rules.tsv` - DAST rules
5. `suppression.xml` - Dependency check config
6. `Dockerfile.multistage` - Optimized build

### 🆕 Ollama Automation
7. **`scripts/ollama-setup.sh`** - EC2 User Data script for automated Ollama installation
8. **`app-tier.yml`** - Production deployment file (MySQL + BankApp only)
9. **`METHOD2_SETUP.md`** - Complete setup guide for separate Ollama EC2
10. **`DEPLOYMENT_METHODS.md`** - Quick comparison of both methods
11. **`DEPLOYMENT_GUIDE.md`** - Detailed deployment instructions

## 📸 Screenshots

### **1️⃣ GitHub Actions – All pipelines passing ✅**
<img width="1909" height="748" alt="image" src="https://github.com/user-attachments/assets/cb568f38-7c75-474b-a3b5-9b5302e7d5fb" />

---

### **2️⃣ EC2 Deployment – Live Application Running**
<img width="1916" height="939" alt="6" src="https://github.com/user-attachments/assets/4f856e06-40aa-4bc2-964e-8bf0e02a0daa" />

---

### **3️⃣ Enhanced Dashboard – With stats cards**
<img width="1916" height="939" alt="Screenshot from 2026-03-13 00-37-01" src="https://github.com/user-attachments/assets/138da4ce-415d-47f0-9a9e-0d89961b2198" />

---

### **4️⃣ SAST Report – Semgrep results & DAST Report – OWASP ZAP findings (Artifacts)**
<img width="1419" height="543" alt="image" src="https://github.com/user-attachments/assets/af88f068-e7a7-4207-814c-66c7d708ebe8" />

---

### **5️⃣ NVD API Key Generation for OWASP Dependency Checks**
<img width="1892" height="875" alt="3" src="https://github.com/user-attachments/assets/6580eb7f-041d-4f88-98a3-ac42ac33df0f" />

---

### **6️⃣ No Leaks Detected & Pipeline Summary**
<img width="1897" height="788" alt="image" src="https://github.com/user-attachments/assets/b226e490-c822-40e1-801f-e0eb49e35ac4" />

---

### **7️⃣ EC2 Instances**
<img width="1700" height="291" alt="Screenshot from 2026-03-13 00-37-42" src="https://github.com/user-attachments/assets/7dfc4a91-ef8a-43a1-9a7d-f78dd2f5b251" />

---

### **8️⃣ GITHUB SECRETS**
<img width="1777" height="897" alt="image" src="https://github.com/user-attachments/assets/64e54e01-800b-451a-b8f9-5098ed671a27" />

---

### **9️⃣ GITHUB VARIABLES**
<img width="1769" height="729" alt="image" src="https://github.com/user-attachments/assets/85cc88ab-1e02-4f24-bb4f-aaf53e426cb7" />

---

### **🔟 Docker Image created, Docker Process running**
<img width="1907" height="407" alt="5" src="https://github.com/user-attachments/assets/32fbe629-c8a6-4173-9d1c-1048db950e55" />

---

## Security Improvements
1. 5-layer security scanning (SAST, DAST, SCA, Container, Secrets)
2. Automated vulnerability detection and reporting
3. Container hardening with multi-stage builds
4. Continuous security monitoring

## 🆕 Ollama Automation Benefits
1. **Zero Manual Setup** - Ollama installs and pulls tinyllama model automatically
2. **Resource Isolation** - AI workload separated from application tier
3. **Cost Optimization** - App EC2 can use smaller instance (t3.medium vs t3.large)
4. **Scalability** - Easy to scale AI tier independently
5. **Flexibility** - Both methods available, switch via workflow comments

## Testing
- ✅ All security scans passing
- ✅ Docker builds successfully
- ✅ Deployed and tested on AWS EC2
- ✅ All workflows green
- ✅ Ollama auto-pull verified on both methods
- ✅ Method 2 production deployment tested

## Required Secrets (for deployment)

| Name | Type | Description |
|------|------|-------------|
| `DOCKERHUB_TOKEN` | Secret | DockerHub access token for pushing images |
| `EC2_SSH_HOST` | Secret | EC2 instance public IP or DNS |
| `EC2_SSH_USER` | Secret | SSH username for EC2 (e.g., `ubuntu`) |
| `EC2_SSH_PRIVATE_KEY` | Secret | Private key used to SSH into EC2 |
| `DB_USERNAME` | Secret | Database username |
| `DB_PASSWORD` | Secret | Database user password |
| `DB_ROOT_PASSWORD` | Secret | MySQL/MariaDB root password |
| **🆕 `OLLAMA_URL`** | **Secret** | **Ollama EC2 private IP with port (e.g., `http://172.31.x.x:11434`) - Required for Method 2** |
| `DOCKERHUB_USER` | Variable | DockerHub username |

## 🎯 What Changed in This Update

**Before:** Manual Ollama setup required  
**After:** Fully automated via Docker Compose (Method 1) or EC2 User Data (Method 2)

**Before:** Single deployment method  
**After:** Two methods with production-grade architecture as default

**Before:** No production-grade AI tier separation  
**After:** Dedicated Ollama EC2 for production scalability

## 📚 Documentation
- `README.md` - Updated with Method 2 as default
- `METHOD2_SETUP.md` - Step-by-step production setup guide
- `DEPLOYMENT_METHODS.md` - Quick comparison table
- `DEPLOYMENT_GUIDE.md` - Detailed instructions for both methods

Thank You :)
