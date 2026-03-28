/* -------------------------------------------------------------------------- */
/* rollback for 20260323_manuscript_review_execution_role_matrix_seed_v01.sql */
/* Scope: local verification only.                                            */
/* -------------------------------------------------------------------------- */

DELETE FROM sys_user_role
WHERE user_id IN (64005, 64006, 64007, 64008, 64009)
   OR (user_id = 64005 AND role_id IN (63003, 63004))
   OR (user_id = 64006 AND role_id = 63000)
   OR (user_id = 64007 AND role_id = 63001)
   OR (user_id = 64008 AND role_id = 63002);

DELETE FROM sys_user
WHERE tenant_id = '000000'
  AND user_id IN (64005, 64006, 64007, 64008, 64009)
  AND user_name IN (
    'mr_certified_initiator',
    'mr_approver_l1_b',
    'mr_approver_l2_b',
    'mr_approver_l3_b',
    'mr_observer'
  );
