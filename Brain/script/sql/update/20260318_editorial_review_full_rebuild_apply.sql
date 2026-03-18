-- editorial review full rebuild for tenant 099142
-- target:
--   tenant_id = 099142
--   flow_code = editorial_review_flow
--   form_path = /editorial/review/detail
--   reviewEdit = fallback only

SET @now_time := NOW();
SET @target_tenant := '099142';
SET @target_flow_code := 'editorial_review_flow';
SET @target_form_path := '/editorial/review/detail';
SET @flow_name := '审校申请流程';
SET @flow_category := COALESCE(
    (
        SELECT `category`
        FROM `flow_definition`
        WHERE `tenant_id` = @target_tenant
          AND `flow_code` = @target_flow_code
        ORDER BY `id` DESC
        LIMIT 1
    ),
    '2022474944735449089'
);
SET @flow_version := COALESCE(
    (
        SELECT CAST(MAX(CAST(`version` AS UNSIGNED)) + 1 AS CHAR)
        FROM `flow_definition`
        WHERE `tenant_id` = @target_tenant
          AND `flow_code` = @target_flow_code
          AND `version` REGEXP '^[0-9]+$'
    ),
    '1'
);
SET @flow_operator := COALESCE(
    (
        SELECT `update_by`
        FROM `flow_definition`
        WHERE `tenant_id` = @target_tenant
          AND `flow_code` = @target_flow_code
        ORDER BY `id` DESC
        LIMIT 1
    ),
    '1'
);

SET @parent_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `path` = 'editorial/review'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @review_edit_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `path` = 'reviewEdit'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @detail_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `path` = 'detail'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @form_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `path` = 'form'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @query_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `perms` = 'editorial:review:query'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @add_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `perms` = 'editorial:review:add'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @edit_button_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `menu_type` = 'F'
      AND `perms` = 'editorial:review:edit'
    ORDER BY `menu_id`
    LIMIT 1
);
SET @remove_menu_id := (
    SELECT `menu_id`
    FROM `sys_menu`
    WHERE `parent_id` = @parent_menu_id
      AND `perms` = 'editorial:review:remove'
    ORDER BY `menu_id`
    LIMIT 1
);

SET @menu_seed := (SELECT IFNULL(MAX(`menu_id`), 0) FROM `sys_menu`);
SET @form_menu_id := IFNULL(@form_menu_id, @menu_seed := @menu_seed + 1);
SET @detail_menu_id := IFNULL(@detail_menu_id, @menu_seed := @menu_seed + 1);
SET @review_edit_menu_id := IFNULL(@review_edit_menu_id, @menu_seed := @menu_seed + 1);

SET @admin_role_id := (SELECT `role_id` FROM `sys_role` WHERE `role_key` = 'admin' LIMIT 1);
SET @applicant_role_id := (SELECT `role_id` FROM `sys_role` WHERE `role_key` = 'editorial_review_applicant' LIMIT 1);
SET @applicant_cert_role_id := (SELECT `role_id` FROM `sys_role` WHERE `role_key` = 'editorial_review_applicant_has_certificate' LIMIT 1);
SET @first_role_id := (SELECT `role_id` FROM `sys_role` WHERE `role_key` = 'editorial_first_level_approver' LIMIT 1);
SET @second_role_id := (SELECT `role_id` FROM `sys_role` WHERE `role_key` = 'editorial_second_level_approver' LIMIT 1);
SET @third_role_id := (SELECT `role_id` FROM `sys_role` WHERE `role_key` = 'editorial_third_level_approver' LIMIT 1);

DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_definition_ids`;
CREATE TEMPORARY TABLE `tmp_editorial_definition_ids`
(
    `id` bigint PRIMARY KEY
)
AS
SELECT `id`
FROM `flow_definition`
WHERE `tenant_id` = @target_tenant
  AND `flow_code` = @target_flow_code;

DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_business_ids`;
CREATE TEMPORARY TABLE `tmp_editorial_business_ids`
(
    `business_id` varchar(40) PRIMARY KEY
)
AS
SELECT CAST(`id` AS CHAR(40)) AS `business_id`
FROM `brain_editorial_review`;

DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_instance_ids`;
CREATE TEMPORARY TABLE `tmp_editorial_instance_ids`
(
    `id` bigint PRIMARY KEY
)
AS
SELECT `id`
FROM `flow_instance`
WHERE `tenant_id` = @target_tenant
  AND (
      `definition_id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`)
      OR `business_id` IN (SELECT `business_id` FROM `tmp_editorial_business_ids`)
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_role_6`;
CREATE TEMPORARY TABLE `tmp_editorial_role_6`
(
    `role_id` bigint PRIMARY KEY
);
INSERT INTO `tmp_editorial_role_6` (`role_id`)
SELECT `role_id`
FROM `sys_role`
WHERE `role_key` IN (
    'admin',
    'editorial_review_applicant',
    'editorial_review_applicant_has_certificate',
    'editorial_first_level_approver',
    'editorial_second_level_approver',
    'editorial_third_level_approver'
);

DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_role_3`;
CREATE TEMPORARY TABLE `tmp_editorial_role_3`
(
    `role_id` bigint PRIMARY KEY
);
INSERT INTO `tmp_editorial_role_3` (`role_id`)
SELECT `role_id`
FROM `sys_role`
WHERE `role_key` IN (
    'admin',
    'editorial_review_applicant',
    'editorial_review_applicant_has_certificate'
);

ALTER TABLE `brain_editorial_review`
    ADD COLUMN IF NOT EXISTS `review_status` tinyint NOT NULL DEFAULT 0 COMMENT '0草稿 10待一级 20待二级 30待终审 40通过 50退回 60终止 70撤销' AFTER `status`;

ALTER TABLE `brain_editorial_review`
    ADD COLUMN IF NOT EXISTS `process_type` varchar(32) NOT NULL DEFAULT 'AUDIT' COMMENT '流程类型：AUDIT/PROOFREAD' AFTER `review_status`;

SET @idx_review_status_exists := (
    SELECT COUNT(1)
    FROM `information_schema`.`statistics`
    WHERE `table_schema` = DATABASE()
      AND `table_name` = 'brain_editorial_review'
      AND `index_name` = 'idx_brain_editorial_review_review_status'
);
SET @idx_review_status_sql := IF(
    @idx_review_status_exists = 0,
    'CREATE INDEX idx_brain_editorial_review_review_status ON brain_editorial_review (review_status)',
    'SELECT 1'
);
PREPARE `stmt_review_status` FROM @idx_review_status_sql;
EXECUTE `stmt_review_status`;
DEALLOCATE PREPARE `stmt_review_status`;

SET @idx_process_type_exists := (
    SELECT COUNT(1)
    FROM `information_schema`.`statistics`
    WHERE `table_schema` = DATABASE()
      AND `table_name` = 'brain_editorial_review'
      AND `index_name` = 'idx_brain_editorial_review_process_type'
);
SET @idx_process_type_sql := IF(
    @idx_process_type_exists = 0,
    'CREATE INDEX idx_brain_editorial_review_process_type ON brain_editorial_review (process_type)',
    'SELECT 1'
);
PREPARE `stmt_process_type` FROM @idx_process_type_sql;
EXECUTE `stmt_process_type`;
DEALLOCATE PREPARE `stmt_process_type`;

DELETE FROM `flow_instance_biz_ext`
WHERE `instance_id` IN (SELECT `id` FROM `tmp_editorial_instance_ids`)
   OR `business_id` IN (SELECT `business_id` FROM `tmp_editorial_business_ids`);

DELETE FROM `flow_his_task`
WHERE `instance_id` IN (SELECT `id` FROM `tmp_editorial_instance_ids`)
   OR `definition_id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`);

DELETE FROM `flow_task`
WHERE `instance_id` IN (SELECT `id` FROM `tmp_editorial_instance_ids`)
   OR `definition_id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`);

DELETE FROM `flow_instance`
WHERE `id` IN (SELECT `id` FROM `tmp_editorial_instance_ids`)
   OR `definition_id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`)
   OR `business_id` IN (SELECT `business_id` FROM `tmp_editorial_business_ids`);

DELETE FROM `flow_skip`
WHERE `definition_id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`);

DELETE FROM `flow_node`
WHERE `definition_id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`);

DELETE FROM `flow_definition`
WHERE `id` IN (SELECT `id` FROM `tmp_editorial_definition_ids`);

DELETE FROM `brain_editorial_history`;
DELETE FROM `brain_editorial_attachment`;
DELETE FROM `brain_editorial_link`;
DELETE FROM `brain_editorial_review`;

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`,
 `menu_type`, `visible`, `status`, `perms`, `icon`, `create_dept`, `create_by`, `create_time`, `update_by`,
 `update_time`, `remark`)
