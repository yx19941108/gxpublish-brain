-- Fix certified applicant routing for editorial_review_flow.
-- Warm-Flow 1.8.4 evaluates skip_condition only on gateway nodes.
-- This script converts applicant-node conditional branches into:
--   applicant-node -> certified-route-node
--   certified-route-node --(ne@@isCertified|true)--> first-review-node
--   certified-route-node --(eq@@isCertified|true)--> second-review-node

SET @target_tenant := '099142';
SET @flow_code := 'editorial_review_flow';
SET @flow_operator := '1';
SET @route_node_code := 'certified-route-node';
SET @route_node_name := 'Certified Route';

SET @definition_id := (
    SELECT id
    FROM flow_definition
    WHERE tenant_id = @target_tenant
      AND flow_code = @flow_code
      AND del_flag = '0'
    ORDER BY id DESC
    LIMIT 1
);

SET @flow_version := (
    SELECT version
    FROM flow_definition
    WHERE id = @definition_id
);

SET @route_node_id := (
    SELECT id
    FROM flow_node
    WHERE definition_id = @definition_id
      AND node_code = @route_node_code
    ORDER BY id DESC
    LIMIT 1
);

SET @route_node_id := IFNULL(@route_node_id, (SELECT IFNULL(MAX(id), 0) + 1 FROM flow_node));
SET @route_to_second_skip_id := (SELECT IFNULL(MAX(id), 0) + 1 FROM flow_skip);

SELECT @target_tenant AS target_tenant,
       @flow_code AS flow_code,
       @definition_id AS definition_id,
       @route_node_id AS route_node_id;

SELECT id,
       node_code,
       node_type,
       coordinate
FROM flow_node
WHERE definition_id = @definition_id
  AND node_code IN ('applicant-node', @route_node_code, 'first-review-node', 'second-review-node')
ORDER BY id;

SELECT id,
       now_node_code,
       next_node_code,
       next_node_type,
       skip_condition
FROM flow_skip
WHERE definition_id = @definition_id
  AND (now_node_code = 'applicant-node' OR now_node_code = @route_node_code)
ORDER BY id;

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT @route_node_id, 3, @definition_id, @route_node_code, @route_node_name, NULL, '0.000', '420,200|420,200',
       NULL, NULL, NULL, 'N', NULL, @flow_version, NOW(), @flow_operator, NOW(), @flow_operator, NULL, '0', @target_tenant
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM flow_node
    WHERE definition_id = @definition_id
      AND node_code = @route_node_code
);

SELECT ROW_COUNT() AS route_node_inserted_rows;

UPDATE flow_skip
SET next_node_code = @route_node_code,
    next_node_type = 3,
    skip_name = NULL,
    skip_condition = NULL,
    coordinate = '300,200;360,200'
WHERE definition_id = @definition_id
  AND now_node_code = 'applicant-node'
  AND next_node_code = 'first-review-node';

SELECT ROW_COUNT() AS applicant_to_route_updated_rows;

UPDATE flow_skip
SET now_node_code = @route_node_code,
    now_node_type = 3,
    next_node_code = 'first-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_condition = 'ne@@isCertified|true',
    coordinate = '450,160;510,160'
WHERE definition_id = @definition_id
  AND now_node_code = 'applicant-node'
  AND next_node_code = 'second-review-node';

SELECT ROW_COUNT() AS route_to_first_updated_rows;

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT @route_to_second_skip_id, @definition_id, @route_node_code, 3, 'second-review-node', 1, NULL, 'PASS',
       'eq@@isCertified|true', '450,240;570,240', NOW(), @flow_operator, NOW(), @flow_operator, '0', @target_tenant
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM flow_skip
    WHERE definition_id = @definition_id
      AND now_node_code = @route_node_code
      AND next_node_code = 'second-review-node'
);

SELECT ROW_COUNT() AS route_to_second_inserted_rows;

SELECT id,
       node_code,
       node_type,
       coordinate
FROM flow_node
WHERE definition_id = @definition_id
  AND node_code IN ('applicant-node', @route_node_code, 'first-review-node', 'second-review-node')
ORDER BY id;

SELECT id,
       now_node_code,
       next_node_code,
       next_node_type,
       skip_condition
FROM flow_skip
WHERE definition_id = @definition_id
  AND (now_node_code = 'applicant-node' OR now_node_code = @route_node_code)
ORDER BY id;
