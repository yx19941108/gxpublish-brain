package com.gxpublish.brain.editorial.service;

import com.gxpublish.brain.common.mybatis.core.page.PageQuery;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;

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
    EditorialReviewDetailVo queryById(Long id);

    /**
     * 查询审校申请列表
     */
    TableDataInfo<EditorialReviewPageItemVo> queryPageList(EditorialReviewBo bo, PageQuery pageQuery);

    /**
     * 查询审校申请列表
     */
    List<EditorialReviewPageItemVo> queryList(EditorialReviewBo bo);

    /**
     * 新增审校申请(保存草稿)
     */
    EditorialReviewDetailVo insertByBo(EditorialReviewBo bo);

    /**
     * 提交并开启流程
     */
    EditorialReviewDetailVo submitAndFlowStart(EditorialReviewBo bo);

    /**
     * 退回后重新提交并重启流程
     */
    EditorialReviewDetailVo resubmitAndFlowStart(EditorialReviewBo bo);

    /**
     * 修改审校申请
     */
    EditorialReviewDetailVo updateByBo(EditorialReviewBo bo);

    /**
     * 校验并批量删除审校申请信息
     */
    Boolean deleteWithValidByIds(List<Long> ids);
    
    /**
     * 查询历史记录
     */
    List<EditorialHistoryVo> queryHistoryList(Long reviewId);

}
