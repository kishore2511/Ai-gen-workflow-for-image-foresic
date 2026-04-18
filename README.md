# Defense Against AI-Generated Visual Media (Cloud-Ready MVP)

This repository provides a one-day MVP for deepfake-image screening:

- **Frontend**: Thymeleaf + Bootstrap pages (login/register/upload/history)
- **Backend**: Spring Boot (auth, upload, hashing, history)
- **ML Service**: FastAPI image classification endpoint (`real` / `fake`)
- **Storage**: local filesystem by default, S3-ready using Spring `aws` profile
- **Database**: H2 by default, RDS/MySQL-ready through environment variables

## Quick start (local)

### 1) Start the Python ML service
```bash
cd ml-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --reload --port 8000
```

Optional: place a trained EfficientNet checkpoint at `ml-service/best_model.pt`.
If this file is not present, the service automatically falls back to the deterministic heuristic model for local development.

### 2) Start Spring Boot backend
```bash
cd backend-java
mvn spring-boot:run
```

Open: `http://localhost:8080`

### 3) One-command local option
```bash
make up
```

### 4) Environment templates
```bash
cp .env.example .env
cp backend-java/.env.example backend-java/.env
cp ml-service/.env.example ml-service/.env
```

## Local quality gates (non-AWS)
- Run ML tests:
```bash
make test-ml
```
- Run backend tests:
```bash
make test-backend
```
- Run local end-to-end smoke test (register/login/upload/history):
```bash
make e2e
```

## API endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/analysis/upload` (Bearer token required)
- `GET /api/analysis/history` (Bearer token required)
- ML service: `GET /health`, `POST /predict`
- Backend health: `GET /actuator/health`


## Progress check against your 1-day plan (local-first)
Given your current constraint (no AWS account/S3/RDS yet), the right target is to make the **local pipeline 100% green** first.

### Local 100% target (no AWS)
You are considered done locally when this command succeeds end-to-end:

```bash
make local-100
```

That command now:
1. Starts Docker services for backend + ML.
2. Waits for `/health` and `/actuator/health`.
3. Runs the full local auth/upload/history smoke flow.

For the detailed local-only completion plan, use:
- `docs/local-100-checklist.md`

## Local-to-AWS migration
1. Create AWS account and IAM user/role with least-privilege access.
2. Create S3 bucket and set `SPRING_PROFILES_ACTIVE=aws`, `S3_BUCKET=...`.
3. Create RDS MySQL instance and set `DB_URL`, `DB_USER`, `DB_PASS`, `DB_DRIVER=com.mysql.cj.jdbc.Driver`.
4. Move secrets from `.env` to AWS Secrets Manager for production.
5. Set `AWS_REGION` to match your target AWS deployment region.

## Security notes
- Passwords are hashed using BCrypt.
- SHA-256 is computed for file content and prediction payload integrity.
- JWT protects private API endpoints.
- AES encryption is not yet wired into DB fields; add JPA attribute encryption if required by evaluator.

## Important model note
`ml-service/model.py` contains a deterministic placeholder heuristic model so the full pipeline works immediately. Replace it with a trained EfficientNet checkpoint for final accuracy reporting.

## Hardening docs
- Secrets strategy: `docs/security-secrets.md`
- IAM policy template: `docs/aws/iam-policy-s3-rds-cloudwatch.json`
- Bucket policy template: `docs/aws/bucket-policy-template.json`
- E2E smoke checklist: `docs/e2e.md`
- Automated E2E script: `scripts/local_e2e.sh`
