package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;

@Tag("dev")
class ManuscriptReviewReadableFirstRoundGuardrailTest {

    @Test
    void shouldAvoidFullTableRecordAndHistoryScansWhenListingLedger() {
        ReadableFixture fixture = new ReadableFixture(4001L);
        when(fixture.recordMapper.selectWaitingBusinessIds(4001L)).thenReturn(List.of());
        when(fixture.recordMapper.selectFinishedBusinessIds(4001L)).thenReturn(List.of());

        TableDataInfo<ManuscriptReviewLedgerItemResponse> ledger = fixture.readableService.listLedger(new ManuscriptReviewLedgerQueryRequest());

        assertEquals(0, ledger.getTotal());
        assertTrue(ledger.getRows().isEmpty());
        verify(fixture.recordMapper, never()).selectList(any());
        verify(fixture.recordMapper, never()).customSelectVisibleLedgerPage(any(), any(), any(), any(), any());
        verify(fixture.historyMapper, never()).selectList(any());
    }

    private static final class ReadableFixture {

        private final ManuscriptReviewRecordMapper recordMapper = mock(ManuscriptReviewRecordMapper.class);
        private final ManuscriptReviewAttachmentMapper attachmentMapper = mock(ManuscriptReviewAttachmentMapper.class);
        private final ManuscriptReviewExternalLinkMapper externalLinkMapper = mock(ManuscriptReviewExternalLinkMapper.class);
        private final ManuscriptReviewHistoryMapper historyMapper = mock(ManuscriptReviewHistoryMapper.class);
        private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper = mock(ManuscriptReviewVideoMarkerMapper.class);
        private final ManuscriptReviewFlowConfigMapper flowConfigMapper = mock(ManuscriptReviewFlowConfigMapper.class);
        private final ManuscriptReviewSystemRoleMapper roleMapper = mock(ManuscriptReviewSystemRoleMapper.class);
        private final ManuscriptReviewSystemUserRoleMapper userRoleMapper = mock(ManuscriptReviewSystemUserRoleMapper.class);
        private final ManuscriptReviewSystemUserMapper userMapper = mock(ManuscriptReviewSystemUserMapper.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        private final ManuscriptReviewReadableService readableService;

        private ReadableFixture(Long currentUserId) {
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            this.readableService = new ManuscriptReviewReadableService(
                recordMapper,
                attachmentMapper,
                externalLinkMapper,
                historyMapper,
                videoMarkerMapper,
                flowConfigMapper,
                roleMapper,
                userRoleMapper,
                userMapper,
                currentUserGateway
            );
        }
    }
}
