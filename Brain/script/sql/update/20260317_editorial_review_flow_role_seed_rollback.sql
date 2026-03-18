-- editorial review rebuild: flow and role alignment rollback
-- note: seeded roles are retained to avoid deleting potentially reused role ids.

SET @now_time := NOW();
SET @editorial_definition_id := (
    SELECT `id`
    FROM `flow_definition`
    WHERE `flow_code` = 'editorial_review_flow' AND `del_flag` = '0'
    ORDER BY `id` DESC
    LIMIT 1
);

UPDATE `flow_definition`
SET `form_path` = '/editorial/review/reviewEdit',
    `update_time` = @now_time,
    `update_by` = '1'
WHERE `id` = @editorial_definition_id;

DELETE FROM `flow_skip`
WHERE `definition_id` = @editorial_definition_id
  AND `now_node_code` IN ('applicant-node', 'first-review-node', 'second-review-node', 'final-review-node');

SET @rollback_skip_seed_base := (SELECT IFNULL(MAX(id), 0) FROM `flow_skip`);
INSERT INTO `flow_skip`
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`,
 `skip_type`, `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`,
 `tenant_id`)
VALUES
(@rollback_skip_seed_base := @rollback_skip_seed_base + 1, @editorial_definition_id, 'applicant-node', 1,
 'dept-audit-node', 1, NULL, 'PASS', NULL, '300,200;360,200', @now_time, '1', @now_time, '1', '0', '000000'),
(@rollback_skip_seed_base := @rollback_skip_seed_base + 1, @editorial_definition_id, 'dept-audit-node', 1,
 'final-audit-node', 1, NULL, 'PASS', NULL, '480,200;540,200', @now_time, '1', @now_time, '1', '0', '000000'),
(@rollback_skip_seed_base := @rollback_skip_seed_base + 1, @editorial_definition_id, 'final-audit-node', 1,
 'end-node', 2, NULL, 'PASS', NULL, '660,200;720,200', @now_time, '1', @now_time, '1', '0', '000000');

DELETE FROM `flow_node`
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = 'final-review-node';

UPDATE `flow_node`
SET `node_code` = 'dept-audit-node',
    `node_name` = '部门经理审批',
    `permission_flag` = 'role:1',
    `coordinate` = '420,200|420,200',
    `form_path` = '/editorial/review/reviewEdit',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]'
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = 'first-review-node';

UPDATE `flow_node`
SET `node_code` = 'final-audit-node',
    `node_name` = '总编室审批',
    `permission_flag` = 'role:1',
    `coordinate` = '600,200|600,200',
    `form_path` = '/editorial/review/reviewEdit',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]'
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = 'second-review-node';

UPDATE `flow_node`
SET `node_name` = '审校申请',
    `permission_flag` = '',
    `coordinate` = '240,200|240,200',
    `form_path` = '/editorial/review/reviewEdit',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,file"}]'
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = 'applicant-node';
