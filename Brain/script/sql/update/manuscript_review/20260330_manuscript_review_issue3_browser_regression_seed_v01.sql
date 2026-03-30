/* -------------------------------------------------------------------------- */
/* Owner: Codex                                                               */
/* Task: manuscript_review issue3 browser regression seed                     */
/* Status: Draft for local dev/test apply only                                */
/* Updated: 2026-03-30 20:30 +08:00                                           */
/* -------------------------------------------------------------------------- */
/*
apply intent
- seed the minimal runtime dataset for issue 3 browser-level closure regression
- cover four viewpoints:
  1) initiator-only ledger visibility
  2) approver waiting + finished visibility
  3) initiator + approver mixed-role union visibility
  4) observer negative visibility
- keep the ledger page action column frozen to "详情" only; verify edit/approve
  actions through detail pages instead of ledger rows

accounts covered
- mr_initiator                (64000)
- mr_approver_l1              (64001)
- mr_certified_initiator      (64005)
- mr_observer                 (64009)
- mr_mix_l1                   (64010)   newly added by this seed

notes
- this seed is intentionally idempotent: it deletes the dedicated sample rows
  by fixed ids/user_name before reinserting them
- it assumes the baseline manuscript_review DDL/DML and workflow seeds already
  exist in the local environment
*/

SET @tenant_id := '000000';
SET @operator := 1;
SET @password_bcrypt := '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne';
SET @audit_definition_id := 2026032500000000101;
SET @audit_flow_code := 'manuscript_review_audit_flow';
SET @approval_form_path := '/manuscript/review/approval';

/* -------------------------------------------------------------------------- */
/* 0) cleanup                                                                 */
/* -------------------------------------------------------------------------- */
DELETE FROM flow_user
WHERE tenant_id = @tenant_id
  AND id IN (
    880000330211, 880000330251
  );

DELETE FROM flow_his_task
WHERE tenant_id = @tenant_id
  AND id IN (
    880000330221, 880000330231,
    880000330261, 880000330271, 880000330281
  );

DELETE FROM flow_task
WHERE tenant_id = @tenant_id
  AND id IN (
    880000330212, 880000330252
  );

DELETE FROM flow_instance
WHERE tenant_id = @tenant_id
  AND id IN (
    880000330102, 880000330103,
    880000330105, 880000330106
  );

DELETE FROM brain_manuscript_review_history
WHERE tenant_id = @tenant_id
  AND id IN (
    880000330401, 880000330402,
    880000330411,
    880000330421, 880000330422,
    880000330431, 880000330432,
    880000330441,
    880000330451, 880000330452
  );

DELETE FROM brain_manuscript_review
WHERE tenant_id = @tenant_id
  AND id IN (
    880000330001, 880000330002, 880000330003,
    880000330004, 880000330005, 880000330006
  );

DELETE FROM sys_user_role
WHERE user_id = 64010;

DELETE FROM sys_user
WHERE tenant_id = @tenant_id
  AND user_id = 64010
  AND user_name = 'mr_mix_l1';

