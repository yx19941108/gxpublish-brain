package com.gxpublish.brain.manuscript.review.gateway.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSerialEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSerialMapper;

@Tag("dev")
class TableBackedManuscriptReviewSerialGatewayTest {

    @Test
    void shouldStartSerialAtOneWhenNoDailyRowExists() {
        ManuscriptReviewSerialMapper serialMapper = mock(ManuscriptReviewSerialMapper.class);
        ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        when(currentUserGateway.getCurrentTenantId()).thenReturn("000000");
        when(serialMapper.selectOne(any())).thenReturn(null);
        when(serialMapper.insert(org.mockito.ArgumentMatchers.<ManuscriptReviewSerialEntity>any())).thenReturn(1);

        TableBackedManuscriptReviewSerialGateway gateway =
            new TableBackedManuscriptReviewSerialGateway(serialMapper, currentUserGateway);

        int serial = gateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260325");

        assertEquals(1, serial);
        ArgumentCaptor<ManuscriptReviewSerialEntity> insertCaptor = ArgumentCaptor.forClass(ManuscriptReviewSerialEntity.class);
        verify(serialMapper).insert(insertCaptor.capture());
        ManuscriptReviewSerialEntity inserted = insertCaptor.getValue();
        assertEquals("000000", inserted.getTenantId());
        assertEquals("AUDIT", inserted.getProcessType());
        assertEquals("20260325", inserted.getBizDate());
        assertEquals(1, inserted.getCurrentSerial());
        verify(serialMapper, never()).update(isNull(), any());
    }

    @Test
    void shouldIncrementSerialWhenDailyRowAlreadyExists() {
        ManuscriptReviewSerialMapper serialMapper = mock(ManuscriptReviewSerialMapper.class);
        ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        when(currentUserGateway.getCurrentTenantId()).thenReturn("000000");

        ManuscriptReviewSerialEntity existing = new ManuscriptReviewSerialEntity();
        existing.setId(60001L);
        existing.setTenantId("000000");
        existing.setProcessType("PROOFREAD");
        existing.setBizDate("20260325");
        existing.setCurrentSerial(7);

        ManuscriptReviewSerialEntity refreshed = new ManuscriptReviewSerialEntity();
        refreshed.setId(60001L);
        refreshed.setTenantId("000000");
        refreshed.setProcessType("PROOFREAD");
        refreshed.setBizDate("20260325");
        refreshed.setCurrentSerial(8);

        when(serialMapper.selectOne(any())).thenReturn(existing, refreshed);
        when(serialMapper.update(isNull(), any())).thenReturn(1);

        TableBackedManuscriptReviewSerialGateway gateway =
            new TableBackedManuscriptReviewSerialGateway(serialMapper, currentUserGateway);

        int serial = gateway.nextSerial(ManuscriptReviewProcessType.PROOFREAD, "20260325");

        assertEquals(8, serial);
        verify(serialMapper, never()).insert(org.mockito.ArgumentMatchers.<ManuscriptReviewSerialEntity>any());
        verify(serialMapper).update(isNull(), any());
    }
}
