package com.gxpublish.brain.manuscript.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;
import com.gxpublish.brain.workflow.service.IFlwInstanceService;

@Tag("dev")
class ManuscriptReviewServiceSpringConstructorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ManuscriptReviewRecordMapper.class, () -> mock(ManuscriptReviewRecordMapper.class))
        .withBean(ManuscriptReviewAttachmentMapper.class, () -> mock(ManuscriptReviewAttachmentMapper.class))
        .withBean(ManuscriptReviewExternalLinkMapper.class, () -> mock(ManuscriptReviewExternalLinkMapper.class))
        .withBean(ManuscriptReviewVideoMarkerMapper.class, () -> mock(ManuscriptReviewVideoMarkerMapper.class))
        .withBean(ManuscriptReviewHistoryMapper.class, () -> mock(ManuscriptReviewHistoryMapper.class))
        .withBean(ManuscriptReviewFlowConfigMapper.class, () -> mock(ManuscriptReviewFlowConfigMapper.class))
        .withBean(ManuscriptReviewSystemRoleMapper.class, () -> mock(ManuscriptReviewSystemRoleMapper.class))
        .withBean(ManuscriptReviewSystemUserRoleMapper.class, () -> mock(ManuscriptReviewSystemUserRoleMapper.class))
        .withBean(ManuscriptReviewSystemUserMapper.class, () -> mock(ManuscriptReviewSystemUserMapper.class))
        .withBean(ManuscriptReviewSerialGateway.class, () -> mock(ManuscriptReviewSerialGateway.class))
        .withBean(ManuscriptReviewCurrentUserGateway.class, () -> mock(ManuscriptReviewCurrentUserGateway.class))
        .withBean(WorkflowService.class, () -> mock(WorkflowService.class))
        .withBean(IFlwInstanceService.class, () -> mock(IFlwInstanceService.class))
        .withBean(ManuscriptReviewService.class);

    @Test
    void shouldCreateServiceBeanWithSingleRuntimeAutowiredConstructor() {
        contextRunner.run(context -> {
            assertThat(context.getStartupFailure()).isNull();
            assertThat(context.getBeanNamesForType(ManuscriptReviewService.class)).hasSize(1);
        });
    }
}
