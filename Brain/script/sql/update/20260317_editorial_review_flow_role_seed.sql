-- editorial review rebuild: flow and role alignment

SET @now_time := NOW();

SET @role_seed_base := (SELECT IFNULL(MAX(role_id), 0) FROM `sys_role`);

INSERT INTO `sys_role`
(`role_id`, `tenant_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`,
 `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT @role_seed_base := @role_seed_base + 1, '000000', '审校一级审批', 'editorial_first_level_approver', 171, '1', 1, 1,
       '0', '0', 103, 1, @now_time, 1, @now_time, '审校流程一级审批角色'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'editorial_first_level_approver'
);

INSERT INTO `sys_role`
(`role_id`, `tenant_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`,
 `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT @role_seed_base := @role_seed_base + 1, '000000', '审校二级审批', 'editorial_second_level_approver', 172, '1', 1, 1,
       '0', '0', 103, 1, @now_time, 1, @now_time, '审校流程二级审批角色'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'editorial_second_level_approver'
);

INSERT INTO `sys_role`
(`role_id`, `tenant_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`,
 `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT @role_seed_base := @role_seed_base + 1, '000000', '审校终审', 'editorial_third_level_approver', 173, '1', 1, 1,
       '0', '0', 103, 1, @now_time, 1, @now_time, '审校流程终审角色'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'editorial_third_level_approver'
);

UPDATE `sys_role`
SET `role_name` = '审校一级审批',
    `role_sort` = 171,
    `data_scope` = '1',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `update_by` = 1,
    `update_time` = @now_time,
    `remark` = '审校流程一级审批角色'
WHERE `role_key` = 'editorial_first_level_approver';

UPDATE `sys_role`
SET `role_name` = '审校二级审批',
    `role_sort` = 172,
    `data_scope` = '1',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `update_by` = 1,
    `update_time` = @now_time,
    `remark` = '审校流程二级审批角色'
WHERE `role_key` = 'editorial_second_level_approver';

UPDATE `sys_role`
SET `role_name` = '审校终审',
    `role_sort` = 173,
    `data_scope` = '1',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `update_by` = 1,
    `update_time` = @now_time,
    `remark` = '审校流程终审角色'
WHERE `role_key` = 'editorial_third_level_approver';

SET @definition_seed_base := (SELECT IFNULL(MAX(id), 0) FROM `flow_definition`);
INSERT INTO `flow_definition`
(`id`, `flow_code`, `flow_name`, `model_value`, `category`, `version`, `is_publish`, `form_custom`, `form_path`,
 `activity_status`, `listener_type`, `listener_path`, `ext`, `create_time`, `create_by`, `update_time`, `update_by`,
 `del_flag`, `tenant_id`)
SELECT @definition_seed_base := @definition_seed_base + 1, 'editorial_review_flow', '审校申请流程', 'CLASSICS', 'editorial',
       '1', 1, 'N', '/editorial/review/detail', 1, NULL, NULL, NULL, @now_time, '1', @now_time, '1', '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_definition` WHERE `flow_code` = 'editorial_review_flow' AND `del_flag` = '0'
);

SET @editorial_definition_id := (
    SELECT `id`
    FROM `flow_definition`
    WHERE `flow_code` = 'editorial_review_flow' AND `del_flag` = '0'
    ORDER BY `id` DESC
    LIMIT 1
);

UPDATE `flow_definition`
SET `flow_name` = '审校申请流程',
    `model_value` = 'CLASSICS',
    `category` = 'editorial',
    `version` = '1',
    `is_publish` = 1,
    `form_custom` = 'N',
    `form_path` = '/editorial/review/detail',
    `activity_status` = 1,
    `update_time` = @now_time,
    `update_by` = '1',
    `del_flag` = '0',
    `tenant_id` = COALESCE(`tenant_id`, '000000')
WHERE `id` = @editorial_definition_id;

