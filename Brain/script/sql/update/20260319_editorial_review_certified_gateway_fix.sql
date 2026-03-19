-- Fix certified applicant routing for warm-flow 1.8.4 by moving conditional PASS
-- branches behind a gateway node. warm-flow only evaluates skipCondition on
-- gateway nodes; non-gateway applicant-node PASS branches always select the first edge.

SET @target_tenant := '099142';
SET @flow_code := 'editorial_review_flow';
SET @route_node_code := 'certified-route-node';
SET @route_node_type := 3;
SET @operator := 'codex';
SET @now_time := NOW();

SET @definition_id := (
    SELECT id
    FROM flow_definition
    WHERE tenant_id = @target_tenant
      AND flow_code = @flow_code
      AND del_flag = '0'
    ORDER BY id DESC
    LIMIT 1
);

SELECT @target_tenant AS target_tenant,
       @flow_code AS flow_code,
       @definition_id AS definition_id,
       @route_node_code AS route_node_code;

SET @flow_version := (
    SELECT version
    FROM flow_definition
    WHERE id = @definition_id
    LIMIT 1
);

SET @route_node_id := (
    SELECT id
    FROM flow_node
    WHERE definition_id = @definition_id
      AND node_code = @route_node_code
      AND del_flag = '0'
    LIMIT 1
);

SET @next_node_seed := (SELECT IFNULL(MAX(id), 0) + 1 FROM flow_node);

INSERT INTO flow_node
(`id`, `node_type`, `definition_id`, `node_code`, `node_name`, `permission_flag`, `node_ratio`, `coordinate`,
 `any_node_skip`, `listener_type`, `listener_path`, `form_custom`, `form_path`, `version`, `create_time`, `create_by`,
 `update_time`, `update_by`, `ext`, `del_flag`, `tenant_id`)
SELECT IFNULL(@route_node_id, @next_node_seed), @route_node_type, @definition_id, @route_node_code, 'Certified Route',
       NULL, '0.000', '420,200|420,200', NULL, NULL, NULL, 'N', NULL, @flow_version, @now_time, @operator, @now_time,
       @operator, '[]', '0', @target_tenant
FROM DUAL
WHERE @route_node_id IS NULL;

UPDATE flow_node
SET node_type = @route_node_type,
    node_name = 'Certified Route',
    permission_flag = NULL,
    node_ratio = '0.000',
    coordinate = '420,200|420,200',
    form_custom = 'N',
    form_path = NULL,
    ext = '[]',
    update_time = @now_time,
    update_by = @operator,
    del_flag = '0',
    tenant_id = @target_tenant
WHERE definition_id = @definition_id
  AND node_code = @route_node_code;

UPDATE flow_skip
SET now_node_code = 'applicant-node',
    now_node_type = 1,
    next_node_code = @route_node_code,
    next_node_type = @route_node_type,
    skip_name = NULL,
    skip_condition = NULL,
    coordinate = '300,200;360,200',
    update_time = @now_time,
    update_by = @operator,
    del_flag = '0',
    tenant_id = @target_tenant
WHERE definition_id = @definition_id
  AND (
        (now_node_code = 'applicant-node' AND next_node_code = 'first-review-node')
        OR (now_node_code = 'applicant-node' AND next_node_code = @route_node_code)
      );

SELECT ROW_COUNT() AS applicant_to_route_rows;

UPDATE flow_skip
SET now_node_code = @route_node_code,
    now_node_type = @route_node_type,
    next_node_code = 'first-review-node',
    next_node_type = 1,
    skip_name = NULL,
    skip_condition = 'ne@@isCertified|true',
    coordinate = '450,160;510,160',
    update_time = @now_time,
    update_by = @operator,
    del_flag = '0',
    tenant_id = @target_tenant
WHERE definition_id = @definition_id
  AND (
        (now_node_code = 'applicant-node' AND next_node_code = 'second-review-node')
        OR (now_node_code = @route_node_code AND next_node_code = 'first-review-node')
      );

SELECT ROW_COUNT() AS route_to_first_rows;

SET @next_skip_seed := (SELECT IFNULL(MAX(id), 0) + 1 FROM flow_skip);

INSERT INTO flow_skip
(`id`, `definition_id`, `now_node_code`, `now_node_type`, `next_node_code`, `next_node_type`, `skip_name`, `skip_type`,
 `skip_condition`, `coordinate`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`, `tenant_id`)
SELECT @next_skip_seed, @definition_id, @route_node_code, @route_node_type, 'second-review-node', 1, NULL, 'PASS',
       'eq@@isCertified|true', '450,240;570,240', @now_time, @operator, @now_time, @operator, '0', @target_tenant
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM flow_skip
    WHERE definition_id = @definition_id
      AND now_node_code = @route_node_code
      AND next_node_code = 'second-review-node'
      AND del_flag = '0'
);

UPDATE flow_skip
SET skip_name = NULL,
    skip_type = 'PASS',
    skip_condition = 'eq@@isCertified|true',
    coordinate = '450,240;570,240',
    update_time = @now_time,
    update_by = @operator,
    del_flag = '0',
    tenant_id = @target_tenant
WHERE definition_id = @definition_id
  AND now_node_code = @route_node_code
  AND next_node_code = 'second-review-node';

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
       skip_condition,
       coordinate
FROM flow_skip
WHERE definition_id = @definition_id
  AND (
        now_node_code = 'applicant-node'
        OR now_node_code = @route_node_code
      )
ORDER BY id;
