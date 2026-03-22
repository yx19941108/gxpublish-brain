/*
manuscript_review - DML v01 (seeds)

affected tables
- sys_menu
- sys_dict_type
- sys_dict_data
- sys_role
- sys_user
- sys_user_role
- sys_role_menu
- brain_manuscript_review_flow_config

backup action (before apply)
- Backup the affected platform tables above (logical dump recommended).
- Confirm the following ID ranges are unused in your environment:
  - sys_menu.menu_id:     60000-60007
  - sys_dict_type.dict_id: 61000-61003
  - sys_dict_data.dict_code: 62000-62039
  - sys_role.role_id:     63000-63004
  - sys_user.user_id:     64000-64004
  - brain_manuscript_review_flow_config.id: 65000-65001

apply order
1) 20260321_manuscript_review_ddl_v01.sql
2) 20260321_manuscript_review_dml_v01.sql

rollback order
1) 20260321_manuscript_review_rollback_v01.sql

NOTE: 本轮仅起草，未 apply。
*/

/* NOTE: do not rely on session variables (MySQL MCP may use non-sticky connections). */

/* -------------------------------------------------------------------------- */
/* 1) 字典：流程类型 / 流程状态 / 当前节点 / 媒体栏目预设（一期先走 sys_dict_* 种子）          */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_dict_type (
  dict_id, tenant_id, dict_name, dict_type,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (61000, '000000', '审校-流程类型', 'manuscript_review_process_type', NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 seeds'),
  (61001, '000000', '审校-流程状态', 'manuscript_review_flow_status', NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 seeds'),
  (61002, '000000', '审校-当前节点', 'manuscript_review_current_node', NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 seeds'),
  (61003, '000000', '审校-媒体栏目预设', 'manuscript_review_media_channel', NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 seeds');

/* 流程类型 */
INSERT INTO sys_dict_data (
  dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
  css_class, list_class, is_default,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (62000, '000000', 1, '审核流程', 'AUDIT', 'manuscript_review_process_type', NULL, NULL, 'Y', NULL, 1, NOW(), 1, NOW(), NULL),
  (62001, '000000', 2, '校对流程', 'PROOFREAD', 'manuscript_review_process_type', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL);

/* 流程状态（列表/详情展示使用 label，value 仅为内部值） */
INSERT INTO sys_dict_data (
  dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
  css_class, list_class, is_default,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (62010, '000000', 1, '审批中', 'IN_APPROVAL', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62011, '000000', 2, '已退回', 'RETURNED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62012, '000000', 3, '已完成', 'COMPLETED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62013, '000000', 4, '已取消', 'CANCELED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62014, '000000', 5, '已驳回', 'REJECTED', 'manuscript_review_flow_status', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL);

/* 当前节点（列表/详情展示使用 label，value 仅为内部值） */
INSERT INTO sys_dict_data (
  dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
  css_class, list_class, is_default,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (62020, '000000', 1, '待一级审批', 'LEVEL_1', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62021, '000000', 2, '待二级审批', 'LEVEL_2', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62022, '000000', 3, '待三级审批', 'LEVEL_3', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62023, '000000', 4, '待发起人处理', 'INITIATOR_ACTION', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62024, '000000', 5, '流程完成', 'DONE', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62025, '000000', 6, '流程已取消', 'CANCELED', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62026, '000000', 7, '流程已驳回', 'REJECTED', 'manuscript_review_current_node', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL);

/* 媒体/栏目预设（示例种子：可按业务补充；手填值不回写字典） */
INSERT INTO sys_dict_data (
  dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
  css_class, list_class, is_default,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (62030, '000000', 1, '广西日报 / 要闻', 'GXRB_NEWS', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62031, '000000', 2, '广西日报 / 经济', 'GXRB_ECON', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62032, '000000', 3, '广西日报 / 民生', 'GXRB_LIFE', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL),
  (62033, '000000', 4, '广西新闻网 / 推荐', 'GXNEWS_RECO', 'manuscript_review_media_channel', NULL, NULL, 'N', NULL, 1, NOW(), 1, NOW(), NULL);

/* -------------------------------------------------------------------------- */
/* 2) 菜单/按钮：一期无草稿/删除语义                                                   */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query_param,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (60000, '审校管理', 0, 20, 'manuscript', 'None', NULL, 1, 0, 'M', '0', '0', NULL, 'document',
   NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 seeds'),
  (60001, '审校台账', 60000, 1, 'review', 'manuscript-review/index', NULL, 1, 0, 'C', '0', '0', 'manuscript:review:list', 'list',
   NULL, 1, NOW(), 1, NOW(), '列表默认仅详情；不含删除/草稿'),
  (60007, '审校表单', 60000, 2, 'review/form', 'manuscript-review/form', NULL, 1, 0, 'C', '1', '0', 'manuscript:review:form', '#',
   NULL, 1, NOW(), 1, NOW(), '新增与修改共用壳层页面（可隐藏）'),
  (60002, '审校详情', 60000, 3, 'review/detail', 'manuscript-review/detail', NULL, 1, 0, 'C', '1', '0', 'manuscript:review:detail', '#',
   NULL, 1, NOW(), 1, NOW(), '详情作为动作中枢（可隐藏）'),
  (60003, '审校提交', 60001, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:submit', '#',
   NULL, 1, NOW(), 1, NOW(), '新增页仅提交，无草稿'),
  (60004, '审校修改', 60001, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:edit', '#',
   NULL, 1, NOW(), 1, NOW(), '可审即可改/退回可改'),
  (60005, '再次提交', 60001, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:resubmit', '#',
   NULL, 1, NOW(), 1, NOW(), '修改与再次提交拆两步'),
  (60006, '去审批', 60001, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'manuscript:review:goApprove', '#',
   NULL, 1, NOW(), 1, NOW(), '修改与去审批拆两步');

/* -------------------------------------------------------------------------- */
/* 3) 角色：一级/二级/三级审批人 + 持证发起人（成员由角色管理维护）                         */
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
  (63003, '000000', '持证发起人', 'manuscript_review_certified_initiator', 4, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), '提交/再次提交时实时判定，命中跳过一级审批'),
  (63004, '000000', '审校发起人', 'manuscript_review_initiator', 0, '1', 1, 1, '0', '0', NULL, 1, NOW(), 1, NOW(), '一期：用于发起审校流程与查看台账/详情（不等同于持证资格）');

/* -------------------------------------------------------------------------- */
/* 4) 菜单授权：给本模块新角色（可选：也可额外授权给现有超级管理员角色）                     */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
  /* module menus */
  (63000, 60000), (63000, 60001), (63000, 60007), (63000, 60002),
  (63001, 60000), (63001, 60001), (63001, 60007), (63001, 60002),
  (63002, 60000), (63002, 60001), (63002, 60007), (63002, 60002),
  (63003, 60000), (63003, 60001), (63003, 60007), (63003, 60002),
  (63004, 60000), (63004, 60001), (63004, 60007), (63004, 60002),

  /* module action perms (align with PRD action boundary) */
  (63000, 60004), (63000, 60006),
  (63001, 60004), (63001, 60006),
  (63002, 60004), (63002, 60006),
  (63003, 60003), (63003, 60004), (63003, 60005),
  (63004, 60003), (63004, 60004), (63004, 60005),

  /* workflow task menus (BPM todo/done; approvers only; copy is hidden in v01) */
  (63000, 11618), (63000, 11619), (63000, 11632),
  (63001, 11618), (63001, 11619), (63001, 11632),
  (63002, 11618), (63002, 11619), (63002, 11632),
  /* workflow initiated menu (my initiated; initiators only; copy is hidden in v01) */
  (63003, 11618), (63003, 11629),
  (63004, 11618), (63004, 11629),

  /* read-only cross-module perms used by form/detail (dict/dept/oss) */
  (63000, 1026), (63000, 1017), (63000, 1600), (63000, 1601), (63000, 1602),
  (63001, 1026), (63001, 1017), (63001, 1600), (63001, 1601), (63001, 1602),
  (63002, 1026), (63002, 1017), (63002, 1600), (63002, 1601), (63002, 1602),
  (63003, 1026), (63003, 1017), (63003, 1600), (63003, 1601), (63003, 1602),
  (63004, 1026), (63004, 1017), (63004, 1600), (63004, 1601), (63004, 1602);

/* -------------------------------------------------------------------------- */
/* 5) 审批链映射配置：流程类型 -> 1/2/3级审批角色（roleKey）                                */
/* -------------------------------------------------------------------------- */
INSERT INTO brain_manuscript_review_flow_config (
  id, tenant_id, process_type, flow_code,
  level_one_role_key, level_two_role_key, level_three_role_key,
  status, create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (65000, '000000', 'AUDIT', 'manuscript_review_audit_flow',
   'manuscript_review_level_1_approver', 'manuscript_review_level_2_approver', 'manuscript_review_level_3_approver',
   '0', NULL, 1, NOW(), 1, NOW(), 'v01 默认流程编码与三级串行审批'),
  (65001, '000000', 'PROOFREAD', 'manuscript_review_proofread_flow',
   'manuscript_review_level_1_approver', 'manuscript_review_level_2_approver', 'manuscript_review_level_3_approver',
   '0', NULL, 1, NOW(), 1, NOW(), 'v01 默认流程编码与三级串行审批');

/* -------------------------------------------------------------------------- */
/* 6) 测试用户与角色绑定（可选：仅用于开发/测试环境）                                      */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_user (
  user_id, tenant_id, dept_id,
  user_name, nick_name, user_type,
  email, phonenumber, sex, avatar,
  password, status, del_flag,
  login_ip, login_date,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (64000, '000000', NULL, 'mr_initiator', '审校发起人', 'sys_user', '', '', '2', NULL, '', '0', '0', '', NULL, NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 test user'),
  (64001, '000000', NULL, 'mr_approver_l1', '审校一级审批人', 'sys_user', '', '', '2', NULL, '', '0', '0', '', NULL, NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 test user'),
  (64002, '000000', NULL, 'mr_approver_l2', '审校二级审批人', 'sys_user', '', '', '2', NULL, '', '0', '0', '', NULL, NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 test user'),
  (64003, '000000', NULL, 'mr_approver_l3', '审校三级审批人', 'sys_user', '', '', '2', NULL, '', '0', '0', '', NULL, NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 test user'),
  (64004, '000000', NULL, 'mr_certified', '持证发起人示例', 'sys_user', '', '', '2', NULL, '', '0', '0', '', NULL, NULL, 1, NOW(), 1, NOW(), 'manuscript_review v01 test user');

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (64000, 63004),
  (64001, 63000),
  (64002, 63001),
  (64003, 63002),
  (64004, 63003);
