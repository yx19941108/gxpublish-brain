package com.gxpublish.brain.manuscript.review.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.common.mybatis.core.mapper.BaseMapperPlus;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;

public interface ManuscriptReviewRecordMapper extends BaseMapperPlus<ManuscriptReviewRecordEntity, ManuscriptReviewRecordEntity> {

    List<Long> selectInitiatedReviewIds(@Param("currentUserId") Long currentUserId);

    List<String> selectWaitingBusinessIds(@Param("currentUserId") Long currentUserId);

    List<String> selectFinishedBusinessIds(@Param("currentUserId") Long currentUserId);

    Page<ManuscriptReviewRecordEntity> customSelectVisibleLedgerPage(@Param("page") Page<ManuscriptReviewRecordEntity> page,
                                                                     @Param("query") ManuscriptReviewLedgerQueryRequest query,
                                                                     @Param("visibleReviewIds") List<Long> visibleReviewIds,
                                                                     @Param("startTimeFrom") Date startTimeFrom,
                                                                     @Param("startTimeTo") Date startTimeTo);
}
