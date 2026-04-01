/*
Owner: Codex
Task: manuscript_review release dml
Status: Draft
Updated: 2026-04-01

Scope:
- dictionaries
- menus
- roles
- role-menu bindings
- workflow definitions / nodes / skips
- flow config

Explicitly excluded:
- dev/test users
- readable fixture seed
- browser regression seed
*/

SET NAMES utf8mb4;

/* -------------------------------------------------------------------------- */
/* 1) 字典                                                                     */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_dict_type (
  dict_id, tenant_id, dict_name, dict_type,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (61000, '000000', '审校-流程类型', 'manuscript_review_process_type', NULL, 1, NOW(), 1, NOW(), 'manuscript_review release'),
  (61001, '000000', '审校-流程状态', 'manuscript_review_flow_status', NULL, 1, NOW(), 1, NOW(), 'manuscript_review release'),
  (61002, '000000', '审校-当前节点', 'manuscript_review_current_node', NULL, 1, NOW(), 1, NOW(), 'manuscript_review release'),
  (61003, '000000', '审校-媒体栏目预设', 'manuscript_review_media_channel', NULL, 1, NOW(), 1, NOW(), 'manuscript_review release')
ON DUPLICATE KEY UPDATE
  dict_name = VALUES(dict_name),
  dict_type = VALUES(dict_type),
  update_by = VALUES(update_by),
  update_time = VALUES(update_time),
  remark = VALUES(remark);

INSERT INTO sys_dict_data (
  dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
  css_class, list_class, is_default,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (62000, '000000', 1, '审核流程', 'AUDIT', 'manuscript_review_process_type', NULL, NULL, 'Y', NULL, 1, NOW(), 1, NOW(), NULL),
  (62001, '000000', 2, '校对流程', 'PROOFREAD', 'manuscript_review_process_type', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62010, '000000', 1, '审批中', 'IN_APPROVAL', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62011, '000000', 2, '已退回', 'RETURNED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62012, '000000', 3, '已完成', 'COMPLETED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62013, '000000', 4, '已取消', 'CANCELED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62014, '000000', 5, '已驳回', 'REJECTED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62020, '000000', 1, '待一级审批', 'LEVEL_1', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62021, '000000', 2, '待二级审批', 'LEVEL_2', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62022, '000000', 3, '待三级审批', 'LEVEL_3', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62023, '000000', 4, '待发起人处理', 'INITIATOR_ACTION', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62024, '000000', 5, '流程完成', 'DONE', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62025, '000000', 6, '流程已取消', 'CANCELED', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62026, '000000', 7, '流程已驳回', 'REJECTED', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62030, '000000', 1, '广西日报 / 要闻', 'GXRB_NEWS', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62031, '000000', 2, '广西日报 / 经济', 'GXRB_ECON', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62032, '000000', 3, '广西日报 / 民生', 'GXRB_LIFE', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62033, '000000', 4, '广西新闻网 / 推荐', 'GXNEWS_RECO', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL)
ON DUPLICATE KEY UPDATE
  dict_sort = VALUES(dict_sort),
  dict_label = VALUES(dict_label),
  dict_value = VALUES(dict_value),
  dict_type = VALUES(dict_type),
  is_default = VALUES(is_default),
  update_by = VALUES(update_by),
  update_time = VALUES(update_time);

/* -------------------------------------------------------------------------- */
/* 2) 菜单                                                                     */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query_param,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (60000, '审校管理', 0, 20, 'manuscript', 'None', NULL, 1, 0, 'M', '0', '0', NULL, 'document', NULL, 1, NOW(), 1, NOW(), 'manuscript_review release'),
  (60001, '审校台账', 60000, 1, 'review', 'manuscript-review/index', NULL, 1, 0, 'C', '0', '0', 'manuscript:review:list', 'list', NULL, 1, NOW(), 1, NOW(), '列表默认仅详情'),
  (60002, '审校详情', 60000, 6, 'review/detail', 'manuscript-review/detail', NULL, 1, 0, 'C', '1', '0', 'manuscript:review:detail', '#', NULL, 1, NOW(), 1, NOW(), '详情动作中枢'),
  (60003, '审校提交', 60001, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:submit', '#', NULL, 1, NOW(), 1, NOW(), '新增页仅提交'),
  (60004, '审校修改', 60001, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:edit', '#', NULL, 1, NOW(), 1, NOW(), '可审即可改/退回可改'),
  (60005, '再次提交', 60001, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:resubmit', '#', NULL, 1, NOW(), 1, NOW(), '修改与再次提交拆两步'),
  (60006, '去审批', 60001, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:goApprove', '#', NULL, 1, NOW(), 1, NOW(), '修改与去审批拆两步'),
  (60007, '审校表单', 60000, 5, 'review/form', 'manuscript-review/form', NULL, 1, 0, 'C', '1', '0', 'manuscript:review:form', '#', NULL, 1, NOW(), 1, NOW(), '新增与修改共用壳层页面'),
  (60008, '我发起的', 60000, 2, 'review/my-document', 'manuscript-review/my-document', NULL, 1, 0, 'C', '0', '0', 'manuscript:review:myDocument', 'guide', NULL, 1, NOW(), 1, NOW(), '发起人专属页'),
  (60009, '我的待办', 60000, 3, 'review/task-waiting', 'manuscript-review/task-waiting', NULL, 1, 0, 'C', '0', '0', 'manuscript:review:taskWaiting', 'waiting', NULL, 1, NOW(), 1, NOW(), '审批人待办页'),
  (60010, '我的已办', 60000, 4, 'review/task-finish', 'manuscript-review/task-finish', NULL, 1, 0, 'C', '0', '0', 'manuscript:review:taskFinish', 'finish', NULL, 1, NOW(), 1, NOW(), '审批人已办页')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_by = VALUES(update_by),
  update_time = VALUES(update_time),
  remark = VALUES(remark);

/* -------------------------------------------------------------------------- */
/* 3) 角色                                                                     */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_role (
  role_id, tenant_id, role_name, role_key, role_sort,
  data_scope, menu_check_strictly, dept_check_strictly,
  status, del_flag,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (63000, '000000', '审校一级审批人', 'manuscript_review_level_1_approver', 1, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), NULL),
  (63001, '000000', '审校二级审批人', 'manuscript_review_level_2_approver', 2, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), NULL),
  (63002, '000000', '审校三级审批人', 'manuscript_review_level_3_approver', 3, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), NULL),
  (63003, '000000', '持证发起人', 'manuscript_review_certified_initiator', 4, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), '命中后跳过一级审批'),
  (63004, '000000', '审校发起人', 'manuscript_review_initiator', 0, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), '发起审校流程与查看台账/详情')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_key = VALUES(role_key),
  role_sort = VALUES(role_sort),
  status = VALUES(status),
  del_flag = VALUES(del_flag),
  update_by = VALUES(update_by),
  update_time = VALUES(update_time),
  remark = VALUES(remark);

/* -------------------------------------------------------------------------- */
/* 4) 角色菜单收敛到最终发布态                                                 */
/* -------------------------------------------------------------------------- */
DELETE FROM sys_role_menu
WHERE role_id IN (63000, 63001, 63002, 63003, 63004)
  AND menu_id IN (
    60000, 60001, 60002, 60003, 60004, 60005, 60006, 60007, 60008, 60009, 60010,
    1026, 1017, 1600, 1601, 1602,
    11618, 11619, 11629, 11632
  );

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
  (63000, 60000), (63000, 60001), (63000, 60002), (63000, 60004), (63000, 60006), (63000, 60009), (63000, 60010),
  (63000, 1026), (63000, 1017), (63000, 1600), (63000, 1601), (63000, 1602),
  (63001, 60000), (63001, 60001), (63001, 60002), (63001, 60004), (63001, 60006), (63001, 60009), (63001, 60010),
  (63001, 1026), (63001, 1017), (63001, 1600), (63001, 1601), (63001, 1602),
  (63002, 60000), (63002, 60001), (63002, 60002), (63002, 60004), (63002, 60006), (63002, 60009), (63002, 60010),
  (63002, 1026), (63002, 1017), (63002, 1600), (63002, 1601), (63002, 1602),
  (63003, 60000), (63003, 60001), (63003, 60002), (63003, 60003), (63003, 60004), (63003, 60005), (63003, 60007), (63003, 60008),
  (63003, 1026), (63003, 1017), (63003, 1600), (63003, 1601), (63003, 1602),
  (63004, 60000), (63004, 60001), (63004, 60002), (63004, 60003), (63004, 60004), (63004, 60005), (63004, 60007), (63004, 60008),
  (63004, 1026), (63004, 1017), (63004, 1600), (63004, 1601), (63004, 1602);

/* -------------------------------------------------------------------------- */
/* 5) BPM 流程定义、节点、连线                                                 */
/* -------------------------------------------------------------------------- */
SET @tenant_id := '000000';
SET @operator := 'release';
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
       '0.000', '420,200|420,200', 'applicant-node', NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'first-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001104, 1, @audit_definition_id, 'second-review-node', '二级审批', '${manuscriptReviewSecondLevelApprover}',
       '0.000', '600,200|600,200', 'applicant-node', NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @audit_definition_id AND node_code = 'second-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001105, 1, @audit_definition_id, 'final-review-node', '三级审批', '${manuscriptReviewThirdLevelApprover}',
       '0.000', '780,200|780,200', 'applicant-node', NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination\"}]', '0', @tenant_id
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
       '0.000', '420,200|420,200', 'applicant-node', NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'first-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001204, 1, @proof_definition_id, 'second-review-node', '二级审批', '${manuscriptReviewSecondLevelApprover}',
       '0.000', '600,200|600,200', 'applicant-node', NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back\"}]', '0', @tenant_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node WHERE definition_id = @proof_definition_id AND node_code = 'second-review-node' AND del_flag = '0');

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT 2026032500000001205, 1, @proof_definition_id, 'final-review-node', '三级审批', '${manuscriptReviewThirdLevelApprover}',
       '0.000', '780,200|780,200', 'applicant-node', NULL, NULL, 'N', @form_path, '1', NOW(), @operator, NOW(), @operator,
       '[{\"code\":\"ButtonPermissionEnum\",\"value\":\"back,termination\"}]', '0', @tenant_id
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

UPDATE brain_manuscript_review_flow_config
SET flow_code = @audit_flow_code,
    level_one_role_key = 'manuscript_review_level_1_approver',
    level_two_role_key = 'manuscript_review_level_2_approver',
    level_three_role_key = 'manuscript_review_level_3_approver',
    status = '0',
    update_by = 1,
    update_time = NOW(),
    remark = 'manuscript_review release'
WHERE tenant_id = @tenant_id
  AND process_type = 'AUDIT';

UPDATE brain_manuscript_review_flow_config
SET flow_code = @proof_flow_code,
    level_one_role_key = 'manuscript_review_level_1_approver',
    level_two_role_key = 'manuscript_review_level_2_approver',
    level_three_role_key = 'manuscript_review_level_3_approver',
    status = '0',
    update_by = 1,
    update_time = NOW(),
    remark = 'manuscript_review release'
WHERE tenant_id = @tenant_id
  AND process_type = 'PROOFREAD';
