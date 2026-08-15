# TASK 01 — Database & Flyway Consolidation

Goal

Replace incremental Phase 1–2 Flyway history (`V1`–`V35`) with a single Phase 3 baseline for disposable local and Testcontainers databases.

Required Work

- Archive `V1`–`V35` out of the active Flyway path (`classpath:db/migration`).
- Add `V1__phase3_baseline.sql` with the post-V35 schema and final reference/seed data.
- Do not replay historical UPDATE/DELETE transforms; encode their final state in the baseline.
- Rewrite tests that asserted individual Flyway versions.
- Recreate the local Compose volume after applying this task.

Verification

- Only `V1__phase3_baseline.sql` remains under `backend/src/main/resources/db/migration`.
- Historical scripts remain in `backend/src/main/resources/db/archive/phase1-phase2`.
- `Phase3BaselineSchemaIntegrationTest` passes.
- Backend tests pass against a fresh Testcontainers database.
- Hibernate `ddl-auto=validate` accepts the baseline schema.

Rollback

- Git tag `pre-phase3-flyway-v35` pins the last commit that still applied `V1`–`V35`.
- Restore archived scripts to `db/migration` and remove the Phase 3 baseline, then wipe the local volume.
