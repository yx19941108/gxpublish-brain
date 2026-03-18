package com.gxpublish.brain.editorial.controller;

import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class EditorialReviewControllerContractTest {

    @Test
    void shouldExposeResubmitEndpoint() throws Exception {
        Method method = EditorialReviewController.class.getMethod("resubmit", EditorialReviewBo.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);

        assertNotNull(postMapping);
        assertTrue(Arrays.stream(postMapping.value()).anyMatch("/resubmit"::equals)
                || Arrays.stream(postMapping.path()).anyMatch("/resubmit"::equals));
    }
}
