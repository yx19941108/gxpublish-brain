/* -------------------------------------------------------------------------- */
/* Owner: Codex                                                               */
/* Task: manuscript_review queue2 workflow/button alignment                   */
/* Status: Draft                                                              */
/* Updated: 2026-03-29 08:28 +08:00                                           */
/* -------------------------------------------------------------------------- */
/*
apply note
- Align manuscript_review queue2 runtime workflow semantics with R7.4.
- Scope:
  1) level-1 / level-2 approval buttons remove termination + transfer + file
  2) level-3 approval buttons remove transfer + file, keep termination
  3) reject target for level-1 / level-2 / level-3 converges to applicant-node
*/

SET @tenant_id := '000000';

UPDATE flow_node n
JOIN flow_definition d ON d.id = n.definition_id
SET n.any_node_skip = 'applicant-node',
    n.ext = '[{"code":"ButtonPermissionEnum","value":"back"}]',
    n.update_time = NOW(),
    n.update_by = 'codex',
    n.del_flag = '0',
    n.tenant_id = @tenant_id
WHERE d.tenant_id = @tenant_id
  AND d.del_flag = '0'
  AND d.flow_code IN ('manuscript_review_audit_flow', 'manuscript_review_proofread_flow')
  AND n.del_flag = '0'
  AND n.node_code IN ('first-review-node', 'second-review-node');

UPDATE flow_node n
JOIN flow_definition d ON d.id = n.definition_id
SET n.any_node_skip = 'applicant-node',
    n.ext = '[{"code":"ButtonPermissionEnum","value":"back,termination"}]',
    n.update_time = NOW(),
    n.update_by = 'codex',
    n.del_flag = '0',
    n.tenant_id = @tenant_id
WHERE d.tenant_id = @tenant_id
  AND d.del_flag = '0'
  AND d.flow_code IN ('manuscript_review_audit_flow', 'manuscript_review_proofread_flow')
  AND n.del_flag = '0'
  AND n.node_code = 'final-review-node';
