Owner: Codex
Task: manuscript-review-tdd
Status: Accepted
Updated: 2026-03-21

# ADR-0001: Manuscript Review Module Implementation Strategy

## Status

Accepted

## Context

- The current manuscript-review line is a clean-slate effort.
- The user set an explicit hard ban against reusing any old editorial business implementation.
- The requirement document still requires reuse of platform capabilities: roles, tenants, dictionaries, OSS, BPM todo/done pages, workflow handling, and history.
- Live database facts show platform tables exist, while manuscript-review business tables do not yet exist.

## Decision

### 1. Create A New Backend Module

- Maven module name: `brain-manuscript-review`
- Java package root: `com.gxpublish.brain.manuscript.review`
- Parent integration points:
  - add the new module to `Brain/brain-modules/pom.xml`
  - add the new dependency to `Brain/brain-admin/pom.xml`

### 2. Keep The First Slice Backend-Only

- First slice owns:
  - ADR and implementation plan
  - backend module skeleton
  - failing tests
  - minimum green implementation
  - DDL/DML/rollback scripts
- Frontend and browser work should consume the first backend contract instead of guessing it.

### 3. Reuse Only Generic Platform Capabilities

- Allowed workflow reference surface:
  - `Brain/brain-common/**`
  - `Brain/brain-modules/brain-workflow/**`
  - official warm-flow docs under `doc/official_doc/warm-flow-doc/**`
- Allowed platform reference surface:
  - `Brain/brain-system/**`
  - official RuoYi-Vue-Plus docs under `doc/official_doc/ruoyi-vue-plus-plus-doc/**`
- Forbidden reference surface:
  - all old editorial business implementation and SQL updates listed in the hard ban

### 4. Domain And Table Strategy

- Business tables will be created fresh under manuscript-review naming, not editorial naming.
- First slice tables should cover:
  - manuscript review main record
  - attachment history/current lifecycle
  - external links lifecycle
  - video marker lifecycle
  - flow-role mapping/configuration
- All business tables must include `tenant_id`.

### 5. Seed Strategy

- DML will seed:
  - workflow role rows
  - menu/button rows
  - dictionary rows needed by the module
  - test users and user-role bindings
- Seeds must avoid delete/draft semantics.

### 6. Workflow Permission Strategy

- Current warm-flow official docs describe `permissionFlag` in terms of `role:<id>`, user id, and department id through `PermissionHandler`.
- Recommendation for the first slice:
  - keep runtime integration compatible with the current workflow engine contract
  - expose business configuration in role terms, but convert to engine-acceptable permission flags at runtime
  - do not leak technical permission flags to business-facing APIs or UI contracts

### 7. TDD Strategy

- No production code before a failing test.
- First red/green order:
  - no draft create
  - manuscript code generation
  - config missing intercept
  - certified applicant skip-first
  - business-readable status/current-node contract

## Consequences

- The implementation cost is higher than patching the old module, but it avoids carrying forward incorrect business semantics.
- The first slice can verify platform integration without depending on old editorial tables or APIs.
- Frontend work must align to the new contract, not old editorial paths.

## Rollback

- Remove the new module from Maven wiring.
- Apply manuscript-review rollback SQL.
- Delete module-local source if the line is abandoned.
