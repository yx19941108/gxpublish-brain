Owner: Codex
Task: manuscript-review-tdd
Status: Draft
Updated: 2026-03-21

# Manuscript Review TDD Implementation Plan

## Scope

- Requirement truth source: `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
- Official references: `doc/official_doc/**`
- Hard ban: do not read or reuse any old editorial business implementation, including `Brain/brain-modules/brain-editorial/**`, `plus-ui-ts/src/views/editorial/review/**`, `plus-ui-ts/src/api/editorial/review.ts`, and old editorial SQL updates.

## Confirmed Facts

- Live MySQL MCP points to `gxpublish_brain`.
- The database already contains `sys_user`, `sys_role`, `sys_menu`, `sys_dict_type`, `sys_dict_data`, and `flow_*` tables.
- The database does not yet contain manuscript-review business tables.
- JDK 17 is available and must be used for backend verification.
- `plus-ui-ts` dependencies and Playwright Chromium are already installed in this worktree.

## Work Package 1: Plan, ADR, And Module Wiring

- Goal: lock backend module naming, integration points, SQL strategy, and rollback boundaries before writing production code.
- Affected Files:
  - `doc/agent/manuscript-review-tdd_20260321-020326/decisions/ADR-0001_manuscript-review-module-implementation-strategy.md`
  - `doc/agent/manuscript-review-tdd_20260321-020326/plans/20260321_manuscript-review_tdd-implementation_plan_v01.md`
  - `Brain/brain-modules/pom.xml`
  - `Brain/brain-admin/pom.xml`
- Risk:
  - naming drift between Maven module, Java package, SQL scripts, and later frontend/API paths
  - accidental reintroduction of banned editorial semantics
- Verification:
  - ADR and plan are written
  - Maven parent/admin references resolve the new module
- Rollback:
  - remove the new module from both pom files
  - keep the ADR for audit, or delete both new docs if explicitly required

## Work Package 2: Red Tests For Domain Rules

- Goal: create failing backend tests for the first ruleset before production code.
- Affected Files:
  - `Brain/brain-modules/brain-manuscript-review/src/test/java/com/gxpublish/brain/manuscript/review/service/ManuscriptReviewServiceTest.java`
  - supporting test fixtures under the same new module
- Risk:
  - tests may accidentally encode old draft/delete behavior
  - tests may couple to persistence too early instead of domain rules
- Verification:
  - tests fail for the expected reasons
  - at minimum, fail on these rules:
    - no draft create path
    - manuscript code generation rule
    - config-missing intercept
    - certified applicant skips first approval
    - status/current-node contract
- Rollback:
  - remove the new test files

## Work Package 3: Green Slice 1

- Goal: implement the minimum backend service and model code required for the first failing tests to pass.
- Affected Files:
  - `Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/**`
- Risk:
  - implementing too much workflow/persistence logic in the first slice
  - mixing business-readable contract fields with raw technical workflow fields
- Verification:
  - targeted Maven test run turns the first selected tests green
  - tests still enforce no-draft semantics
- Rollback:
  - revert module-local source files only

## Work Package 4: DDL And DML First Slice

- Goal: define the initial database surface for manuscript review and seed the minimum supporting data.
- Affected Files:
  - `Brain/script/sql/update/20260321_manuscript_review_ddl_v01.sql`
  - `Brain/script/sql/update/20260321_manuscript_review_dml_v01.sql`
  - `Brain/script/sql/update/20260321_manuscript_review_rollback_v01.sql`
- Risk:
  - missing `tenant_id`
  - weak role/menu/dict/test-user seeds
  - irreversible SQL
- Verification:
  - apply succeeds against `gxpublish_brain`
  - new tables and seed rows are queryable
- Rollback:
  - run `20260321_manuscript_review_rollback_v01.sql`

## Work Package 5: Next Backend Expansion

- Goal: after Slice 1 is green, expand into persistence, workflow start/resubmit/cancel/reject, and history/resource lifecycle.
- Affected Files:
  - module controller/service/mapper/resource files under `Brain/brain-modules/brain-manuscript-review/**`
- Risk:
  - moving too far before the first slice is stable
  - workflow permission flag design may conflict with current warm-flow integration
- Verification:
  - add new failing tests first, then expand implementation
- Rollback:
  - stay within module boundary and paired SQL rollback

## First Verification Commands

```bash
cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/Brain
JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false test
```

## Rollout Note

- This plan intentionally starts with backend foundation only.
- Frontend and browser work should wait for the first backend contract slice and SQL seeds.
