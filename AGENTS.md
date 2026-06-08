# AGENTS.md

## Scope
This file defines project-level execution rules for `gxpublish-brain`.

## Documentation Priority (Required)
For this project, documentation and context priority is:
1. **Primary**: this repository's documentation, configuration, scripts, and code comments.
2. **Secondary**: official `RuoYi-Vue-Plus` documentation (used as supplementary context and cross-check input).
3. **Tertiary**: general ecosystem docs for related dependencies.

## Core Planning Rule (Required)
Before proposing or executing a plan, the agent must:
1. Read relevant local repository docs/config/scripts first.
2. Use official `RuoYi-Vue-Plus` docs to supplement and validate framework/stack assumptions.
3. Identify technology stack constraints (frameworks, versions, integration patterns) from collected context.
4. Map constraints to this repository implementation (`Brain` backend, `plus-ui-ts` frontend).
5. Build and execute the task plan based on that mapping.

Conflict handling:
1. If local repository docs conflict with official docs, the agent must first notify the user and clearly list the conflict points.
2. The agent must provide a recommended approach with brief rationale and risks/tradeoffs.
3. The agent must wait for the user's choice before executing conflict-sensitive changes.
4. If the user explicitly requests standardization to upstream, follow user instruction and document the gap.

Availability fallback:
1. If official docs are unavailable, continue with local repository context and state assumptions.
2. If local docs are insufficient, use official docs/ecosystem docs as supplement and mark risk points clearly.

## Goal
Deliver safe, testable, and rollback-friendly changes without breaking current behavior.
Prioritize small and verifiable steps over large rewrites.

## Project Structure
- `Brain/`: backend (Spring Boot 3 + Maven multi-module)
- `plus-ui-ts/`: frontend (Vue3 + TypeScript + Vite)
- `Brain/script/sql/`: initialization SQL and multi-database scripts
- `Brain/script/sql/update/`: version upgrade SQL scripts

## Technical Baseline
- Backend: JDK 17, Maven, Spring Boot 3.5.x, MyBatis-Plus
- Frontend: Node >= 20.15, npm >= 8.19, Vite 6, Element Plus
- Local integration ports: backend `9888`, frontend `9999`
- Frontend proxy: `/dev-api` -> `http://localhost:9888`

## Backend Module Mapping
- Startup entry: `Brain/brain-admin`
- Business modules: `Brain/brain-modules`
- Key submodules:
  - `brain-system`
  - `brain-job`
  - `brain-generator`
  - `brain-workflow`
  - `brain-manuscript-review`
  - `brain-demo`

## Change Rules
1. If API fields change, update backend DTO/VO, frontend `src/api`, and corresponding frontend `types` together.
2. If manuscript review workflow changes, verify both:
   - Flow definition and seed scripts under `Brain/script/sql/update/manuscript_review/`
   - Frontend pages/routes under `plus-ui-ts/src/views/manuscript-review/*`
3. If DB schema changes, provide upgrade scripts in `Brain/script/sql/update`.
4. If database compatibility is required, keep `oracle/postgres/sqlserver` scripts aligned.
5. Never commit build artifacts or temp files (`target/`, `dist/`, `logs/`, `data/`, `*.jar`, `*.zip`, etc.).
6. Never commit real credentials, secrets, tokens, or production-only endpoints.
7. Avoid repository-wide reformatting; format only touched files.

## Common Commands

### Backend
```bash
cd Brain
mvn clean package -DskipTests
mvn -pl brain-admin -am test -DskipTests=false
mvn -pl brain-admin -am spring-boot:run
```

### Frontend
```bash
cd plus-ui-ts
npm install
npm run dev
npm run lint:eslint
npm run build:prod
```

## Delivery Workflow
1. Implement a minimal runnable change first, then iterate.
2. For each delivery, include:
   - change scope
   - impact surface
   - verification method and result
   - rollback approach
3. For config/script changes, state environment prerequisites and differences (local/test/prod).

## Acceptance Checklist
1. Backend build passes.
2. Frontend lint/build passes.
3. Integration endpoints behave as expected.
4. Key business path smoke test is completed.
5. No secret leakage and no unrelated file changes.
