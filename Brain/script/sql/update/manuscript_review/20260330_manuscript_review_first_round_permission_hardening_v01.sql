-- Owner: Codex
-- Task: manuscript_review R8 first-round permission hardening
-- Status: Draft
-- Updated: 2026-03-30 13:30:00 +08:00

-- 第一轮问题 9：收紧“审校表单”入口，只保留发起侧角色。

DELETE FROM sys_role_menu
WHERE menu_id = 60007
  AND role_id IN (63000, 63001, 63002);
