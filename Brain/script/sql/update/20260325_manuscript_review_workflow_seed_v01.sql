/* -------------------------------------------------------------------------- */
/* Owner: Codex                                                               */
/* Task: manuscript_review WP6-G1-M1 workflow seed                            */
/* Status: Draft for local dev/test apply only                                */
/* Updated: 2026-03-25 14:45 +08:00                                           */
/* -------------------------------------------------------------------------- */
/*
apply order
1) 20260321_manuscript_review_ddl_v01.sql
2) 20260321_manuscript_review_dml_v01.sql
3) 20260323_manuscript_review_dev_login_seed_v01.sql
4) 20260323_manuscript_review_execution_role_matrix_seed_v01.sql
5) 20260325_manuscript_review_workflow_seed_v01.sql

rollback order
1) 20260325_manuscript_review_workflow_seed_rollback_v01.sql

scope
- formal workflow seed for manuscript_review
- seeds flow_definition / flow_node / flow_skip / brain_manuscript_review_flow_config
- does not seed dev-only review data
*/

SET @tenant_id := '000000';
SET @operator := 'codex';
SET @form_path := '/manuscript/review/approval';

SET @audit_definition_id := 2026032500000000101;
SET @proof_definition_id := 2026032500000000102;

SET @audit_flow_code := 'manuscript_review_audit_flow';
SET @proof_flow_code := 'manuscript_review_proofread_flow';

INSERT INTO flow_definition
(`id`, `flow_code`, `flow_name`, `model_value`, `category`, `version`, `is_publish`, `form_custom`, `form_path`,
 `activity_status`, `listener_type`, `listener_path`, `ext`, `create_time`, `create_by`, `update_time`, `update_by`,
 `del_flag`, `tenant_id`)
SELECT @audit_definition_id, @audit_flow_code, '稿件审校-审核流程', 'CLASSICS', 'manuscript', '1', 1, 'N', @form_path,
       1, NULL, NULL, NULL, NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM flow_definition
    WHERE tenant_id = @tenant_id
      AND flow_code = @audit_flow_code
      AND version = '1'
      AND del_flag = '0'
);

INSERT INTO flow_definition
(`id`, `flow_code`, `flow_name`, `model_value`, `category`, `version`, `is_publish`, `form_custom`, `form_path`,
 `activity_status`, `listener_type`, `listener_path`, `ext`, `create_time`, `create_by`, `update_time`, `update_by`,
 `del_flag`, `tenant_id`)
SELECT @proof_definition_id, @proof_flow_code, '稿件审校-校对流程', 'CLASSICS', 'manuscript', '1', 1, 'N', @form_path,
       1, NULL, NULL, NULL, NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM flow_definition
    WHERE tenant_id = @tenant_id
      AND flow_code = @proof_flow_code
      AND version = '1'
      AND del_flag = '0'
);

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001101, 0, @audit_definition_id, 'start-node', '开始', NULL, '0.000', '100,200|100,200',
       NULL, NULL, NULL, 'N', NULL, '1', NOW(), @operator, NOW(), @operator, NULL, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'start-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001102, 1, @audit_definition_id, 'applicant-node', '审校申请', '', '0.000', '240,200|240,200',
       NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'applicant-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001103, 1, @audit_definition_id, 'first-review-node', '一级审批', '${manuscriptReviewFirstLevelApprover}',
       '0.000', '420,200|420,200', NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,transfer,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'first-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001104, 1, @audit_definition_id, 'second-review-node', '二级审批', '${manuscriptReviewSecondLevelApprover}',
       '0.000', '600,200|600,200', NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,transfer,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'second-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001105, 1, @audit_definition_id, 'final-review-node', '三级审批', '${manuscriptReviewThirdLevelApprover}',
       '0.000', '780,200|780,200', NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,transfer,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'final-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001106, 2, @audit_definition_id, 'end-node', '结束', NULL, '0.000', '960,200|960,200',
       NULL, NULL, NULL, 'N', NULL, '1', NOW(), @operator, NOW(), @operator, NULL, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'end-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001107, 3, @audit_definition_id, 'certified-route-node', 'Certified Route', NULL, '0.000',
       '420,200|420,200', NULL, NULL, NULL, 'N', NULL, '1', NOW(), @operator, NOW(), @operator, '[]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'certified-route-node' AND del_flag = '0');

