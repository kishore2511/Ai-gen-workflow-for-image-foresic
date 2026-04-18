# Security & Secrets Plan

## Environment variables
- `JWT_SECRET`: Minimum 32 chars; never commit real value.
- `DB_URL`, `DB_USER`, `DB_PASS`, `DB_DRIVER`: DB credentials.
- `ML_PREDICT_URL`: Internal ML endpoint.
- `S3_BUCKET`, `AWS_REGION`: AWS storage configuration.

## AWS Secrets Manager naming
- `/forensic/prod/jwt_secret`
- `/forensic/prod/db_user`
- `/forensic/prod/db_pass`
- `/forensic/prod/ml_predict_url`

## Rotation recommendations
- JWT secret: every 90 days.
- DB password: every 60 days.
- IAM credentials: use role-based auth where possible; avoid static keys.

## Local development
Use `.env` values only for local dev. Never reuse dev secrets in production.