SELECT @review_edit_menu_id, '审校旧入口兜底', @parent_menu_id, 99, 'reviewEdit', 'editorial/review/reviewEdit', NULL,
       1, 1, 'C', '1', '0', 'editorial:review:edit', '#', 100, 1, @now_time, 1, @now_time,
       'fallback only, not primary entry'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = @review_edit_menu_id
);

UPDATE `sys_menu`
SET `menu_name` = '审校旧入口兜底',
    `parent_id` = @parent_menu_id,
    `order_num` = 99,
    `path` = 'reviewEdit',
    `component` = 'editorial/review/reviewEdit',
    `query_param` = NULL,
    `is_frame` = 1,
    `is_cache` = 1,
    `menu_type` = 'C',
    `visible` = '1',
    `status` = '0',
    `perms` = 'editorial:review:edit',
    `icon` = '#',
    `update_by` = 1,
    `update_time` = @now_time,
    `remark` = 'fallback only, not primary entry'
WHERE `menu_id` = @review_edit_menu_id;

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`,
 `menu_type`, `visible`, `status`, `perms`, `icon`, `create_dept`, `create_by`, `create_time`, `update_by`,
 `update_time`, `remark`)
SELECT @form_menu_id, '审校申请表单', @parent_menu_id, 2, 'form', 'editorial/review/form', NULL,
       1, 1, 'C', '1', '0', 'editorial:review:add', '#', 100, 1, @now_time, 1, @now_time,
       'create and edit draft entry'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = @form_menu_id
);

UPDATE `sys_menu`
SET `menu_name` = '审校申请表单',
    `parent_id` = @parent_menu_id,
    `order_num` = 2,
    `path` = 'form',
    `component` = 'editorial/review/form',
    `query_param` = NULL,
    `is_frame` = 1,
    `is_cache` = 1,
    `menu_type` = 'C',
    `visible` = '1',
    `status` = '0',
    `perms` = 'editorial:review:add',
    `icon` = '#',
    `update_by` = 1,
    `update_time` = @now_time,
    `remark` = 'draft create/edit entry'
WHERE `menu_id` = @form_menu_id;

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`,
 `menu_type`, `visible`, `status`, `perms`, `icon`, `create_dept`, `create_by`, `create_time`, `update_by`,
 `update_time`, `remark`)
SELECT @detail_menu_id, '审校申请详情', @parent_menu_id, 3, 'detail', 'editorial/review/detail', NULL,
       1, 1, 'C', '1', '0', 'editorial:review:query', '#', 100, 1, @now_time, 1, @now_time,
       'detail and approval shell entry'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = @detail_menu_id
);

UPDATE `sys_menu`
SET `menu_name` = '审校申请详情',
    `parent_id` = @parent_menu_id,
    `order_num` = 3,
    `path` = 'detail',
    `component` = 'editorial/review/detail',
    `query_param` = NULL,
    `is_frame` = 1,
    `is_cache` = 1,
    `menu_type` = 'C',
    `visible` = '1',
    `status` = '0',
    `perms` = 'editorial:review:query',
    `icon` = '#',
    `update_by` = 1,
    `update_time` = @now_time,
    `remark` = 'detail and approval shell entry'
WHERE `menu_id` = @detail_menu_id;

