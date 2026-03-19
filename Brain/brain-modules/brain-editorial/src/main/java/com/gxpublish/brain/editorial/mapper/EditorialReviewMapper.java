package com.gxpublish.brain.editorial.mapper;

import com.gxpublish.brain.common.mybatis.core.mapper.BaseMapperPlus;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewTaskContextVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 编辑部审校Mapper接口
 *
 * @author gxpublish
 */
public interface EditorialReviewMapper extends BaseMapperPlus<EditorialReview, EditorialReviewDetailVo> {

    /**
     * 自定义分页查询（支持XML动态SQL作用域过滤）
     */
    Page<EditorialReviewPageItemVo> customSelectPage(@Param("page") Page<EditorialReview> page,
                                                     @Param("bo") EditorialReviewBo bo,
                                                     @Param("params") Map<String, Object> params);

    /**
     * 自定义列表查询（支持XML动态SQL作用域过滤）
     */
    List<EditorialReviewPageItemVo> customSelectList(@Param("bo") EditorialReviewBo bo,
                                                     @Param("params") Map<String, Object> params);

    /**
     * 查询当前登录人在指定业务单据上的待办任务上下文。
     */
    EditorialReviewTaskContextVo selectCurrentTaskContext(@Param("reviewId") String reviewId,
                                                          @Param("processedBy") String processedBy);

    /**
     * 批量查询当前登录人在指定业务单据上的待办任务上下文。
     */
    List<EditorialReviewTaskContextVo> selectCurrentTaskContexts(@Param("reviewIds") List<String> reviewIds,
                                                                 @Param("processedBy") String processedBy);
}
