# Architecture Overview

1. User registers/logs in via Spring Boot web UI.
2. Passwords are hashed with BCrypt and persisted in SQL DB.
3. Authenticated user uploads image to `/api/analysis/upload`.
4. Backend stores file in local storage (or S3 in `aws` profile).
5. Backend calls Python ML service (`/predict`) for fake/real detection.
6. Backend computes SHA-256 for image bytes and prediction payload.
7. Metadata and results are stored in DB and shown in history UI.

## Deployment path
- Local: H2 + local upload directory + FastAPI.
- AWS: RDS MySQL + S3 bucket + IAM role + same Java/Python services.
