# Deployment Methods

This project supports two deployment methods. **Method 2 is the default** for production.

## Method 1: All-in-One (Local/Development)

**File:** `docker-compose.yml`

**Architecture:**
```
┌─────────────────────────────┐
│      One EC2 Instance       │
│  - MySQL container          │
│  - Ollama container         │
│  - BankApp container        │
└─────────────────────────────┘
```

**Use for:**
- Local development
- Testing
- Simple deployments

**How to use:**
```bash
docker compose up -d
```

**To switch pipeline to Method 1:**
Edit `.github/workflows/deploy-to-server.yml`:
- Uncomment the Method 1 sections
- Comment out the Method 2 sections

---

## Method 2: Separate Ollama Tier (Production - DEFAULT)

**File:** `app-tier.yml`

**Architecture:**
```
┌─────────────────────────┐     ┌─────────────────┐
│   App EC2               │     │  Ollama EC2     │
│  - MySQL container      │     │                 │
│  - BankApp container    │────▶│  Native Ollama  │
└─────────────────────────┘     └─────────────────┘
```

**Use for:**
- Production deployments
- Better resource isolation
- Scalability

**How to use:**
```bash
docker compose -f app-tier.yml up -d
```

**Setup Guide:** See [METHOD2_SETUP.md](METHOD2_SETUP.md)

**Required:**
- Separate Ollama EC2 with `scripts/ollama-setup.sh` in User Data
- GitHub secret: `OLLAMA_URL` = `http://<OLLAMA-PRIVATE-IP>:11434`

---

## Quick Comparison

| Feature | Method 1 | Method 2 (Default) |
|---------|----------|-------------------|
| **File** | docker-compose.yml | app-tier.yml |
| **EC2 Count** | 1 | 2 |
| **Ollama** | Docker container | Native on separate EC2 |
| **MySQL** | Docker container | Docker container |
| **Cost** | ~$60/month (1x t3.large) | ~$90/month (t3.medium + t3.large) |
| **Best For** | Development/Testing | Production |
| **GitHub Secret** | No OLLAMA_URL needed | Requires OLLAMA_URL |

---

## Files Overview

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Method 1 - All services in Docker |
| `app-tier.yml` | Method 2 - App tier only (MySQL + BankApp) |
| `scripts/ollama-setup.sh` | Automates Ollama installation on EC2 |
| `METHOD2_SETUP.md` | Complete setup guide for Method 2 |
| `DEPLOYMENT_GUIDE.md` | Detailed comparison and instructions |

---

## Current Configuration

✅ **Default:** Method 2 (Separate Ollama EC2)
- Pipeline uses `app-tier.yml`
- Requires `OLLAMA_URL` secret
- Method 1 code is commented in workflow for easy switching
