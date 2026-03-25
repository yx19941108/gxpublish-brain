package com.gxpublish.brain.manuscript.review.gateway.impl;

import java.util.Date;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSerialEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSerialMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TableBackedManuscriptReviewSerialGateway implements ManuscriptReviewSerialGateway {

    private static final String DEFAULT_TENANT_ID = "000000";

    private final ManuscriptReviewSerialMapper serialMapper;
    private final ManuscriptReviewCurrentUserGateway currentUserGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int nextSerial(ManuscriptReviewProcessType processType, String businessDate) {
        String tenantId = normalizeTenantId(currentUserGateway.getCurrentTenantId());
        if (tryInsertFirstSerial(tenantId, processType, businessDate)) {
            return 1;
        }
        return incrementExistingSerial(tenantId, processType, businessDate);
    }

    private boolean tryInsertFirstSerial(String tenantId, ManuscriptReviewProcessType processType, String businessDate) {
        ManuscriptReviewSerialEntity existing = serialMapper.selectOne(buildUniqueQuery(tenantId, processType, businessDate));
        if (existing != null) {
            return false;
        }

        ManuscriptReviewSerialEntity entity = new ManuscriptReviewSerialEntity();
        entity.setId(IdWorker.getId());
        entity.setTenantId(tenantId);
        entity.setProcessType(processType.name());
        entity.setBizDate(businessDate);
        entity.setCurrentSerial(1);
        entity.setUpdateTime(new Date());
        try {
            return serialMapper.insert(entity) > 0;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    private int incrementExistingSerial(String tenantId, ManuscriptReviewProcessType processType, String businessDate) {
        serialMapper.update(
            null,
            new UpdateWrapper<ManuscriptReviewSerialEntity>()
                .setSql("current_serial = current_serial + 1")
                .set("update_time", new Date())
                .eq("tenant_id", tenantId)
                .eq("process_type", processType.name())
                .eq("biz_date", businessDate)
        );
        ManuscriptReviewSerialEntity refreshed = serialMapper.selectOne(buildUniqueQuery(tenantId, processType, businessDate));
        return refreshed == null || refreshed.getCurrentSerial() == null ? 1 : refreshed.getCurrentSerial();
    }

    private QueryWrapper<ManuscriptReviewSerialEntity> buildUniqueQuery(String tenantId,
                                                                        ManuscriptReviewProcessType processType,
                                                                        String businessDate) {
        return new QueryWrapper<ManuscriptReviewSerialEntity>()
            .eq("tenant_id", tenantId)
            .eq("process_type", processType.name())
            .eq("biz_date", businessDate)
            .last("limit 1");
    }

    private String normalizeTenantId(String tenantId) {
        if (tenantId == null) {
            return DEFAULT_TENANT_ID;
        }
        String normalized = tenantId.trim();
        return normalized.isEmpty() ? DEFAULT_TENANT_ID : normalized;
    }
}
