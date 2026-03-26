-- Owner: Codex
-- Task: manuscript_review WP6-G1 backProcess route fix
-- Status: Draft
-- Updated: 2026-03-26 15:10 +08:00

-- 目标：
-- 为 manuscript_review 审批节点补齐 `REJECT` 类型回退边，
-- 使 `/workflow/task/backProcess` 能从审批节点返回 `applicant-node`。

SET @tenant_id := '000000';
SET @operator := 'codex';

SET @audit_definition_id := 2026032500000000101;
SET @proof_definition_id := 2026032500000000102;

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032600000002108, @audit_definition_id, 'first-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
       '390,160;300,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @audit_definition_id
      AND now_node_code = 'first-review-node'
      AND next_node_code = 'applicant-node'
      AND skip_type = 'REJECT'
      AND del_flag = '0'
);

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032600000002109, @audit_definition_id, 'second-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
       '570,160;300,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @audit_definition_id
      AND now_node_code = 'second-review-node'
      AND next_node_code = 'applicant-node'
      AND skip_type = 'REJECT'
      AND del_flag = '0'
);

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032600000002110, @audit_definition_id, 'final-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
       '750,160;300,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @audit_definition_id
      AND now_node_code = 'final-review-node'
      AND next_node_code = 'applicant-node'
      AND skip_type = 'REJECT'
      AND del_flag = '0'
);

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032600000002208, @proof_definition_id, 'first-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
       '390,160;300,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @proof_definition_id
      AND now_node_code = 'first-review-node'
      AND next_node_code = 'applicant-node'
      AND skip_type = 'REJECT'
      AND del_flag = '0'
);

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032600000002209, @proof_definition_id, 'second-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
       '570,160;300,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @proof_definition_id
      AND now_node_code = 'second-review-node'
      AND next_node_code = 'applicant-node'
      AND skip_type = 'REJECT'
      AND del_flag = '0'
);

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032600000002210, @proof_definition_id, 'final-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
       '750,160;300,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @proof_definition_id
      AND now_node_code = 'final-review-node'
      AND next_node_code = 'applicant-node'
      AND skip_type = 'REJECT'
      AND del_flag = '0'
);