/* -------------------------------------------------------------------------- */
/* 1) mixed-role execution account                                            */
/* -------------------------------------------------------------------------- */
INSERT INTO sys_user (
  user_id, tenant_id, dept_id,
  user_name, nick_name, user_type,
  email, phonenumber, sex, avatar,
  password, status, del_flag,
  login_ip, login_date,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES (
  64010, @tenant_id, NULL,
  'mr_mix_l1', '审校混合角色L1', 'sys_user',
  '', '', '2', NULL,
  @password_bcrypt, '0', '0',
  '', NULL,
  NULL, @operator, NOW(), @operator, NOW(), 'issue3 browser regression mixed-role seed user'
);

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (64010, 63004),
  (64010, 63000);

/* -------------------------------------------------------------------------- */
/* 2) review records                                                          */
/* -------------------------------------------------------------------------- */
INSERT INTO brain_manuscript_review (
  id, tenant_id, process_type, flow_code, flow_instance_id,
  flow_status_label, current_node_status, current_node_label,
  manuscript_code, external_manuscript_code,
  title, media_channel, submit_department, author_name, remark_text, content_body,
  content, note, media_channel_dict_code, media_channel_label,
  submitter_dept_id, submitter_dept_name, author_names,
  initiator_user_id, initiator_name,
  first_submit_time, latest_submit_time,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (
    880000330001, @tenant_id, 'AUDIT', @audit_flow_code, NULL,
    '已退回', 'RETURN_TO_INITIATOR', '待发起人处理',
    'MR-I3-INIT-001', 'MR-I3-INIT-001',
    '问题3回归-发起人仅可见样本',
    '审校回归/问题3/发起',
    '审校部',
    '张三',
    'issue3 initiator-only returned sample',
    '用于验证纯发起人台账仅看我发起且台账行仅保留详情按钮。',
    '用于验证纯发起人台账仅看我发起且台账行仅保留详情按钮。',
    NULL,
    NULL,
    '集团网站/要闻',
    NULL, '审校部', '张三',
    64000, '审校发起人',
    '2026-03-30 19:00:00', '2026-03-30 19:20:00',
    NULL, 64000, '2026-03-30 19:00:00', 64000, '2026-03-30 19:20:00',
    'initiator-only returned sample'
  ),
  (
    880000330002, @tenant_id, 'AUDIT', @audit_flow_code, 880000330102,
    '审批中', 'LEVEL_1', '待一级审批',
    'MR-I3-L1-WAIT-001', 'MR-I3-L1-WAIT-001',
    '问题3回归-一级审批待办样本',
    '审校回归/问题3/一级待办',
    '审校部',
    '李四',
    'issue3 l1 waiting sample',
    '用于验证纯审批人台账可见我的待办，详情页存在修改/去审批。',
    '用于验证纯审批人台账可见我的待办，详情页存在修改/去审批。',
    NULL,
    NULL,
    '集团网站/要闻',
    NULL, '审校部', '李四',
    64005, '持证发起人',
    '2026-03-30 19:10:00', '2026-03-30 19:10:00',
    NULL, 64005, '2026-03-30 19:10:00', 64005, '2026-03-30 19:10:00',
    'approver waiting sample for mr_approver_l1'
  ),
  (
    880000330003, @tenant_id, 'AUDIT', @audit_flow_code, 880000330103,
    '审批中', 'LEVEL_2', '待二级审批',
    'MR-I3-L1-FIN-001', 'MR-I3-L1-FIN-001',
    '问题3回归-一级审批已办样本',
    '审校回归/问题3/一级已办',
    '审校部',
    '李四',
    'issue3 l1 finished sample',
    '用于验证纯审批人台账可见我的已办，但详情页仅查看。',
    '用于验证纯审批人台账可见我的已办，但详情页仅查看。',
    NULL,
    NULL,
    '集团网站/要闻',
    NULL, '审校部', '李四',
    64005, '持证发起人',
    '2026-03-30 18:40:00', '2026-03-30 19:05:00',
    NULL, 64005, '2026-03-30 18:40:00', 64001, '2026-03-30 19:05:00',
    'approver finished sample for mr_approver_l1'
  ),
  (
    880000330004, @tenant_id, 'AUDIT', @audit_flow_code, NULL,
    '已退回', 'RETURN_TO_INITIATOR', '待发起人处理',
    'MR-I3-MIX-INIT-001', 'MR-I3-MIX-INIT-001',
    '问题3回归-混合角色发起样本',
    '审校回归/问题3/混合发起',
    '审校部',
    '王五',
    'issue3 mixed-role initiator sample',
    '用于验证混合角色台账可见我发起的样本。',
    '用于验证混合角色台账可见我发起的样本。',
    NULL,
    NULL,
    '集团网站/要闻',
    NULL, '审校部', '王五',
    64010, '审校混合角色L1',
    '2026-03-30 18:20:00', '2026-03-30 18:50:00',
    NULL, 64010, '2026-03-30 18:20:00', 64010, '2026-03-30 18:50:00',
    'mixed-role initiator sample'
  ),
  (
    880000330005, @tenant_id, 'AUDIT', @audit_flow_code, 880000330105,
    '审批中', 'LEVEL_1', '待一级审批',
    'MR-I3-MIX-WAIT-001', 'MR-I3-MIX-WAIT-001',
    '问题3回归-混合角色待办样本',
    '审校回归/问题3/混合待办',
    '审校部',
    '赵六',
    'issue3 mixed-role waiting sample',
    '用于验证混合角色台账可见我的待办样本，详情页存在修改/去审批。',
    '用于验证混合角色台账可见我的待办样本，详情页存在修改/去审批。',
    NULL,
    NULL,
    '集团网站/要闻',
    NULL, '审校部', '赵六',
    64005, '持证发起人',
    '2026-03-30 19:15:00', '2026-03-30 19:15:00',
    NULL, 64005, '2026-03-30 19:15:00', 64005, '2026-03-30 19:15:00',
    'mixed-role waiting sample'
  ),
  (
    880000330006, @tenant_id, 'AUDIT', @audit_flow_code, 880000330106,
    '审批中', 'LEVEL_2', '待二级审批',
    'MR-I3-MIX-FIN-001', 'MR-I3-MIX-FIN-001',
    '问题3回归-混合角色已办样本',
    '审校回归/问题3/混合已办',
    '审校部',
    '赵六',
    'issue3 mixed-role finished sample',
    '用于验证混合角色台账可见我的已办样本，但详情页仅查看。',
    '用于验证混合角色台账可见我的已办样本，但详情页仅查看。',
    NULL,
    NULL,
    '集团网站/要闻',
    NULL, '审校部', '赵六',
    64005, '持证发起人',
    '2026-03-30 18:30:00', '2026-03-30 18:55:00',
    NULL, 64005, '2026-03-30 18:30:00', 64010, '2026-03-30 18:55:00',
    'mixed-role finished sample'
  );

/* -------------------------------------------------------------------------- */
/* 3) readable history                                                        */
/* -------------------------------------------------------------------------- */
INSERT INTO brain_manuscript_review_history (
  id, tenant_id, review_id,
  action_type, action_text, actor_user_id, actor_name,
  create_time, ext_json
) VALUES
  (
    880000330401, @tenant_id, 880000330001,
    'CREATE', '审校发起人新增了流程。', 64000, '审校发起人',
    '2026-03-30 19:00:00', NULL
  ),
  (
    880000330402, @tenant_id, 880000330001,
    'RETURN_TO_INITIATOR', '审校一级审批人退回了流程，待发起人处理。', 64001, '审校一级审批人',
    '2026-03-30 19:20:00', NULL
  ),
  (
    880000330411, @tenant_id, 880000330002,
    'CREATE', '持证发起人新增了流程。', 64005, '持证发起人',
    '2026-03-30 19:10:00', NULL
  ),
  (
    880000330421, @tenant_id, 880000330003,
    'CREATE', '持证发起人新增了流程。', 64005, '持证发起人',
    '2026-03-30 18:40:00', NULL
  ),
  (
    880000330422, @tenant_id, 880000330003,
    'LEVEL_ONE_APPROVED', '审校一级审批人审批通过，流程进入待二级审批。', 64001, '审校一级审批人',
    '2026-03-30 19:05:00', NULL
  ),
  (
    880000330431, @tenant_id, 880000330004,
    'CREATE', '审校混合角色L1新增了流程。', 64010, '审校混合角色L1',
    '2026-03-30 18:20:00', NULL
  ),
  (
    880000330432, @tenant_id, 880000330004,
    'RETURN_TO_INITIATOR', '审校一级审批人退回了流程，待发起人处理。', 64001, '审校一级审批人',
    '2026-03-30 18:50:00', NULL
  ),
  (
    880000330441, @tenant_id, 880000330005,
    'CREATE', '持证发起人新增了流程。', 64005, '持证发起人',
    '2026-03-30 19:15:00', NULL
  ),
  (
    880000330451, @tenant_id, 880000330006,
    'CREATE', '持证发起人新增了流程。', 64005, '持证发起人',
    '2026-03-30 18:30:00', NULL
  ),
  (
    880000330452, @tenant_id, 880000330006,
    'LEVEL_ONE_APPROVED', '审校混合角色L1审批通过，流程进入待二级审批。', 64010, '审校混合角色L1',
    '2026-03-30 18:55:00', NULL
  );

/* -------------------------------------------------------------------------- */
/* 4) workflow runtime data                                                   */
/* -------------------------------------------------------------------------- */
INSERT INTO flow_instance (
  id, definition_id, business_id,
  node_type, node_code, node_name,
  variable, flow_status, activity_status, def_json,
  create_time, create_by, update_time, update_by, ext, del_flag, tenant_id
) VALUES
  (
    880000330102, @audit_definition_id, '880000330002',
    1, 'first-review-node', '一级审批',
    '{"businessId":"880000330002","initiator":"64005","processType":"AUDIT","manuscriptReviewFirstLevelApprover":"role:63000","manuscriptReviewSecondLevelApprover":"role:63001","manuscriptReviewThirdLevelApprover":"role:63002","isCertified":true}',
    'WAITING', 1, '{"formPath":"/manuscript/review/approval"}',
    '2026-03-30 19:10:00', 64005, '2026-03-30 19:10:00', 64005, NULL, '0', @tenant_id
  ),
  (
    880000330103, @audit_definition_id, '880000330003',
    1, 'second-review-node', '二级审批',
    '{"businessId":"880000330003","initiator":"64005","processType":"AUDIT","manuscriptReviewFirstLevelApprover":"role:63000","manuscriptReviewSecondLevelApprover":"role:63001","manuscriptReviewThirdLevelApprover":"role:63002","isCertified":true}',
    'WAITING', 1, '{"formPath":"/manuscript/review/approval"}',
    '2026-03-30 18:40:00', 64005, '2026-03-30 19:05:00', 64001, NULL, '0', @tenant_id
  ),
  (
    880000330105, @audit_definition_id, '880000330005',
    1, 'first-review-node', '一级审批',
    '{"businessId":"880000330005","initiator":"64005","processType":"AUDIT","manuscriptReviewFirstLevelApprover":"role:63000","manuscriptReviewSecondLevelApprover":"role:63001","manuscriptReviewThirdLevelApprover":"role:63002","isCertified":true}',
    'WAITING', 1, '{"formPath":"/manuscript/review/approval"}',
    '2026-03-30 19:15:00', 64005, '2026-03-30 19:15:00', 64005, NULL, '0', @tenant_id
  ),
  (
    880000330106, @audit_definition_id, '880000330006',
    1, 'second-review-node', '二级审批',
    '{"businessId":"880000330006","initiator":"64005","processType":"AUDIT","manuscriptReviewFirstLevelApprover":"role:63000","manuscriptReviewSecondLevelApprover":"role:63001","manuscriptReviewThirdLevelApprover":"role:63002","isCertified":true}',
    'WAITING', 1, '{"formPath":"/manuscript/review/approval"}',
    '2026-03-30 18:30:00', 64005, '2026-03-30 18:55:00', 64010, NULL, '0', @tenant_id
  );

INSERT INTO flow_task (
  id, definition_id, instance_id,
  node_code, node_name, node_type,
  flow_status, form_custom, form_path,
  create_time, create_by, update_time, update_by, del_flag, tenant_id
) VALUES
  (
    880000330212, @audit_definition_id, 880000330102,
    'first-review-node', '一级审批', 1,
    'WAITING', 'N', @approval_form_path,
    '2026-03-30 19:10:00', 64005, '2026-03-30 19:10:00', 64005, '0', @tenant_id
  ),
  (
    880000330252, @audit_definition_id, 880000330105,
    'first-review-node', '一级审批', 1,
    'WAITING', 'N', @approval_form_path,
    '2026-03-30 19:15:00', 64005, '2026-03-30 19:15:00', 64005, '0', @tenant_id
  );

INSERT INTO flow_user (
  id, type, processed_by, associated,
  create_time, create_by, update_time, update_by, del_flag, tenant_id
) VALUES
  (
    880000330211, '1', 64001, 880000330212,
    '2026-03-30 19:10:00', 64005, '2026-03-30 19:10:00', 64005, '0', @tenant_id
  ),
  (
    880000330251, '1', 64010, 880000330252,
    '2026-03-30 19:15:00', 64005, '2026-03-30 19:15:00', 64005, '0', @tenant_id
  );

INSERT INTO flow_his_task (
  id, definition_id, instance_id, task_id,
  node_code, node_name, node_type,
  target_node_code, target_node_name,
  approver, cooperate_type, collaborator,
  skip_type, flow_status, form_custom, form_path,
  message, variable, ext,
  create_time, update_time, del_flag, tenant_id
) VALUES
  (
    880000330221, @audit_definition_id, 880000330102, 880000330201,
    'applicant-node', '审校申请', 1,
    'first-review-node', '一级审批',
    64005, 1, NULL,
    'PASS', 'PASS', 'N', @approval_form_path,
    NULL, '{"businessId":"880000330002","initiator":"64005"}', NULL,
    '2026-03-30 19:10:00', '2026-03-30 19:10:00', '0', @tenant_id
  ),
  (
    880000330231, @audit_definition_id, 880000330103, 880000330202,
    'applicant-node', '审校申请', 1,
    'first-review-node', '一级审批',
    64005, 1, NULL,
    'PASS', 'PASS', 'N', @approval_form_path,
    NULL, '{"businessId":"880000330003","initiator":"64005"}', NULL,
    '2026-03-30 18:40:00', '2026-03-30 18:40:00', '0', @tenant_id
  ),
  (
    880000330261, @audit_definition_id, 880000330103, 880000330203,
    'first-review-node', '一级审批', 1,
    'second-review-node', '二级审批',
    64001, 1, NULL,
    'PASS', 'PASS', 'N', @approval_form_path,
    NULL, '{"businessId":"880000330003","initiator":"64005"}', NULL,
    '2026-03-30 19:05:00', '2026-03-30 19:05:00', '0', @tenant_id
  ),
  (
    880000330271, @audit_definition_id, 880000330106, 880000330204,
    'applicant-node', '审校申请', 1,
    'first-review-node', '一级审批',
    64005, 1, NULL,
    'PASS', 'PASS', 'N', @approval_form_path,
    NULL, '{"businessId":"880000330006","initiator":"64005"}', NULL,
    '2026-03-30 18:30:00', '2026-03-30 18:30:00', '0', @tenant_id
  ),
  (
    880000330281, @audit_definition_id, 880000330106, 880000330205,
    'first-review-node', '一级审批', 1,
    'second-review-node', '二级审批',
    64010, 1, NULL,
    'PASS', 'PASS', 'N', @approval_form_path,
    NULL, '{"businessId":"880000330006","initiator":"64005"}', NULL,
    '2026-03-30 18:55:00', '2026-03-30 18:55:00', '0', @tenant_id
  );
