# Local 100% Completion Checklist (No AWS)

Use this when your immediate goal is to finish a fully working local MVP before any cloud deployment.

## Definition of done
You are **100% complete locally** when all of the following pass:

1. ML service health is `ok` at `GET /health`.
2. Spring backend health is `UP` at `GET /actuator/health`.
3. Register/login works and returns JWT.
4. Uploading an image returns label, confidence, and SHA-256 fields.
5. History endpoint returns the saved result.
6. The one-command local verifier succeeds:
   ```bash
   make local-100
   ```

## Hour-by-hour local execution plan (no cloud dependencies)

### Hour 0–1: Environment bootstrap
- Copy env templates:
  - `cp .env.example .env`
  - `cp backend-java/.env.example backend-java/.env`
  - `cp ml-service/.env.example ml-service/.env`
- Ensure Docker is running.

### Hour 1–3: Build and run services
- Start local stack:
  ```bash
  docker compose up --build -d
  ```
- Verify health endpoints manually.

### Hour 3–5: Functional auth + upload verification
- Register/login from Postman or browser UI.
- Upload a sample image and verify response includes:
  - `predictionLabel`
  - `confidence`
  - `imageSha256`
  - `resultSha256`

### Hour 5–7: Local data integrity checks
- Confirm DB writes by checking history endpoint.
- Confirm upload files are stored locally (default upload directory).

### Hour 7–9: Automated local smoke coverage
- Run:
  ```bash
  make e2e
  ```
- Fix any runtime/config issue until consistently green.

### Hour 9–10: Security baseline (local)
- Ensure no hardcoded secrets in tracked files.
- Keep secrets in `.env` only and out of commits.

### Hour 10–12: Final local sign-off
- Run one-command verification:
  ```bash
  make local-100
  ```
- Capture screenshots and demo steps for presentation.

## Optional: keep services running after verification
If you want `make local-100` not to tear down containers automatically:

```bash
KEEP_STACK_UP=1 make local-100
```
