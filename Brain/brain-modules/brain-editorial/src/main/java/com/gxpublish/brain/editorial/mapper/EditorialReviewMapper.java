package com.gxpublish.brain.editorial.mapper;

import com.gxpublish.brain.common.mybatis.core.mapper.BaseMapperPlus;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewVo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 编辑部审校Mapper接口
 *
 * @author gxpublish
 */
public interface EditorialReviewMapper extends BaseMapperPlus<EditorialReview, EditorialReviewVo> {

    /**
     * 自定义分页查询（支持XML动态SQL作用域过滤）
     */
    Page<EditorialReviewVo> customSelectPage(@Param("page") Page<EditorialReview> page,
            @Param("bo") EditorialReviewBo bo,
            @Param("params") Map<String, Object> params);

    /**
     * 自定义列表查询（支持XML动态SQL作用域过滤）
     */
    List<EditorialReviewVo> customSelectList(@Param("bo") EditorialReviewBo bo,
            @Param("params") Map<String, Object> params);
}
