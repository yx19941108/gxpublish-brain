package com.gxpublish.brain.manuscript.review.service;

import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.SubmitAndStartManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.UpdateManuscriptReviewCommand;

/**
 * 稿件审校写侧服务接口。
 *
 * <p>v6.26 追加改动：为对齐 brain-system 常见结构，补充 service 接口，
 * 并将“新增并提交一体化”作为主写入能力暴露给控制层。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
public interface IManuscriptReviewService {

    /**
     * 新增草稿主单。
     *
     * <p>v6.26 追加改动：该方法仅保留最小兼容能力，不再作为新增页正式提交主链路。</p>
     *
     * @param command 主单新增命令，承载主表基础字段
     * @return 新建流程单主键
     */
    Long create(CreateManuscriptReviewCommand command);

    /**
     * 修改现有流程单。
     *
     * @param command 主单修改命令
     * @return 无返回值
     */
    void update(UpdateManuscriptReviewCommand command);

    /**
     * 兼容旧草稿记录的提交发起能力。
     *
     * <p>v6.26 追加改动：仅用于边界内最小兼容，不再作为新增页正式提交入口。</p>
     *
     * @param reviewId 已存在流程单主键
     * @return 系统稿件号
     */
    String submitAndFlowStart(Long reviewId);

    /**
     * 新增并提交一体化事务入口。
     *
     * @param command 新增并提交命令，包含主表字段、附件/视频参数、外链参数
     * @return 系统稿件号
     */
    Long submitAndFlowStart(SubmitAndStartManuscriptReviewCommand command);

    /**
     * 发起人修改后再次提交。
     *
     * @param command 再次提交命令
     * @return 无返回值
     */
    void resubmit(ResubmitManuscriptReviewCommand command);

    /**
     * 发起人撤销流程。
     *
     * @param reviewId 流程单主键
     * @param reason 撤销原因
     * @return 无返回值
     */
    void cancelProcessApply(Long reviewId, String reason);

    /**
     * 追加资源。
     *
     * @param command 资源新增命令
     * @return 资源主键
     */
    Long addResource(AddManuscriptReviewResourceCommand command);

    /**
     * 停用资源。
     *
     * @param command 资源停用命令
     * @return 无返回值
     */
    void disableResource(DisableManuscriptReviewResourceCommand command);

    /**
     * 追加视频标注。
     *
     * @param command 标注新增命令
     * @return 标注主键
     */
    Long addVideoMark(AddManuscriptReviewVideoMarkCommand command);

    /**
     * 停用视频标注。
     *
     * @param command 标注停用命令
     * @return 无返回值
     */
    void disableVideoMark(DisableManuscriptReviewVideoMarkCommand command);
}
