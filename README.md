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

## API endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/analysis/upload` (Bearer token required)
- `GET /api/analysis/history` (Bearer token required)
- ML service: `GET /health`, `POST /predict`

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