UPDATE flow_node
SET node_type = 3,
    node_name = 'Certified Route',
    permission_flag = NULL,
    node_ratio = '0.000',
    coordinate = '420,200|420,200',
    form_custom = 'N',
    form_path = NULL,
    ext = '[]',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @audit_definition_id
  AND node_code = 'certified-route-node';

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002101, @audit_definition_id, 'start-node', 0, 'applicant-node', 1, NULL, 'PASS', NULL,
       '120,200;180,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @audit_definition_id AND now_node_code = 'start-node' AND next_node_code = 'applicant-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002102, @audit_definition_id, 'applicant-node', 1, 'first-review-node', 1, NULL, 'PASS', NULL,
       '300,200;360,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @audit_definition_id AND now_node_code = 'applicant-node' AND next_node_code = 'first-review-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002103, @audit_definition_id, 'first-review-node', 1, 'second-review-node', 1, NULL, 'PASS', NULL,
       '480,200;540,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @audit_definition_id AND now_node_code = 'first-review-node' AND next_node_code = 'second-review-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002104, @audit_definition_id, 'second-review-node', 1, 'final-review-node', 1, NULL, 'PASS', NULL,
       '660,200;720,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @audit_definition_id AND now_node_code = 'second-review-node' AND next_node_code = 'final-review-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002105, @audit_definition_id, 'final-review-node', 1, 'end-node', 2, NULL, 'PASS', NULL,
       '840,200;900,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @audit_definition_id AND now_node_code = 'final-review-node' AND next_node_code = 'end-node' AND del_flag = '0');

UPDATE flow_skip
SET now_node_code = 'applicant-node',
    now_node_type = 1,
    next_node_code = 'certified-route-node',
    next_node_type = 3,
    skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = NULL,
    coordinate = '300,200;360,200',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @audit_definition_id
  AND (
        (now_node_code = 'applicant-node' AND next_node_code = 'first-review-node')
        OR (now_node_code = 'applicant-node' AND next_node_code = 'certified-route-node')
      );

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002106, @audit_definition_id, 'certified-route-node', 3, 'first-review-node', 1, NULL, 'PASS',
       'ne@@isCertified|true', '450,160;510,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @audit_definition_id
      AND now_node_code = 'certified-route-node'
      AND next_node_code = 'first-review-node'
      AND del_flag = '0'
);

UPDATE flow_skip
SET now_node_code = 'certified-route-node',
    now_node_type = 3,
    next_node_code = 'first-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = 'ne@@isCertified|true',
    coordinate = '450,160;510,160',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @audit_definition_id
  AND now_node_code = 'certified-route-node'
  AND next_node_code = 'first-review-node';

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002107, @audit_definition_id, 'certified-route-node', 3, 'second-review-node', 1, NULL, 'PASS',
       'eq@@isCertified|true', '450,240;570,240', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @audit_definition_id
      AND now_node_code = 'certified-route-node'
      AND next_node_code = 'second-review-node'
      AND del_flag = '0'
);

UPDATE flow_skip
SET now_node_code = 'certified-route-node',
    now_node_type = 3,
    next_node_code = 'second-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = 'eq@@isCertified|true',
    coordinate = '450,240;570,240',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @audit_definition_id
  AND now_node_code = 'certified-route-node'
  AND next_node_code = 'second-review-node';

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002108, @audit_definition_id, 'first-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
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
SELECT 2026032500000002109, @audit_definition_id, 'second-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
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
SELECT 2026032500000002110, @audit_definition_id, 'final-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
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

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001201, 0, @proof_definition_id, 'start-node', '开始', NULL, '0.000', '100,200|100,200',
       NULL, NULL, NULL, 'N', NULL, '1', NOW(), @operator, NOW(), @operator, NULL, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'start-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001202, 1, @proof_definition_id, 'applicant-node', '审校申请', '', '0.000', '240,200|240,200',
       NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'applicant-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001203, 1, @proof_definition_id, 'first-review-node', '一级审批', '${manuscriptReviewFirstLevelApprover}',
       '0.000', '420,200|420,200', NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,transfer,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'first-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001204, 1, @proof_definition_id, 'second-review-node', '二级审批', '${manuscriptReviewSecondLevelApprover}',
       '0.000', '600,200|600,200', NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,transfer,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'second-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001205, 1, @proof_definition_id, 'final-review-node', '三级审批', '${manuscriptReviewThirdLevelApprover}',
       '0.000', '780,200|780,200', NULL, NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination,transfer,file\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'final-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001206, 2, @proof_definition_id, 'end-node', '结束', NULL, '0.000', '960,200|960,200',
       NULL, NULL, NULL, 'N', NULL, '1', NOW(), @operator, NOW(), @operator, NULL, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'end-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001207, 3, @proof_definition_id, 'certified-route-node', 'Certified Route', NULL, '0.000',
       '420,200|420,200', NULL, NULL, NULL, 'N', NULL, '1', NOW(), @operator, NOW(), @operator, '[]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'certified-route-node' AND del_flag = '0');

