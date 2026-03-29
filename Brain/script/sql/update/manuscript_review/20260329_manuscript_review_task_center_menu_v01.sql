/* -------------------------------------------------------------------------- */
/* Owner: Codex                                                               */
/* Task: manuscript_review dedicated task-center menus                        */
/* Status: Draft                                                              */
/* Updated: 2026-03-29 10:45 +08:00                                           */
/* -------------------------------------------------------------------------- */
/*
apply note
- Decouple manuscript_review task-center menus from generic workflow menus.
- Roles:
  1) initiators -> dedicated "我发起的"
  2) approvers -> dedicated "我的待办" + "我的已办"
*/

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query_param,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 60008, '我发起的', 60000, 2, 'review/my-document', 'manuscript-review/my-document', NULL,
       1, 0, 'C', '0', '0', 'manuscript:review:myDocument', 'guide',
       NULL, 1, NOW(), 1, NOW(), 'manuscript_review 专属我发起页面'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 60008);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query_param,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 60009, '我的待办', 60000, 3, 'review/task-waiting', 'manuscript-review/task-waiting', NULL,
       1, 0, 'C', '0', '0', 'manuscript:review:taskWaiting', 'waiting',
       NULL, 1, NOW(), 1, NOW(), 'manuscript_review 专属待办页面'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 60009);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query_param,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 60010, '我的已办', 60000, 4, 'review/task-finish', 'manuscript-review/task-finish', NULL,
       1, 0, 'C', '0', '0', 'manuscript:review:taskFinish', 'finish',
       NULL, 1, NOW(), 1, NOW(), 'manuscript_review 专属已办页面'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 60010);

DELETE FROM sys_role_menu
WHERE role_id IN (63000, 63001, 63002, 63003, 63004)
  AND menu_id IN (11618, 11619, 11629, 11632);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63003, 60008 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63003 AND menu_id = 60008);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63004, 60008 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63004 AND menu_id = 60008);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63000, 60009 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63000 AND menu_id = 60009);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63001, 60009 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63001 AND menu_id = 60009);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63002, 60009 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63002 AND menu_id = 60009);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63000, 60010 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63000 AND menu_id = 60010);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63001, 60010 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63001 AND menu_id = 60010);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 63002, 60010 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 63002 AND menu_id = 60010);
