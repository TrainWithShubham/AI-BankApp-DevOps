# Method 2 Setup Guide - Separate Ollama EC2

Your project is now configured to use **Method 2 by default** (separate Ollama EC2).

## Architecture
```
┌─────────────────────────┐     ┌─────────────────┐
│   App EC2               │     │  Ollama EC2     │
│  - MySQL container      │     │                 │
│  - BankApp container    │────▶│  Native Ollama  │
└─────────────────────────┘     └─────────────────┘
```

## Setup Steps

### Step 1: Launch Ollama EC2

1. **Go to AWS EC2 Console** → Launch Instance

2. **Configure Instance:**
   - Name: `bankapp-ollama`
   - AMI: Ubuntu 22.04 LTS
   - Instance Type: `t3.large` (8GB RAM minimum)
   - Key Pair: Select your existing key pair

3. **Configure Security Group:**
   - Create new security group: `ollama-sg`
   - Add inbound rule:
     - Type: Custom TCP
     - Port: 11434
     - Source: Security group of your App EC2 (or App EC2's private IP)

4. **Add User Data:**
   - Scroll down to "Advanced details"
   - In "User Data" field, paste the entire content from `scripts/ollama-setup.sh`
   - Or paste this:
   ```bash
   #!/bin/bash
   export HOME=/root
   curl -fsSL https://ollama.com/install.sh | sh
   sleep 10
   mkdir -p /etc/systemd/system/ollama.service.d
   cat <<EOF > /etc/systemd/system/ollama.service.d/override.conf
   [Service]
   Environment="OLLAMA_HOST=0.0.0.0"
   EOF
   systemctl daemon-reload
   systemctl restart ollama
   echo "Waiting for Ollama server to start..."
   MAX_RETRIES=30
   RETRY_COUNT=0
   while ! curl -s http://localhost:11434/api/tags > /dev/null; do
       RETRY_COUNT=$((RETRY_COUNT+1))
       if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
           echo "Ollama server failed to start in time."
           exit 1
       fi
       sleep 2
   done
   echo "Pulling tinyllama model..."
   ollama pull tinyllama
   echo "Ollama setup complete and listening on port 11434"
   ```

5. **Launch Instance**

6. **Note the PRIVATE IP:**
   - After instance launches, copy the **Private IPv4 address** (e.g., `172.31.x.x`)
   - You'll need this for the next step

7. **Verify Ollama is working:**
   ```bash
   # SSH into Ollama EC2
   ssh -i your-key.pem ubuntu@<OLLAMA-PUBLIC-IP>
   
   # Check if model is pulled
   ollama list
   # Should show: tinyllama
   ```

---

### Step 2: Launch App EC2

1. **Go to AWS EC2 Console** → Launch Instance

2. **Configure Instance:**
   - Name: `bankapp-server`
   - AMI: Ubuntu 22.04 LTS
   - Instance Type: `t3.medium` (4GB RAM)
   - Key Pair: Select your existing key pair

3. **Configure Security Group:**
   - Add inbound rules:
     - Port 22 (SSH)
     - Port 8080 (HTTP)

4. **Launch Instance**

---

### Step 3: Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

**Add/Update these secrets:**

| Secret Name | Value | Example |
|-------------|-------|---------|
| `EC2_SSH_HOST` | App EC2 Public IP | `54.123.45.67` |
| `EC2_SSH_USER` | SSH username | `ubuntu` |
| `EC2_SSH_PRIVATE_KEY` | Your .pem file content | `-----BEGIN RSA PRIVATE KEY-----...` |
| `OLLAMA_URL` | Ollama EC2 Private IP with port | `http://172.31.x.x:11434` |
| `DB_USERNAME` | Database username | `bankuser` |
| `DB_PASSWORD` | Database password | `Test@123` |
| `DB_ROOT_PASSWORD` | MySQL root password | `Test@123` |
| `DOCKERHUB_TOKEN` | Docker Hub access token | Get from hub.docker.com |

**Add/Update these variables:**

| Variable Name | Value |
|---------------|-------|
| `DOCKERHUB_USER` | Your Docker Hub username |

---

### Step 4: Push to GitHub

```bash
cd AI-BankApp-DevOps
git add .
git commit -m "Configured Method 2 deployment with separate Ollama EC2"
git push origin main
```

The GitHub Actions pipeline will automatically:
1. Build your Docker image
2. Push to Docker Hub
3. Deploy to App EC2 using `app-tier.yml`
4. Connect to your separate Ollama EC2

---

## Verification

After deployment completes:

1. **Check App EC2:**
   ```bash
   ssh -i your-key.pem ubuntu@<APP-EC2-IP>
   cd ~/devops
   sudo docker ps
   # Should show: bankapp-mysql and bankapp containers
   ```

2. **Test Ollama Connection:**
   ```bash
   # From App EC2
   nc -zv 172.31.x.x 11434
   # Should show: Connection succeeded
   ```

3. **Access Application:**
   - Open browser: `http://<APP-EC2-PUBLIC-IP>:8080`
   - Test the AI chatbot feature

---

## Cost Estimate

| Resource | Instance Type | Monthly Cost |
|----------|---------------|--------------|
| App EC2 | t3.medium | ~$30 |
| Ollama EC2 | t3.large | ~$60 |
| **Total** | | **~$90/month** |

---

## Switching Back to Method 1

If you want to go back to all-in-one setup:

1. Edit `.github/workflows/deploy-to-server.yml`:
   - Change `app-tier.yml` back to `docker-compose.yml`
   - Remove `OLLAMA_URL` from .env
   - Remove `-f app-tier.yml` flags

2. Use only 1 EC2 instance (t3.large or m7i.large)

3. Push changes to GitHub
