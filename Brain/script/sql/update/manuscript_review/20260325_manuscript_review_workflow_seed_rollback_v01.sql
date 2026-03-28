/* -------------------------------------------------------------------------- */
/* Owner: Codex                                                               */
/* Task: manuscript_review WP6-G1-M1 workflow seed rollback                   */
/* Status: Draft for local dev/test rollback only                             */
/* Updated: 2026-03-25 14:45 +08:00                                           */
/* -------------------------------------------------------------------------- */
/*
rollback note
- Execute only when manuscript_review workflow seed created by
  20260325_manuscript_review_workflow_seed_v01.sql needs to be reverted.
- If runtime instances/tasks have already been created, clean runtime data first.
*/

SET @tenant_id := '000000';

DELETE FROM brain_manuscript_review_flow_config
WHERE tenant_id = @tenant_id
  AND id IN (2026032500000003101, 2026032500000003102);

DELETE FROM flow_skip
WHERE tenant_id = @tenant_id
  AND definition_id IN (2026032500000000101, 2026032500000000102);

DELETE FROM flow_node
WHERE tenant_id = @tenant_id
  AND definition_id IN (2026032500000000101, 2026032500000000102);

DELETE FROM flow_definition
WHERE tenant_id = @tenant_id
  AND id IN (2026032500000000101, 2026032500000000102);
