package com.gxpublish.brain.manuscript.review.mapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

@Tag("dev")
class ManuscriptReviewRecordMapperXmlGuardrailTest {

    private static final String RESOURCE_PATH = "mapper/manuscript/review/ManuscriptReviewRecordMapper.xml";
    private static final String EXPECTED_FLOW_CODE_FILTER =
        "d.flow_code IN ('manuscript_review_audit_flow', 'manuscript_review_proofread_flow')";

    @Test
    void shouldKeepWorkflowVisibilityQueriesScopedToPublishedManuscriptReviewFlows() throws IOException {
        String xml = readMapperXml();
        String waitingBlock = extractSelectBlock(xml, "selectWaitingBusinessIds");
        String finishedBlock = extractSelectBlock(xml, "selectFinishedBusinessIds");

        assertTrue(waitingBlock.contains(EXPECTED_FLOW_CODE_FILTER));
        assertTrue(finishedBlock.contains(EXPECTED_FLOW_CODE_FILTER));
        assertFalse(waitingBlock.contains("d.flow_code = 'manuscript_review_'"));
        assertFalse(finishedBlock.contains("d.flow_code = 'manuscript_review_'"));
    }

    private String readMapperXml() throws IOException {
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extractSelectBlock(String xml, String selectId) {
        Matcher matcher = Pattern.compile("<select id=\"" + selectId + "\"[\\s\\S]*?</select>").matcher(xml);
        assertTrue(matcher.find(), "missing select block: " + selectId);
        String block = matcher.group();
        assertNotNull(block);
        return block.replaceAll("\\s+", " ").trim();
    }
}
