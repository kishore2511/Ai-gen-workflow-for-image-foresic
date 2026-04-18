# Local E2E Smoke Test

1. Start ML service and backend.
2. Register user via `/api/auth/register`.
3. Login and obtain JWT from `/api/auth/login`.
4. Upload image via `/api/analysis/upload` with `Authorization: Bearer <token>`.
5. Verify `/api/analysis/history` returns a record with label/confidence/hash fields.
6. Verify backend health endpoint at `/actuator/health`.
