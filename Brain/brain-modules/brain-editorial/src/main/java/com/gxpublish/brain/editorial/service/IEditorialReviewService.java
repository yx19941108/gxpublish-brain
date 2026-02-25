package com.gxpublish.brain.editorial.service;

import com.gxpublish.brain.common.mybatis.core.page.PageQuery;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewVo;

import java.util.List;

/**
 * 编辑部审校Service接口
 *
 * @author gxpublish
 */
public interface IEditorialReviewService {

    /**
     * 查询审校申请
     */
    EditorialReviewVo queryById(Long id);

    /**
     * 查询审校申请列表
     */
    TableDataInfo<EditorialReviewVo> queryPageList(EditorialReviewBo bo, PageQuery pageQuery);

    /**
     * 查询审校申请列表
     */
    List<EditorialReviewVo> queryList(EditorialReviewBo bo);

    /**
     * 新增审校申请(保存草稿)
     */
    EditorialReviewVo insertByBo(EditorialReviewBo bo);

    /**
     * 提交并开启流程
     */
    EditorialReviewVo submitAndFlowStart(EditorialReviewBo bo);

    /**
     * 修改审校申请
     */
    EditorialReviewVo updateByBo(EditorialReviewBo bo);

    /**
     * 校验并批量删除审校申请信息
     */
    Boolean deleteWithValidByIds(List<Long> ids);
    
    /**
     * 查询历史记录
     */
    List<EditorialHistoryVo> queryHistoryList(Long reviewId);

}