UPDATE flow_node
SET node_type = 3,
    node_name = 'Certified Route',
    permission_flag = NULL,
    node_ratio = '0.000',
    coordinate = '420,200|420,200',
    form_custom = 'N',
    form_path = NULL,
    ext = '[]',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @proof_definition_id
  AND node_code = 'certified-route-node';

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002201, @proof_definition_id, 'start-node', 0, 'applicant-node', 1, NULL, 'PASS', NULL,
       '120,200;180,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @proof_definition_id AND now_node_code = 'start-node' AND next_node_code = 'applicant-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002202, @proof_definition_id, 'applicant-node', 1, 'first-review-node', 1, NULL, 'PASS', NULL,
       '300,200;360,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @proof_definition_id AND now_node_code = 'applicant-node' AND next_node_code = 'first-review-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002203, @proof_definition_id, 'first-review-node', 1, 'second-review-node', 1, NULL, 'PASS', NULL,
       '480,200;540,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @proof_definition_id AND now_node_code = 'first-review-node' AND next_node_code = 'second-review-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002204, @proof_definition_id, 'second-review-node', 1, 'final-review-node', 1, NULL, 'PASS', NULL,
       '660,200;720,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @proof_definition_id AND now_node_code = 'second-review-node' AND next_node_code = 'final-review-node' AND del_flag = '0');

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002205, @proof_definition_id, 'final-review-node', 1, 'end-node', 2, NULL, 'PASS', NULL,
       '840,200;900,200', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_skip WHERE definition_id = @proof_definition_id AND now_node_code = 'final-review-node' AND next_node_code = 'end-node' AND del_flag = '0');

UPDATE flow_skip
SET now_node_code = 'applicant-node',
    now_node_type = 1,
    next_node_code = 'certified-route-node',
    next_node_type = 3,
    skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = NULL,
    coordinate = '300,200;360,200',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @proof_definition_id
  AND (
        (now_node_code = 'applicant-node' AND next_node_code = 'first-review-node')
        OR (now_node_code = 'applicant-node' AND next_node_code = 'certified-route-node')
      );

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002206, @proof_definition_id, 'certified-route-node', 3, 'first-review-node', 1, NULL, 'PASS',
       'ne@@isCertified|true', '450,160;510,160', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @proof_definition_id
      AND now_node_code = 'certified-route-node'
      AND next_node_code = 'first-review-node'
      AND del_flag = '0'
);

UPDATE flow_skip
SET now_node_code = 'certified-route-node',
    now_node_type = 3,
    next_node_code = 'first-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = 'ne@@isCertified|true',
    coordinate = '450,160;510,160',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @proof_definition_id
  AND now_node_code = 'certified-route-node'
  AND next_node_code = 'first-review-node';

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002207, @proof_definition_id, 'certified-route-node', 3, 'second-review-node', 1, NULL, 'PASS',
       'eq@@isCertified|true', '450,240;570,240', NOW(), @operator, NOW(), @operator, '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM flow_skip
    WHERE definition_id = @proof_definition_id
      AND now_node_code = 'certified-route-node'
      AND next_node_code = 'second-review-node'
      AND del_flag = '0'
);

UPDATE flow_skip
SET now_node_code = 'certified-route-node',
    now_node_type = 3,
    next_node_code = 'second-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = 'eq@@isCertified|true',
    coordinate = '450,240;570,240',
    update_time = NOW(),
    update_by = @operator,
    del_flag = '0',
    tenant_id = @tenant_id
WHERE definition_id = @proof_definition_id
  AND now_node_code = 'certified-route-node'
  AND next_node_code = 'second-review-node';

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT 2026032500000002208, @proof_definition_id, 'first-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
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
SELECT 2026032500000002209, @proof_definition_id, 'second-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
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
SELECT 2026032500000002210, @proof_definition_id, 'final-review-node', 1, 'applicant-node', 1, NULL, 'REJECT', NULL,
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

INSERT INTO brain_manuscript_review_flow_config
(`id`, `tenant_id`, `process_type`, `flow_code`, `level_one_role_key`, `level_two_role_key`, `level_three_role_key`,
 `status`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2026032500000003101, @tenant_id, 'AUDIT', @audit_flow_code,
       'manuscript_review_level_1_approver', 'manuscript_review_level_2_approver', 'manuscript_review_level_3_approver',
       '0', NULL, 1, NOW(), 1, NOW(), 'manuscript_review workflow seed v01'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM brain_manuscript_review_flow_config
    WHERE tenant_id = @tenant_id
      AND process_type = 'AUDIT'
      AND status = '0'
);

INSERT INTO brain_manuscript_review_flow_config
(`id`, `tenant_id`, `process_type`, `flow_code`, `level_one_role_key`, `level_two_role_key`, `level_three_role_key`,
 `status`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2026032500000003102, @tenant_id, 'PROOFREAD', @proof_flow_code,
       'manuscript_review_level_1_approver', 'manuscript_review_level_2_approver', 'manuscript_review_level_3_approver',
       '0', NULL, 1, NOW(), 1, NOW(), 'manuscript_review workflow seed v01'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM brain_manuscript_review_flow_config
    WHERE tenant_id = @tenant_id
      AND process_type = 'PROOFREAD'
      AND status = '0'
);
