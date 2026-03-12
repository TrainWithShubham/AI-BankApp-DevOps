# DevSecOps Pipeline Flow

## Visual Pipeline Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                  DEVSECOPS MAIN PIPELINE                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────┐
        │    STAGE 1: CI - Security Scans         │
        │  ┌──────────────────────────────────┐   │
        │  │ 1. Code Quality (Checkstyle)     │   │
        │  │ 2. Secrets Scan (Gitleaks)       │   │
        │  │ 3. Dependency Scan (OWASP)       │   │
        │  │ 4. Docker Lint (Hadolint)        │   │
        │  │ 5. SAST (Semgrep)                │   │
        │  └──────────────────────────────────┘   │
        └─────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────┐
        │    STAGE 2: Build & Container Scan      │
        │  ┌──────────────────────────────────┐   │
        │  │ 6. Build (Maven + Docker)        │   │
        │  │ 7. Container Scan (Trivy)        │   │
        │  └──────────────────────────────────┘   │
        └─────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────┐
        │    STAGE 3: Deploy & DAST               │
        │  ┌──────────────────────────────────┐   │
        │  │ 8. Deploy to EC2                 │   │
        │  │ 9. DAST (OWASP ZAP)              │   │
        │  └──────────────────────────────────┘   │
        └─────────────────────────────────────────┘
```

## Pipeline Files

| File | Purpose | When It Runs |
|------|---------|--------------|
| `devsecops-pipeline.yml` | Main orchestrator | On push to main/devsecops |
| `code-quality.yml` | Java code style checks | Stage 1 |
| `secrets-scan.yml` | Git history secret scan | Stage 1 |
| `dependency-scan.yml` | Maven dependency CVE scan | Stage 1 |
| `docker-lint.yml` | Dockerfile best practices | Stage 1 |
| `sast-scan.yml` ⭐ | Static code security scan | Stage 1 |
| `docker-build-push.yml` | Build & push to Docker Hub | Stage 2 |
| `image-scan.yml` | Container vulnerability scan | Stage 2 |
| `deploy-to-server.yml` | SSH deploy to EC2 | Stage 3 |
| `dast-scan.yml` ⭐ | Live app security scan | Stage 3 |

## Security Gates

### Gate 1-5: CI Security Scans (Parallel)
- ✅ Code Quality
- ✅ Secrets Scan
- ✅ Dependency Scan (OWASP with cache)
- ✅ Docker Lint
- ⭐ **SAST (Semgrep)**

### Gate 6-7: Build & Scan (Sequential)
- ✅ Maven Build
- ✅ Trivy Container Scan

### Gate 8-9: Deploy & Test (Sequential)
- ✅ Deploy to EC2
- ⭐ **DAST (OWASP ZAP)** 

## What You'll See in GitHub Actions

```
devsecops-pipeline
├── code-quality ✓
├── secrets-scan ✓
├── dependency-scan ✓
├── docker-scan ✓
├── sast ⭐
├── build ✓
├── trivy ✓
├── deploy ✓
└── dast ⭐
```

## SAST vs DAST

| Aspect | SAST (Semgrep) | DAST (OWASP ZAP) |
|--------|----------------|------------------|
| **When** | Before build | After deploy |
| **Tests** | Source code | Running application |
| **Finds** | Code vulnerabilities | Runtime vulnerabilities |
| **Speed** | Fast (~15s) | Slower (~2-3 min) |
| **Examples** | SQL injection in code, hardcoded secrets | Missing security headers, XSS in forms |

## Total Pipeline Time

- **Without SAST/DAST**: ~12-15 minutes
- **With SAST/DAST**: ~15-18 minutes
- **SAST adds**: ~15 seconds
- **DAST adds**: ~2-3 minutes

## Artifacts Generated

1. `owasp-dependency-check-report` (HTML)
2. `trivy-scan-report` (SARIF)
3. `zap-dast-report` (HTML) ⭐

---