DELETE FROM `sys_role_menu`
WHERE `menu_id` IN (
    @parent_menu_id,
    @review_edit_menu_id,
    @detail_menu_id,
    @form_menu_id,
    @query_menu_id,
    @add_menu_id,
    @edit_button_menu_id,
    @remove_menu_id
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @parent_menu_id
FROM `tmp_editorial_role_6`
WHERE @parent_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @review_edit_menu_id
FROM `tmp_editorial_role_6`
WHERE @review_edit_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @detail_menu_id
FROM `tmp_editorial_role_6`
WHERE @detail_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @query_menu_id
FROM `tmp_editorial_role_6`
WHERE @query_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @edit_button_menu_id
FROM `tmp_editorial_role_6`
WHERE @edit_button_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @form_menu_id
FROM `tmp_editorial_role_3`
WHERE @form_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @add_menu_id
FROM `tmp_editorial_role_3`
WHERE @add_menu_id IS NOT NULL;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, @remove_menu_id
FROM `tmp_editorial_role_3`
WHERE @remove_menu_id IS NOT NULL;

SET @definition_seed := (SELECT IFNULL(MAX(`id`), 0) FROM `flow_definition`);
SET @new_definition_id := @definition_seed + 1;

INSERT INTO `flow_definition`
(`id`, `flow_code`, `flow_name`, `model_value`, `category`, `version`, `is_publish`, `form_custom`, `form_path`,
 `activity_status`, `listener_type`, `listener_path`, `ext`, `create_time`, `create_by`, `update_time`, `update_by`,
 `del_flag`, `tenant_id`)
VALUES
(@new_definition_id, @target_flow_code, @flow_name, 'CLASSICS', @flow_category, @flow_version, 1, 'N', @target_form_path,
 1, NULL, NULL, '{"autoPass":false}', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant);

SET @node_seed := (SELECT IFNULL(MAX(`id`), 0) FROM `flow_node`);
SET @start_node_id := @node_seed + 1;
SET @applicant_node_id := @node_seed + 2;
SET @first_node_id := @node_seed + 3;
SET @second_node_id := @node_seed + 4;
SET @final_node_id := @node_seed + 5;
SET @end_node_id := @node_seed + 6;

INSERT INTO `flow_node`
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
VALUES
(@start_node_id, 0, @new_definition_id, 'start-node', '开始', NULL, '0.000', '100,200|100,200',
 NULL, NULL, NULL, 'N', NULL, @flow_version, @now_time, @flow_operator, @now_time, @flow_operator, NULL, '0', @target_tenant),
(@applicant_node_id, 1, @new_definition_id, 'applicant-node', '审校申请', '', '0.000', '240,200|240,200',
 NULL, NULL, NULL, 'N', @target_form_path, @flow_version, @now_time, @flow_operator, @now_time, @flow_operator,
 '[{"code":"ButtonPermissionEnum","value":"back,termination,file"}]', '0', @target_tenant),
(@first_node_id, 1, @new_definition_id, 'first-review-node', '一级审批', CONCAT('role:', @first_role_id), '0.000', '420,160|420,160',
 NULL, NULL, NULL, 'N', @target_form_path, @flow_version, @now_time, @flow_operator, @now_time, @flow_operator,
 '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]', '0', @target_tenant),
(@second_node_id, 1, @new_definition_id, 'second-review-node', '二级审批', CONCAT('role:', @second_role_id), '0.000', '600,200|600,200',
 NULL, NULL, NULL, 'N', @target_form_path, @flow_version, @now_time, @flow_operator, @now_time, @flow_operator,
 '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]', '0', @target_tenant),
(@final_node_id, 1, @new_definition_id, 'final-review-node', '终审', CONCAT('role:', @third_role_id), '0.000', '780,200|780,200',
 NULL, NULL, NULL, 'N', @target_form_path, @flow_version, @now_time, @flow_operator, @now_time, @flow_operator,
 '[{"code":"ButtonPermissionEnum","value":"back,termination,transfer,file"}]', '0', @target_tenant),
(@end_node_id, 2, @new_definition_id, 'end-node', '结束', NULL, '0.000', '960,200|960,200',
 NULL, NULL, NULL, 'N', NULL, @flow_version, @now_time, @flow_operator, @now_time, @flow_operator, NULL, '0', @target_tenant);

SET @skip_seed := (SELECT IFNULL(MAX(`id`), 0) FROM `flow_skip`);

INSERT INTO `flow_skip`
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
VALUES
(@skip_seed + 1, @new_definition_id, 'start-node', 0, 'applicant-node', 1, NULL, 'PASS',
 NULL, '120,200;180,200', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant),
(@skip_seed + 2, @new_definition_id, 'applicant-node', 1, 'first-review-node', 1, '普通申请走一级', 'PASS',
 'ne@@isCertified|true', '300,160;360,160', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant),
(@skip_seed + 3, @new_definition_id, 'applicant-node', 1, 'second-review-node', 1, '持证申请跳一级', 'PASS',
 'eq@@isCertified|true', '300,240;540,240', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant),
(@skip_seed + 4, @new_definition_id, 'first-review-node', 1, 'second-review-node', 1, NULL, 'PASS',
 NULL, '480,160;540,160', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant),
(@skip_seed + 5, @new_definition_id, 'second-review-node', 1, 'final-review-node', 1, NULL, 'PASS',
 NULL, '660,200;720,200', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant),
(@skip_seed + 6, @new_definition_id, 'final-review-node', 1, 'end-node', 2, NULL, 'PASS',
 NULL, '840,200;900,200', @now_time, @flow_operator, @now_time, @flow_operator, '0', @target_tenant);

DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_instance_ids`;
DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_business_ids`;
DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_definition_ids`;
DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_role_3`;
DROP TEMPORARY TABLE IF EXISTS `tmp_editorial_role_6`;
