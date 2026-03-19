package com.gxpublish.brain.editorial.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class EditorialReviewWorkflowDefinitionTest {

    @Test
    void shouldNormalizeSupportedProcessType() {
        assertEquals("AUDIT", EditorialReviewWorkflowDefinition.normalizeProcessType(" audit "));
        assertEquals("PROOFREAD", EditorialReviewWorkflowDefinition.normalizeProcessType("proofread"));
    }

    @Test
    void shouldRejectUnsupportedProcessType() {
        assertThrows(ServiceException.class,
            () -> EditorialReviewWorkflowDefinition.normalizeProcessType("publish"));
    }

    @Test
    void shouldResolveWaitingStatusByLockedNodeCodeOnly() {
        assertEquals(ReviewStatusEnum.WAITING_FIRST.getCode(),
            EditorialReviewWorkflowDefinition.resolveWaitingStatus("first-review-node"));
        assertEquals(ReviewStatusEnum.WAITING_SECOND.getCode(),
            EditorialReviewWorkflowDefinition.resolveWaitingStatus("second-review-node"));
        assertEquals(ReviewStatusEnum.WAITING_FINAL.getCode(),
            EditorialReviewWorkflowDefinition.resolveWaitingStatus("final-review-node"));
        assertNull(EditorialReviewWorkflowDefinition.resolveWaitingStatus("dept-audit-node"));
    }

    @Test
    void shouldSkipFirstWaitingStatusForCertifiedApplicant() {
        assertEquals(ReviewStatusEnum.WAITING_FIRST.getCode(),
            EditorialReviewWorkflowDefinition.resolveSubmitWaitingStatus(false));
        assertEquals(ReviewStatusEnum.WAITING_SECOND.getCode(),
            EditorialReviewWorkflowDefinition.resolveSubmitWaitingStatus(true));
    }

    @Test
    void shouldRouteCertifiedSplitThroughSerialGateway() throws IOException {
        JsonNode root = loadFlowDefinition();
        JsonNode nodeList = root.path("nodeList");

        JsonNode applicantNode = requireNode(nodeList, "applicant-node");
        JsonNode applicantSkips = applicantNode.path("skipList");
        assertEquals(1, applicantSkips.size(), "applicant-node should route to a single gateway node");
        JsonNode applicantSkip = applicantSkips.get(0);
        assertEquals("certified-route-node", applicantSkip.path("nextNodeCode").asText());
        assertTrue(applicantSkip.path("skipCondition").isNull() || applicantSkip.path("skipCondition").asText().isBlank());

        JsonNode routeGateway = requireNode(nodeList, "certified-route-node");
        assertEquals("3", routeGateway.path("nodeType").asText(), "certified split must use a serial gateway");
        JsonNode gatewaySkips = routeGateway.path("skipList");
        assertEquals(2, gatewaySkips.size(), "serial gateway should own the two conditional branches");
        assertEquals("ne@@isCertified|true", gatewaySkips.get(0).path("skipCondition").asText());
        assertEquals("first-review-node", gatewaySkips.get(0).path("nextNodeCode").asText());
        assertEquals("eq@@isCertified|true", gatewaySkips.get(1).path("skipCondition").asText());
        assertEquals("second-review-node", gatewaySkips.get(1).path("nextNodeCode").asText());
    }

    private JsonNode loadFlowDefinition() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = EditorialReviewWorkflowDefinitionTest.class.getClassLoader()
            .getResourceAsStream("flow/editorial_review_flow.json")) {
            assertNotNull(inputStream, "editorial_review_flow.json must exist");
            return objectMapper.readTree(inputStream);
        }
    }

    private JsonNode requireNode(JsonNode nodeList, String nodeCode) {
        for (JsonNode node : nodeList) {
            if (nodeCode.equals(node.path("nodeCode").asText())) {
                return node;
            }
        }
        throw new AssertionError("Missing node: " + nodeCode);
    }
}
