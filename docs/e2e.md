# Local E2E Smoke Test

## Automated path (recommended)
```bash
make e2e
# or
./scripts/local_e2e.sh
```

This validates:
1. `GET /health` on ML service
2. `GET /actuator/health` on backend
3. User register + login
4. Authenticated upload to `/api/analysis/upload`
5. Authenticated fetch from `/api/analysis/history`
6. Response invariants: label/confidence + SHA-256 fields

## Manual path
1. Start ML service and backend.
2. Register user via `/api/auth/register`.
3. Login and obtain JWT from `/api/auth/login`.
4. Upload image via `/api/analysis/upload` with `Authorization: Bearer <token>`.
5. Verify `/api/analysis/history` returns a record with label/confidence/hash fields.
6. Verify backend health endpoint at `/actuator/health`.
