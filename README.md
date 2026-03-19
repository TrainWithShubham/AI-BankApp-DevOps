<div align="center">

# DevSecOps Banking Application

A high-performance, cloud-native financial platform built with Spring Boot 3 and Java 21. Deployed on **Kind (Kubernetes in Docker)** with a fully automated **GitOps pipeline** using GitHub Actions, ArgoCD, and Helm - enforcing **8 sequential security gates** before any code reaches production.

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-orange.svg)](.github/workflows/devsecops-main.yml)
[![Kind](https://img.shields.io/badge/Deploy-Kind-yellow.svg)](#phase-3-cloud-native-deployment-gitops--gateway-api)
[![ArgoCD](https://img.shields.io/badge/GitOps-ArgoCD-red.svg)](#phase-3-cloud-native-deployment-gitops--gateway-api)

</div>

![dashboard](screenshots/1.png)

---

## Technical Architecture

The application is deployed on a modern, cloud-native **Kind** cluster. GitHub Actions handles all CI/CD security gates, then updates Helm manifests in the repo, which **ArgoCD** automatically synchronizes to the cluster.

![architecture](screenshots/architecture.png)

---

## Security Pipeline — 8 Gates

The pipeline enforces **8 sequential security gates** across three modular workflows: [`ci.yml`](.github/workflows/ci.yml), [`build.yml`](.github/workflows/build.yml), and [`cd.yml`](.github/workflows/cd.yml), all orchestrated by [`devsecops-main.yml`](.github/workflows/devsecops-main.yml).

| Gate | Job | Workflow | Tool | Behavior |
| :---: | :--- | :--- | :--- | :--- |
| 1 | `gitleaks` | `ci.yml` | Gitleaks | **Strict** — Fails if any secrets found in full Git history |
| 2 | `lint` | `ci.yml` | Checkstyle | **Audit** — Reports Java style violations (Google Style), does not block |
| 3 | `sast` | `ci.yml` | Semgrep | **Strict** — SAST on Java code (OWASP Top 10 + secrets rules) |
| 4 | `sca` | `ci.yml` | OWASP Dependency Check | **Strict** — Fails if any CVE with CVSS ≥ 7.0 found in Maven deps |
| 5 | `build` | `build.yml` | Maven | Compiles, packages JAR, uploads as build artifact |
| 6 | `image_scan` | `build.yml` | Trivy | **Strict** — Fails on CRITICAL or HIGH CVE in the Docker image |
| 7 | `push_to_dockerhub` | `build.yml` | Docker Hub (Secrets) | Pushes verified image to Docker Hub using secure secrets |
| 8 | `gitops-update` | `cd.yml` | Helm / ArgoCD | Updates `charts/bankapp/values.yaml` with new image tag → triggers ArgoCD auto-sync |

> **ArgoCD** is configured with `automated.selfHeal: true` — once `values.yaml` is updated, ArgoCD automatically pulls and deploys the new image to the Kind cluster without any manual intervention.

All scan reports (OWASP, Trivy) are uploaded as downloadable **Artifacts** in each GitHub Actions run.

---

## Technology Stack

| Category | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.4.1, Spring Security, Spring Data JPA |
| **Frontend** | Thymeleaf, Bootstrap |
| **AI Integration** | Google Gemini API (`gemini-3-flash-preview`) |
| **Database** | MySQL 8.0 (Kubernetes Pod) |
| **Container** | Docker (eclipse-temurin:21-jre-alpine, non-root user) |
| **Kubernetes** | Kind (Kubernetes in Docker) |
| **GitOps** | ArgoCD, Helm 3 |
| **Networking** | Kubernetes Gateway API (GatewayClass: `envoy`) |
| **CI/CD** | GitHub Actions (Standard Workflow) |
| **Security Tools** | Gitleaks, Checkstyle, Semgrep, OWASP Dependency Check, Trivy |
| **Registry** | Docker Hub |
| **Secrets** | Kubernetes Secrets, GitHub Actions Secrets |

---

## Implementation Phases

### Phase 1: Local Infrastructure Initialization (Kind)

> ### Environment Requirements
> | # | Component | Purpose | How to create it |
> | :---: | :--- | :--- | :--- |
> | 1 | **EC2 (Ubuntu)**| Host Instance | Launch `t3.medium` (Ubuntu 24.04) |
> | 2 | **Docker** | Container Runtime | Install via `apt` |
> | 3 | **Kind** | Local Kubernetes Cluster | Install via binary |
> | 4 | **Gemini API Key** | Enables AI assistant responses | Store in Kubernetes Secret |
> | 5 | **kubectl/Helm** | Cluster management | Install binaries |

#### Step 0 — EC2 Launch & Docker Setup

1. **Launch Instance**: 
   - AMI: **Ubuntu Server 24.04 LTS**.
   - Type: **t3.medium** (Min 2 vCPU, 4GB RAM).
   - Security Group: Allow **22 (SSH)**, **80 (HTTP)**, **443 (HTTPS)**, and **8081 (ArgoCD)**.

2. **Update & Install Docker & Helm**:
   ```bash
   sudo apt update && sudo apt upgrade -y
   
   # Install Docker
   sudo apt install docker.io -y
   sudo usermod -aG docker $USER && newgrp docker

   # Install Helm
   curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
   ```

#### Step 1 — Infrastructure Initialization (Kind)
   - Install Kind:
   
      ```bash
      curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.27.0/kind-linux-amd64 

      chmod +x ./kind

      sudo mv ./kind /usr/local/bin/kind
      ```

   - Install Kubectl

      ```bash
      curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

      sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
      
      kubectl version --client
      ```

   - Clone this repository and navigate to the project directory

   - Create Kind Cluster:
  
      ```bash
      chmod +x scripts/kind-setup.sh
      
      ./scripts/kind-setup.sh
      ```
  
   - **Install Gateway API & Envoy Gateway**: (Crucial for Networking)
  
      ```bash
      # 1. Install Gateway API CRDs
      kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml
  
      # 2. Install Envoy Gateway via Helm
      helm install eg oci://docker.io/envoyproxy/gateway-helm \
         --version v1.7.1 \
         -n envoy-gateway-system \
         --create-namespace \
         --set service.type=NodePort
      ```
  
      - Wait for Envoy Gateway to be ready:
   
         ```bash
         kubectl wait -n envoy-gateway-system deployment/envoy-gateway --for=condition=Available --timeout=5m
         ```

   - No local AI runtime is required. This project uses Gemini over HTTPS.
    
   - **Verification**: Confirm Gateway API CRDs and Envoy Gateway are installed and healthy.

      ```bash
      kubectl get crd gateways.gateway.networking.k8s.io
      kubectl get pods -n envoy-gateway-system
      ```

---

### Phase 2: Security and Pipeline Configuration

#### 1. Docker Hub Repository
- Create a repository named `devsecops-bankapp` on [hub.docker.com](https://hub.docker.com/).

#### 2. GitHub Repository Secrets
Configure the following Action Secrets in **Settings → Secrets and variables → Actions**:

| Secret Name | Description |
| :--- | :--- |
| `DOCKERHUB_USERNAME` | Your Docker Hub username |
| `DOCKERHUB_TOKEN` | Your Docker Hub Personal Access Token (PAT) |
| `DOCKERHUB_REPO` | Your full repo name (e.g., `username/devsecops-bankapp`) |
| `NVD_API_KEY` | Free API key from [nvd.nist.gov](https://nvd.nist.gov/developers/request-an-api-key) |

> **Note**: `GITHUB_TOKEN` is used automatically by `cd.yml` to commit the updated `values.yaml` — ensure **Settings → Actions → General → Workflow permissions** is set to **"Read and write permissions"**.

#### Obtaining the NVD API Key (Optional but Recommended)
The `NVD_API_KEY` raises the NVD API rate limit from ~5 requests/30s to 50 requests/30s, reducing the OWASP Dependency Check scan time from 30+ minutes to under 8 minutes. Without it the SCA job will time out.

**Step 1: Request the API Key**
- Go to [https://nvd.nist.gov/developers/request-an-api-key](https://nvd.nist.gov/developers/request-an-api-key).
- Enter your `Organization name`, `email address`, and select `organization type`.
- Accept **Terms of Use** and Click **Submit**.

   ![request](screenshots/22.png)

**Step 2: Activate the API Key**
- Check your email inbox for a message from `nvd-noreply@nist.gov`.

   ![email](screenshots/25.png)

- Click the **activation link** in the email.
- Enter `UUID` provided in email and Enter `Email` to activate
- The link confirms your key and marks it as active.  

   ![api-activate](screenshots/23.png)

**Step 3: Get the API Key**
- After clicking the activation link, the page will generate your API key.
- Copy and save it securely.

   ![api-key](screenshots/24.png)

**Step 4**: Add as GitHub Secret named `NVD_API_KEY`.
   
   ![github-secret](screenshots/15.png)

---

### Phase 3: Cloud-Native Deployment (GitOps + Gateway API)

#### Step 1 — Create Namespace, DB Secret, and Gemini Secret

```bash
kubectl create namespace bankapp-prod

kubectl create secret generic bankapp-db-secrets \
  --from-literal=password=<YOUR_DB_PASSWORD> \
  -n bankapp-prod

kubectl create secret generic bankapp-ai-secrets \
   --from-literal=gemini-api-key=<YOUR_GEMINI_API_KEY> \
   -n bankapp-prod
```

> Get Gemini API key from [Google AI Studio](https://aistudio.google.com/api-keys). This key is required for the AI assistant functionality in the BankApp backend. If you do not have a Gemini API key, you can still deploy and run the application, but AI-powered features will not work.

#### Step 2 — Verify Kind Networking

Ensure your EC2 Security Group allows traffic on ports **80** and **443** (host ports mapped by Kind).

> **Note**: In a Kind cluster, Envoy is exposed through NodePorts **30080/30443**, and `kind-setup.sh` maps host ports **80/443** to those NodePorts.

> **Networking Update**: This project uses **Gateway API + Envoy Gateway** (not Kubernetes Ingress). Ingress is deprecated, and all traffic exposure is handled declaratively via `Gateway`, `HTTPRoute`, and `EnvoyProxy` manifests in the Helm chart.

#### Step 2.1 — Configure nip.io Hostname + TLS Values

Update `charts/bankapp/values.yaml` before ArgoCD sync:

> **Important**: `gateway.host` must be a real `<PUBLIC_IP>.nip.io` value and `email` must be valid for Let's Encrypt ACME registration.
>
> **Important**: The deployment reads `GEMINI_API_KEY` from secret `bankapp-ai-secrets` and key `gemini-api-key`.
> If you change these names, update `charts/bankapp/templates/deployment.yaml` accordingly.

> **Important**: Replace any placeholder/default values in `charts/bankapp/values.yaml` (especially `gateway.host` and `gateway.tls.certManager.email`) with your own environment values before syncing with ArgoCD.

#### Step 2.2 — Install Cert-Manager (Required for HTTPS)

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml

kubectl wait -n cert-manager --for=condition=Available deployment/cert-manager --timeout=5m

kubectl wait -n cert-manager --for=condition=Available deployment/cert-manager-cainjector --timeout=5m

kubectl wait -n cert-manager --for=condition=Available deployment/cert-manager-webhook --timeout=5m

# Required when using HTTP01 solver with Gateway API
kubectl -n cert-manager patch deploy cert-manager --type='json' \
   -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--enable-gateway-api"}]'

kubectl -n cert-manager rollout restart deploy cert-manager

kubectl -n cert-manager rollout status deploy cert-manager

# Verify
kubectl get pods -n cert-manager 

```

#### Step 3 — Install ArgoCD

Install ArgoCD manually after Kind + Envoy setup:

```bash
kubectl create namespace argocd

kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

kubectl wait -n argocd --for=condition=Ready pods --all --timeout=5m
```

#### Step 4 — Login to ArgoCD

Fetch initial admin password and login to ArgoCD UI:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo
```

Expose ArgoCD UI (if not externally exposed):

```bash
kubectl port-forward svc/argocd-server -n argocd 8081:80 --address 0.0.0.0 &
```

Login via `http://<PUBLIC_IP>:8081` with user `admin`.

#### Step 5 — Deploy via ArgoCD (Apply Manifest)

```bash
kubectl apply -f gitops/argocd-app.yaml
```

**ArgoCD Application** (`gitops/argocd-app.yaml`):
- Points to `charts/bankapp` in this repo.
- Deploys to namespace: `bankapp-prod`.
- Auto-sync enabled with `prune: true` and `selfHeal: true`.


ArgoCD will sync `charts/bankapp` and deploy all resources.

![argocd-app-healthy](screenshots/argocd-app-healthy.png)

#### Step 6 — Verify Access (Gateway API & nip.io)
 
Once the application is synced by ArgoCD, the Gateway resource will automatically trigger the creation of the Envoy data-plane service. Thanks to our declarative configuration, this service is created as a **NodePort** with the correct mappings (30080/30443) automatically.
 
Access your app at:
- **HTTP**: `http://<YOUR_PUBLIC_IP>.nip.io`
- **HTTPS**: `https://<YOUR_PUBLIC_IP>.nip.io`
 
**Verification commands**:
```bash
kubectl get gateway bankapp-gateway -n bankapp-prod
kubectl get svc -n envoy-gateway-system
kubectl get certificate -n bankapp-prod
kubectl describe certificate bankapp-tls -n bankapp-prod
kubectl get certificaterequest,order,challenge -n bankapp-prod
```

If certificate issuance is stuck, inspect challenge reason:

```bash
kubectl describe challenge -n bankapp-prod
```

If the reason shows `gateway api is not enabled`, re-run the cert-manager patch command from Step 2.2 and recreate ACME resources.

> **Note**: `Challenge` resources are temporary. If certificate issuance already succeeded, `kubectl describe challenge -n bankapp-prod` may return `No resources found`, which is normal.
> Check `kubectl get certificate -n bankapp-prod` and confirm `READY=True`.
 
> **Note**: For Let's Encrypt to verify your domain and enable HTTPS (optional Phase 3 check), ensure your EC2 Security Group allows traffic on ports **80** and **443**.

#### Step 7 — Trigger the GitOps Pipeline

Push code to `main`. GitHub Actions will:
1. Run 8 security gates.
2. Gate 8 commits the new tag to `values.yaml`.
3. ArgoCD auto-syncs the new image to the Kind cluster.

![github-action-success](screenshots/github-action-success.png)

![app-dashboard](screenshots/app-dashboard.png)

![app-transaction](screenshots/app-transaction.png)

#### AI Assistant Behavior (Current)

- For account-specific intents like balance and transaction history, the backend can return deterministic answers directly from the database for reliability and speed.
- For open-ended prompts (for example: financial concepts), responses are generated through Gemini.
- Fast responses are therefore expected for balance/transaction questions and do not always indicate an external AI call.

<div align="center">

![ai-working](screenshots/ai-working-1.png)

![ai-transaction](screenshots/ai-transaction-working.png)

![ai-working](screenshots/ai-working-2.png)

</div>

---

## Verification & Access

| Check | Command |
| :--- | :--- |
| Cluster Nodes | `kubectl get nodes` |
| Application Pods | `kubectl get pods -n bankapp-prod` |
| ArgoCD Apps | `kubectl get applications -n argocd` |
| Gateway Status | `kubectl get gateway -n bankapp-prod` |
| HTTP Routes | `kubectl get httproute -n bankapp-prod` |

**Access Points:**
*   **BankApp (HTTP)**: `http://<YOUR_PUBLIC_IP>.nip.io/`
*   **BankApp (HTTPS)**: `https://<YOUR_PUBLIC_IP>.nip.io/`
*   **Nginx Demo**: `http://<YOUR_PUBLIC_IP>.nip.io/nginx`
*   **ArgoCD UI**: `http://<YOUR_PUBLIC_IP>:8081` (via `kubectl port-forward`)

**Path Routing:**
| Path | Backend | Description |
| :--- | :--- | :--- |
| `/` | BankApp (port 8080) | Spring Boot Banking Application |
| `/nginx` | Nginx (port 80) | Demo service to showcase Gateway API routing |

---

## Helm Chart Structure

```
charts/bankapp/
├── Chart.yaml              # Chart metadata (name: bankapp, version: 0.1.0)
├── values.yaml             # All configurable values (image, DB, Nginx)
└── templates/
    ├── _helpers.tpl        # Shared template helpers
    ├── certificate.yaml    # cert-manager Certificate for nip.io TLS
    ├── deployment.yaml     # BankApp Deployment (Docker Hub image, health probes)
    ├── envoyproxy.yaml     # Declarative NodePort configuration for Kind
    ├── gateway.yaml        # Gateway API — Gateway resource (ports 80/443)
    ├── gatewayclass.yaml   # Envoy Gateway Class (linked to EnvoyProxy)
    ├── httproute.yaml      # Gateway API — HTTPRoute (path-based routing)
    ├── issuer.yaml         # cert-manager ACME Issuer (Let's Encrypt)
    ├── mysql.yaml          # MySQL 8.0 Deployment + ClusterIP Service
    ├── nginx.yaml          # Nginx Demo Deployment + Service
    └── service.yaml        # BankApp ClusterIP Service (port 8080)
```

---

## DB Verification

```bash
kubectl exec -it <mysql-pod-name> -n bankapp-prod -- mysql -u bankuser -p bankapp -e "SELECT * FROM accounts;"
```

![mysql-test](screenshots/27.png)

---

## 🧹 Cleanup

To delete the local infrastructure:

1. **Delete Kind Cluster**:
   ```bash
   kind delete cluster --name bankapp-kind-cluster
   ```

2. **Delete EC2 Instance**
   
---

<div align="center">

Happy Learning

**TrainWithShubham**

</div>