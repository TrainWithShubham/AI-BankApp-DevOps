<div align="center">

# DevSecOps Banking Application

A high-performance, containerized financial platform built with Spring Boot 3, Java 21, and integrated Contextual AI. This project implements a secure "DevSecOps Pipeline" using GitHub Actions, OIDC authentication, and AWS managed services.

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-orange.svg)](.github/workflows/devsecops.yml)
[![AWS OIDC](https://img.shields.io/badge/Security-OIDC-red.svg)](#phase-3-security-and-identity-configuration)

</div>

![dashboard](screenshots/1.png)

---

## Technical Architecture

The application is deployed across a multi-tier, segmented AWS environment. The control plane leverages GitHub Actions with integrated security gates at every stage.

```mermaid
graph TD
    subgraph "External Control Plane"
        GH[GitHub Actions]
        User[User Browser]
    end

    subgraph "AWS Infrastructure (VPC)"
        subgraph "Application Tier"
            AppEC2[App EC2 - Ubuntu/Docker]
        end

        subgraph "Data Tier"
            RDS[(Amazon RDS - MySQL 8.0)]
        end

        subgraph "Artificial Intelligence Tier"
            Ollama[Ollama EC2 - AI Engine]
        end

        subgraph "Identity & Secrets"
            Secrets[AWS Secrets Manager]
            OIDC[IAM OIDC Provider]
        end

        subgraph "Registry"
            ECR[Amazon ECR]
        end
    end

    GH -->|1. OIDC Authentication| OIDC
    GH -->|2. Push Scanned Image| ECR
    GH -->|3. SSH Orchestration| AppEC2
    GH -->|4. DAST Scan| AppEC2
    
    User -->|Port 8080| AppEC2
    AppEC2 -->|JDBC Connection| RDS
    AppEC2 -->|REST Integration| Ollama
    AppEC2 -->|Runtime Secrets| Secrets
    AppEC2 -->|Pull Image| ECR
```

---

## Security Pipeline (DevSecOps Pipeline)

The CI/CD pipeline enforces **9 sequential security gates** before any code reaches production:

| Gate | Name | Tool | Purpose |
| :---: | :--- | :--- | :--- |
| 1 | Secret Scan | Gitleaks | Scans entire Git history for leaked secrets |
| 2 | Lint | Checkstyle | Enforces Java Google-Style coding standards |
| 3 | SAST | Semgrep | Scans Java source code for security flaws and OWASP Top 10 |
| 4 | SCA | OWASP Dependency Check (first time run can take more than 30+ minutes) | Scans Maven dependencies for known CVEs |
| 5 | Build | Maven | Compiles and packages the application |
| 6 | Container Scan | Trivy | Scans the Docker image for OS and library vulnerabilities |
| 7 | Push | Amazon ECR | Pushes the image only after Trivy passes |
| 8 | Deploy | SSH / Docker Compose | Automated deployment to AWS EC2 |
| 9 | DAST | OWASP ZAP | Dynamic attack surface scanning on live app |

---

## Technology Stack

- **Backend Framework**: Java 21, Spring Boot 3.4.1
- **Security Strategy**: Spring Security, IAM OIDC, Secrets Manager
- **Persistence Layer**: Amazon RDS for MySQL 8.0 (Dev/Test Tier)
- **AI Integration**: Ollama (TinyLlama)
- **DevOps Tooling**: Docker, Docker Compose, GitHub Actions, AWS CLI, jq
- **Infrastructure**: Amazon EC2, Amazon ECR, Amazon VPC

---

## Implementation Phases

### Phase 1: AWS Infrastructure Initialization

1. **Container Registry (ECR)**:
   - Establish a private ECR repository named `devsecops-bankapp`.

      ![ECR](screenshots/2.png)

2. **Application Server (EC2)**:

   - Deploy an Ubuntu 22.04 instance with below `User Data`.

      ```bash
      #!/bin/bash

      sudo apt update 
      sudo apt install -y docker.io docker-compose-v2 jq mysql-client
      sudo usermod -aG docker ubuntu
      sudo newgrp docker
      sudo snap install aws-cli --classic
      ```

   - Configure Security Group to open inbound rule for Port 22 (Management) and Port 8080 (Service).

      > Better to give `name` to Security Group created.

   - Create an IAM Instance Profile(IAM EC2 role) containing permissions:
     - `AmazonEC2ContainerRegistryPowerUser`
     - `AWSSecretsManagerClientReadOnlyAccess`

        ![Permissions](screenshots/3.png)

   - Attach it to Application EC2. Select EC2 -> Actions -> Security -> Modify IAM role -> Attach created IAM role.

      ![IAM role](screenshots/4.png)
   
   - Connect to EC2 Instance and Run below command to check whether IAM role is working or not.

      ```bash
      aws sts get-caller-identity
      ```

      You will get your account details with IAM role assumed.

3. **Database Tier (RDS)**:
   - Provision a MySQL 8.0 instance using the **Dev/Test** template.

     ![rds](screenshots/5.png)

   - Utilize the **Set up EC2 connection** feature to automatically establish connectivity with the Application EC2.

     ![rds-ec2](screenshots/6.png)

     ![rds-db-created](screenshots/7.png)

4. **AI Engine Tier (Ollama)**:
   - Deploy a dedicated Ubuntu EC2 instance.
   - Open Inbound Port `11434` from the Application EC2 Security Group.

      > Better to give `name` to Security Group created.
    
      ![ollama-sg](screenshots/8.png)

   - Automate initialization using the [ollama-setup.sh](scripts/ollama-setup.sh) script via EC2 User Data.
    
      ![user-data](screenshots/9.png)

   - Verify the AI engine is responsive and the model is pulled in `AI engine EC2`:

     ```bash
     ollama list
     ```

      ![ollama-list](screenshots/21.png)

---

### Phase 2: Database Initialization

1. **Schema Provisioning**:
   - Access the RDS instance from the Application EC2:

     ```bash
     mysql -h <RDS-ENDPOINT> -u <USERNAME> -p
     ```

   - Initialize the application database:

     ```sql
     CREATE DATABASE bankappdb;
     EXIT;
     ```

---

### Phase 3: Security and Identity Configuration

The deployment pipeline utilizes OpenID Connect (OIDC) for secure, keyless authentication between GitHub and AWS.

1. **IAM Identity Provider**:
   - Provider URL: `https://token.actions.githubusercontent.com`
   - Audience: `sts.amazonaws.com`

      ![identity-provider](screenshots/10.png)

2. **Deployment Role**:
   - Click on created `Identity provider`
   - Asign & Create a role named `GitHubActionsRole`.
   - Enter following details:
      - `Identity provider`: Select created one.
      - `Audience`: Select created one.
      - `GitHub organization`: Your GitHub Username or Orgs Name where this repo is located.
      - `GitHub repository`: Write the Repository name of this project. `(e.g, DevSecOps-Bankapp)`
      - `GitHub branch`: branch to use for this project `(e.g, aws)`
      - Click on `Next`

      ![role](screenshots/11.png)

   - Assign `AmazonEC2ContainerRegistryPowerUser` permissions.

      ![iam permission](screenshots/12.png)

   - Click on `Next`, Enter name of role and click on `Create role`.

      ![iam role](screenshots/13.png)

---

### Phase 4: Secrets and Pipeline Configuration

#### 1. AWS Secrets Manager
Create a secret named `bankapp/prod-secrets` in `Other type of secret` with the following key-value pairs:

| Secret Key | Description |
| :--- | :--- |
| `DB_HOST` | The RDS instance endpoint address |
| `DB_PORT` | The database port (standard is `3306`) |
| `DB_NAME` | The application database name (`bankappdb`) |
| `DB_USER` | The administrative username for the RDS instance |
| `DB_PASSWORD` | The administrative password for the RDS instance |
| `OLLAMA_URL` | The private URL for the AI tier (`http://<PRIVATE-IP>:11434`) |

![aws-ssm](screenshots/14.png)

#### 2. GitHub Repository Secrets
Configure the following Action Secrets within your GitHub repository settings:

| Secret Name | Description |
| :--- | :--- |
| `AWS_ROLE_ARN` | The ARN of the `GitHubActionsRole` |
| `AWS_REGION` | The AWS region where resources are deployed |
| `AWS_ACCOUNT_ID` | Your 12-digit AWS account number |
| `ECR_REPOSITORY` | The name of the ECR repository (`devsecops-bankapp`) |
| `EC2_HOST` | The public IP address of the Application EC2 |
| `EC2_USER` | The SSH username (default is `ubuntu`) |
| `EC2_SSH_KEY` | The content of your private SSH key (`.pem` file) |
| `NVD_API_KEY` | Free API key from [nvd.nist.gov](https://nvd.nist.gov/developers/request-an-api-key) for OWASP SCA scans |

> **Note**: The `NVD_API_KEY` raises the NVD API rate limit from ~5 requests/30s to 50 requests/30s, reducing the OWASP Dependency Check scan time from 30+ minutes to under 8 minutes. Without it the SCA job will time out.

#### Obtaining the NVD API Key

**Step 1: Request the API Key**
- Go to [https://nvd.nist.gov/developers/request-an-api-key](https://nvd.nist.gov/developers/request-an-api-key).
- Enter your `Organzation name`, `email address`, and select `organization type`.
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

**Step 4: Add as GitHub Secret**
- Go to your repository on GitHub.
- Navigate to **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.
- Name: `NVD_API_KEY`
- Value: Paste the copied API key.
- Click **Add Secret**.

![github-secret](screenshots/15.png)

---

## Continuous Integration and Deployment

The DevSecOps lifecycle is orchestrated through the [DevSecOps Main Pipeline](.github/workflows/devsecops-main.yml), which securely sequences three modular workflows: [CI](.github/workflows/ci.yml), [Build](.github/workflows/build.yml), and [CD](.github/workflows/cd.yml). Together they enforce **9 sequential security gates** before any code reaches production. Every `git push` to the `main` or `aws` branch triggers the full pipeline automatically.

| Gate | Job | Tool | Action |
| :---: | :--- | :--- | :--- |
| 1 | `gitleaks` | Gitleaks | **Strict**: Fails if any secrets are found in history. |
| 2 | `lint` | Checkstyle | **Audit**: Reports style violations but doesn't block (Google Style). |
| 3 | `sast` | Semgrep | **Strict**: Scans code for vulnerabilities. Fails on findings. |
| 4 | `sca` | OWASP Dependency Check | **Strict**: Fails if any dependency has CVSS > 7.0. |
| 5 | `build` | Maven | Standard build and test stage. |
| 6 | `image_scan` | Trivy | **Strict**: Scans Docker image layers. Fails on any High/Critical CVE. |
| 7 | `push_to_ecr` | Amazon ECR | Pushes the verified image to AWS ECR using OIDC. |
| 8 | `deploy` | SSH / Docker Compose | Fetches secrets from AWS Secrets Manager and recreates the container. |
| 9 | `dast` | OWASP ZAP | **Audit Mode**: Comprehensive scan that reports findings as artifacts, but does not block the pipeline. |

All scan reports (OWASP, Trivy, ZAP) are uploaded as downloadable **Artifacts** in each GitHub Actions run, YOu can look into the **Artifacts**.

- CI/CD

   ![github-actions](screenshots/16.png)

- Artifacts

   ![artifacts](screenshots/26.png)
   
---

## Operational Verification

- **Process Status**: `docker ps`

  ![docker ps](screenshots/19.png)

- **Application Working**:

  ![app](screenshots/20.png)

- **Database Connectivity**: 

  ```bash
  mysql -h <RDS-ENDPOINT> -u <USER> -p bankappdb -e "SELECT * FROM accounts;"
  ```

  ![mysql-result](screenshots/17.png)

- **Network Validation**: 

  ```bash
  nc -zv <OLLAMA-PRIVATE-IP> 11434
  ```

  ![ollama-success](screenshots/18.png)

---

<div align="center">

Happy Learning

**TrainWithShubham**  

</div>