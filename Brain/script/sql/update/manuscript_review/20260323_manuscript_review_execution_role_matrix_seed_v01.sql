/* -------------------------------------------------------------------------- */
/* dev-only manuscript_review execution role-matrix seed                      */
/* Scope: local verification only. Do not promote as a formal upgrade script. */
/* Purpose: supplement execution-phase test accounts for role visibility,     */
/*          candidate pool, certified initiator, and unrelated observer.      */
/* Apply after the already-executed baseline:                                 */
/*   1) 20260321_manuscript_review_ddl_v01.sql                                */
/*   2) 20260321_manuscript_review_dml_v01.sql                                */
/*   3) 20260323_manuscript_review_dev_login_seed_v01-废弃 .sql                     */
/*   4) 20260323_manuscript_review_readable_http_fixture_v01.sql              */
/* -------------------------------------------------------------------------- */

/*
backup action (before apply)
- Backup sys_user and sys_user_role rows for tenant_id = '000000' if your
  environment already uses the IDs or user_names below.
- Confirm the following IDs are unused:
  - sys_user.user_id: 64005-64009

apply intent
- Add one certified initiator who can actually launch flows.
- Add second candidate approvers for L1/L2/L3.
- Add one unrelated observer user for visibility-negative tests.
*/

INSERT INTO sys_user (
  user_id, tenant_id, dept_id,
  user_name, nick_name, user_type,
  email, phonenumber, sex, avatar,
  password, status, del_flag,
  login_ip, login_date,
  create_dept, create_by, create_time, update_by, update_time, remark
) VALUES
  (
    64005, '000000', NULL,
    'mr_certified_initiator', '持证发起人',
    'sys_user', '', '', '2', NULL,
    '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne',
    '0', '0', '', NULL,
    NULL, 1, NOW(), 1, NOW(), 'manuscript_review execution seed user'
  ),
  (
    64006, '000000', NULL,
    'mr_approver_l1_b', '审校一级审批人B',
    'sys_user', '', '', '2', NULL,
    '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne',
    '0', '0', '', NULL,
    NULL, 1, NOW(), 1, NOW(), 'manuscript_review execution seed user'
  ),
  (
    64007, '000000', NULL,
    'mr_approver_l2_b', '审校二级审批人B',
    'sys_user', '', '', '2', NULL,
    '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne',
    '0', '0', '', NULL,
    NULL, 1, NOW(), 1, NOW(), 'manuscript_review execution seed user'
  ),
  (
    64008, '000000', NULL,
    'mr_approver_l3_b', '审校三级审批人B',
    'sys_user', '', '', '2', NULL,
    '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne',
    '0', '0', '', NULL,
    NULL, 1, NOW(), 1, NOW(), 'manuscript_review execution seed user'
  ),
  (
    64009, '000000', NULL,
    'mr_observer', '审校无关用户',
    'sys_user', '', '', '2', NULL,
    '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne',
    '0', '0', '', NULL,
    NULL, 1, NOW(), 1, NOW(), 'manuscript_review execution seed user'
  );

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (64005, 63003),
  (64005, 63004),
  (64006, 63000),
  (64007, 63001),
  (64008, 63002);