SET @legacy_first_node_code := (
    SELECT `node_code`
    FROM `flow_node`
    WHERE `definition_id` = @editorial_definition_id
      AND `node_name` IN ('部门经理审批', '一级审批')
    ORDER BY `id`
    LIMIT 1
);
SET @legacy_second_node_code := (
    SELECT `node_code`
    FROM `flow_node`
    WHERE `definition_id` = @editorial_definition_id
      AND `node_name` IN ('总编室审批', '二级审批')
    ORDER BY `id`
    LIMIT 1
);
SET @legacy_final_node_code := (
    SELECT `node_code`
    FROM `flow_node`
    WHERE `definition_id` = @editorial_definition_id
      AND `node_name` IN ('终审', '社领导审批')
    ORDER BY `id`
    LIMIT 1
);

UPDATE `flow_node`
SET `node_code` = 'first-review-node'
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = @legacy_first_node_code
  AND @legacy_first_node_code IS NOT NULL;

UPDATE `flow_node`
SET `node_code` = 'second-review-node'
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = @legacy_second_node_code
  AND @legacy_second_node_code IS NOT NULL;

UPDATE `flow_node`
SET `node_code` = 'final-review-node'
WHERE `definition_id` = @editorial_definition_id
  AND `node_code` = @legacy_final_node_code
  AND @legacy_final_node_code IS NOT NULL;

SET @node_seed_base := (SELECT IFNULL(MAX(id), 0) FROM `flow_node`);

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @node_seed_base := @node_seed_base + 1, 0, @editorial_definition_id, 'start-node', '开始', NULL, '0.000',
       '100,200|100,200', NULL, NULL, NULL, 'N', NULL, '1', @now_time, '1', @now_time, '1', NULL, '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_node` WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'start-node'
);

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @node_seed_base := @node_seed_base + 1, 1, @editorial_definition_id, 'applicant-node', '审校申请', '',
       '0.000', '240,200|240,200', NULL, NULL, NULL, 'N', '/editorial/review/detail', '1', @now_time, '1',
       @now_time, '1', '[{"code":"ButtonPermissionEnum","value":"back,termination,file"}]', '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_node` WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'applicant-node'
);

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @node_seed_base := @node_seed_base + 1, 1, @editorial_definition_id, 'first-review-node', '一级审批',
       '${editorialFirstLevelApprover}', '0.000', '420,160|420,160', NULL, NULL, NULL, 'N',
       '/editorial/review/detail', '1', @now_time, '1', @now_time, '1',
       '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]', '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_node` WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'first-review-node'
);

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @node_seed_base := @node_seed_base + 1, 1, @editorial_definition_id, 'second-review-node', '二级审批',
       '${editorialSecondLevelApprover}', '0.000', '600,200|600,200', NULL, NULL, NULL, 'N',
       '/editorial/review/detail', '1', @now_time, '1', @now_time, '1',
       '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]', '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_node` WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'second-review-node'
);

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @node_seed_base := @node_seed_base + 1, 1, @editorial_definition_id, 'final-review-node', '终审',
       '${editorialThirdLevelApprover}', '0.000', '780,200|780,200', NULL, NULL, NULL, 'N',
       '/editorial/review/detail', '1', @now_time, '1', @now_time, '1',
       '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]', '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_node` WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'final-review-node'
);

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @node_seed_base := @node_seed_base + 1, 2, @editorial_definition_id, 'end-node', '结束', NULL, '0.000',
       '960,200|960,200', NULL, NULL, NULL, 'N', NULL, '1', @now_time, '1', @now_time, '1', NULL, '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `flow_node` WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'end-node'
);

UPDATE `flow_node`
SET `node_name` = '开始',
    `permission_flag` = NULL,
    `node_ratio` = '0.000',
    `coordinate` = '100,200|100,200',
    `form_custom` = 'N',
    `form_path` = NULL,
    `version` = '1',
    `update_time` = @now_time,
    `update_by` = '1',
    `del_flag` = '0'
WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'start-node';

UPDATE `flow_node`
SET `node_name` = '审校申请',
    `permission_flag` = '',
    `node_ratio` = '0.000',
    `coordinate` = '240,200|240,200',
    `form_custom` = 'N',
    `form_path` = '/editorial/review/detail',
    `version` = '1',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,file"}]',
    `del_flag` = '0'
WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'applicant-node';

UPDATE `flow_node`
SET `node_name` = '一级审批',
    `permission_flag` = '${editorialFirstLevelApprover}',
    `node_ratio` = '0.000',
    `coordinate` = '420,160|420,160',
    `form_custom` = 'N',
    `form_path` = '/editorial/review/detail',
    `version` = '1',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]',
    `del_flag` = '0'
WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'first-review-node';

UPDATE `flow_node`
SET `node_name` = '二级审批',
    `permission_flag` = '${editorialSecondLevelApprover}',
    `node_ratio` = '0.000',
    `coordinate` = '600,200|600,200',
    `form_custom` = 'N',
    `form_path` = '/editorial/review/detail',
    `version` = '1',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]',
    `del_flag` = '0'
WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'second-review-node';

UPDATE `flow_node`
SET `node_name` = '终审',
    `permission_flag` = '${editorialThirdLevelApprover}',
    `node_ratio` = '0.000',
    `coordinate` = '780,200|780,200',
    `form_custom` = 'N',
    `form_path` = '/editorial/review/detail',
    `version` = '1',
    `update_time` = @now_time,
    `update_by` = '1',
    `ext` = '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]',
    `del_flag` = '0'
WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'final-review-node';

UPDATE `flow_node`
SET `node_name` = '结束',
    `permission_flag` = NULL,
    `node_ratio` = '0.000',
    `coordinate` = '960,200|960,200',
    `form_custom` = 'N',
    `form_path` = NULL,
    `version` = '1',
    `update_time` = @now_time,
    `update_by` = '1',
    `del_flag` = '0'
WHERE `definition_id` = @editorial_definition_id AND `node_code` = 'end-node';

DELETE FROM `flow_skip`
WHERE `definition_id` = @editorial_definition_id
  AND `now_node_code` <> 'start-node';

SET @skip_seed_base := (SELECT IFNULL(MAX(id), 0) FROM `flow_skip`);

INSERT INTO `flow_skip`
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`,
 `skip_type`, `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`,
 `tenant_id`)
SELECT @skip_seed_base := @skip_seed_base + 1, @editorial_definition_id, 'start-node', 0, 'applicant-node', 1, NULL,
       'PASS', NULL, '120,200;180,200', @now_time, '1', @now_time, '1', '0', '000000'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM `flow_skip`
    WHERE `definition_id` = @editorial_definition_id
      AND `now_node_code` = 'start-node'
      AND `next_node_code` = 'applicant-node'
);

INSERT INTO `flow_skip`
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`,
 `skip_type`, `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`,
 `tenant_id`)
VALUES
(@skip_seed_base := @skip_seed_base + 1, @editorial_definition_id, 'applicant-node', 1, 'first-review-node', 1, NULL,
 'PASS', 'default@@${!isCertified}', '300,160;360,160', @now_time, '1', @now_time, '1', '0', '000000'),
(@skip_seed_base := @skip_seed_base + 1, @editorial_definition_id, 'applicant-node', 1, 'second-review-node', 1, NULL,
 'PASS', 'default@@${isCertified}', '300,240;540,240', @now_time, '1', @now_time, '1', '0', '000000'),
(@skip_seed_base := @skip_seed_base + 1, @editorial_definition_id, 'first-review-node', 1, 'second-review-node', 1,
 NULL, 'PASS', NULL, '480,160;540,160', @now_time, '1', @now_time, '1', '0', '000000'),
(@skip_seed_base := @skip_seed_base + 1, @editorial_definition_id, 'second-review-node', 1, 'final-review-node', 1,
 NULL, 'PASS', NULL, '660,200;720,200', @now_time, '1', @now_time, '1', '0', '000000'),
(@skip_seed_base := @skip_seed_base + 1, @editorial_definition_id, 'final-review-node', 1, 'end-node', 2, NULL,
 'PASS', NULL, '840,200;900,200', @now_time, '1', @now_time, '1', '0', '000000');
