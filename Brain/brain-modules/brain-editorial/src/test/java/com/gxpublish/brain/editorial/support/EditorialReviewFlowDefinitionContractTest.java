package com.gxpublish.brain.editorial.support;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("dev")
class EditorialReviewFlowDefinitionContractTest {

    @Test
    void shouldNotExposeTransferButtonForEditorialWorkflow() throws IOException {
        var inputStream = EditorialReviewFlowDefinitionContractTest.class.getClassLoader()
            .getResourceAsStream("flow/editorial_review_flow.json");
        assertNotNull(inputStream);
        String flowDefinition = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertFalse(flowDefinition.contains("\"transfer\""));
    }
}
