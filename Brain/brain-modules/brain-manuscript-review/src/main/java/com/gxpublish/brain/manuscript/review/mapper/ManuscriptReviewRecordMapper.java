package com.gxpublish.brain.manuscript.review.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.common.mybatis.core.mapper.BaseMapperPlus;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;

public interface ManuscriptReviewRecordMapper extends BaseMapperPlus<ManuscriptReviewRecordEntity, ManuscriptReviewRecordEntity> {

    Page<ManuscriptReviewRecordEntity> customSelectVisibleLedgerPage(@Param("page") Page<ManuscriptReviewRecordEntity> page,
                                                                     @Param("query") ManuscriptReviewLedgerQueryRequest query,
                                                                     @Param("currentUserId") Long currentUserId,
                                                                     @Param("allowInitiator") boolean allowInitiator,
                                                                     @Param("approverNodeStatuses") List<String> approverNodeStatuses,
                                                                     @Param("startTimeFrom") Date startTimeFrom,
                                                                     @Param("startTimeTo") Date startTimeTo);
}
