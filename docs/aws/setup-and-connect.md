# AWS Setup & Connect Guide (Spring Boot + FastAPI + S3 + RDS)

This guide is for your exact situation: you already have the code, but you do **not** yet have AWS resources.

---

## 1) Accounts and services you must create

### Required
1. **AWS account** (root account + MFA enabled)
2. **IAM admin user** for day-to-day console/API usage (do not use root)
3. **S3 bucket** for uploaded images
4. **RDS MySQL database** for persistent data
5. **(Recommended) EC2 instance** to host backend + ML services

### Strongly recommended for production
6. **Secrets Manager** (or SSM Parameter Store) for DB password, JWT secret, AWS app config
7. **CloudWatch Logs** for backend/service logs
8. **Route53 + ACM certificate + ALB** for HTTPS public domain

---

## 2) Local prerequisites before AWS

On your local machine (or VM), make sure these are installed:
- Java 17+
- Maven 3.9+
- Python 3.10+
- pip + venv
- Docker + Docker Compose (optional but helpful)
- AWS CLI v2

Then configure AWS CLI:

```bash
aws configure
# AWS Access Key ID: <from IAM user>
# AWS Secret Access Key: <from IAM user>
# Default region name: us-east-1 (or your preferred region)
# Default output format: json
```

Verify identity:

```bash
aws sts get-caller-identity
```

---

## 3) Create IAM user/permissions (minimum needed)

### 3.1 Create IAM user
- Console: IAM → Users → Create user
- Enable access key for CLI
- Attach policies:
  - During setup: `AdministratorAccess` (temporary, easiest)
  - After setup: replace with least-privilege policy from this repo and scoped RDS permissions

### 3.2 S3 least-privilege policy
Use the template in this repo and replace `YOUR_BUCKET` with your real bucket name:
- `docs/aws/iam-policy-s3-rds-cloudwatch.json`

---

## 4) Create the S3 bucket

### Console steps
1. S3 → Create bucket
2. Bucket name: globally unique (example: `forensic-uploads-<yourname>-<rand>`)
3. Region: same region as your compute and RDS
4. Keep public access blocked
5. Create bucket

### Optional CLI
```bash
aws s3api create-bucket \
  --bucket <your-bucket-name> \
  --region <your-region> \
  --create-bucket-configuration LocationConstraint=<your-region>
```

---

## 5) Create the RDS MySQL instance

### Console steps
1. RDS → Create database
2. Engine: **MySQL**
3. Template: Free tier/dev (for testing)
4. DB instance identifier: e.g. `forensic-mysql-dev`
5. Master username/password: save securely
6. Connectivity:
   - Put in same VPC/subnet plan as your app host
   - Security group inbound: allow TCP 3306 from your app server security group (preferred)
7. Create database

After creation, copy:
- **Endpoint** (host)
- **Port** (3306)
- **DB name** (create one if needed, e.g. `forensicdb`)

Initialize schema using this repo SQL:
- `sql/schema.sql`

Example connection URL:

```text
jdbc:mysql://<rds-endpoint>:3306/forensicdb?useSSL=true&requireSSL=true
```

---

## 6) Connect your code to AWS (exact env vars)

Your code already supports AWS profile + S3 + RDS via env vars.

Set these in your deployment environment (`.env`, systemd, ECS task, etc.):

```bash
SPRING_PROFILES_ACTIVE=aws
AWS_REGION=us-east-1
S3_BUCKET=<your-bucket-name>
ML_PREDICT_URL=http://127.0.0.1:8000/predict

DB_URL=jdbc:mysql://<rds-endpoint>:3306/forensicdb?useSSL=true&requireSSL=true
DB_USER=<rds-username>
DB_PASS=<rds-password>
DB_DRIVER=com.mysql.cj.jdbc.Driver

JWT_SECRET=<long-random-secret-at-least-32-chars>
JWT_EXPIRATION_MS=86400000
```

Why these matter:
- `SPRING_PROFILES_ACTIVE=aws` switches storage to S3 implementation.
- `S3_BUCKET` is consumed by `S3StorageService`.
- `DB_*` points Spring datasource to RDS MySQL instead of local H2.

---

## 7) Run both services on the server

### 7.1 ML service
```bash
cd ml-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8000
```

### 7.2 Spring Boot backend
```bash
cd backend-java
mvn spring-boot:run
```

Backend health check:
```bash
curl http://127.0.0.1:8080/actuator/health
```

ML health check:
```bash
curl http://127.0.0.1:8000/health
```

---

## 8) Verify you are truly connected to AWS

Run these validation checks after one test upload:

1. **App works**
   - Register/login and upload image from UI/API
2. **S3 confirms object**
   ```bash
   aws s3 ls s3://<your-bucket-name>/uploads/ --recursive | tail
   ```
3. **RDS confirms row inserted**
   - Query `analysis_requests` and confirm latest record has storage key/hash/prediction
4. **History endpoint returns saved data**
   ```bash
   curl -H "Authorization: Bearer <jwt>" http://127.0.0.1:8080/api/analysis/history
   ```

If all 4 pass, your code is connected to AWS correctly.

---

## 9) Common failure fixes

1. **`AccessDenied` on S3 upload**
   - Wrong bucket name, region mismatch, or IAM policy not attached.
2. **Cannot connect to RDS**
   - Security group rules missing or wrong endpoint/user/pass.
3. **Backend can’t reach ML**
   - `ML_PREDICT_URL` incorrect for your runtime network.
4. **Still writing to local instead of S3**
   - `SPRING_PROFILES_ACTIVE` not set to `aws`.
5. **TLS/RDS errors**
   - Ensure proper JDBC SSL params for your RDS setup.

---

## 10) What to create first (recommended order)

1. AWS account + MFA
2. IAM user + AWS CLI config
3. S3 bucket
4. RDS MySQL
5. Set env vars on host
6. Run ML + backend
7. Do one upload and verify in S3 + RDS

This order minimizes confusion and gives you visible proof each connection works.
