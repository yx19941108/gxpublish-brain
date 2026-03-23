/* -------------------------------------------------------------------------- */
/* dev-only manuscript_review login seed                                      */
/* Scope: local verification only. Do not promote as a formal upgrade script. */
/* Purpose: enable existing mr_* users to log in without changing other data. */
/* -------------------------------------------------------------------------- */

UPDATE sys_user
SET password = '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne'
WHERE tenant_id = '000000'
  AND user_type = 'sys_user'
  AND user_id IN (64000, 64001, 64002, 64003, 64004)
  AND user_name IN (
    'mr_initiator',
    'mr_approver_l1',
    'mr_approver_l2',
    'mr_approver_l3',
    'mr_certified'
  );
