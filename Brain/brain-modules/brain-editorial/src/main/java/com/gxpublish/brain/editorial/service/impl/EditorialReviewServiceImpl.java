package com.gxpublish.brain.editorial.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
import com.gxpublish.brain.common.core.domain.event.ProcessDeleteEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessTaskEvent;
import com.gxpublish.brain.common.core.enums.BusinessStatusEnum;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.common.core.utils.MapstructUtils;
import com.gxpublish.brain.common.core.utils.StreamUtils;
import com.gxpublish.brain.common.core.utils.StringUtils;
import com.gxpublish.brain.common.mybatis.core.page.PageQuery;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import com.gxpublish.brain.editorial.domain.EditorialAttachment;
import com.gxpublish.brain.editorial.domain.EditorialHistory;
import com.gxpublish.brain.editorial.domain.EditorialLink;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.bo.EditorialLinkBo;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewVo;
import com.gxpublish.brain.editorial.mapper.EditorialAttachmentMapper;
import com.gxpublish.brain.editorial.mapper.EditorialHistoryMapper;
import com.gxpublish.brain.editorial.mapper.EditorialLinkMapper;
import com.gxpublish.brain.editorial.mapper.EditorialReviewMapper;
import com.gxpublish.brain.editorial.service.IEditorialReviewService;
import com.gxpublish.brain.editorial.domain.param.EditorialScopeParam;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import com.gxpublish.brain.editorial.service.strategy.EditorialDataScopeFactory;
import com.gxpublish.brain.workflow.common.constant.FlowConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 编辑部审校Service业务层处理
 *
 * @author gxpublish
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class EditorialReviewServiceImpl implements IEditorialReviewService {

    public static final String HAS_CERTIFICATE_APPLICANT_ROLE_KEY = "editorial_review_applicant_has_certificate";
    public static final String EDITORIAL_REVIEW_FLOW_CODE = "editorial_review_flow";
    private static final Set<String> FIRST_REVIEW_NODE_CODES = Set.of(
            "1b2489b3-db28-4b57-82c1-3fd1c8ae1d59",
            "dept-audit-node",
            "first-audit-node",
            "first-review-node",
            "level1-audit-node");
    private static final Set<String> SECOND_REVIEW_NODE_CODES = Set.of(
            "ee5f5403-21c2-49e8-80b1-3c72ddad0148",
            "second-audit-node",
            "second-review-node",
            "level2-audit-node");
    private static final Set<String> THIRD_REVIEW_NODE_CODES = Set.of(
            "2583d1cb-2312-4f41-9cfc-35784c59330a",
            "final-audit-node",
            "third-audit-node",
            "third-review-node",
            "level3-audit-node");
    private final EditorialReviewMapper baseMapper;
    private final EditorialAttachmentMapper attachmentMapper;
    private final EditorialLinkMapper linkMapper;
    private final EditorialHistoryMapper historyMapper;
    private final WorkflowService workflowService;
    private final EditorialDataScopeFactory dataScopeFactory;

    @Override
    public EditorialReviewVo queryById(Long id) {
        EditorialReviewVo vo = baseMapper.selectVoById(id);
        if (vo != null) {
            // 查询当前附件
            if (vo.getCurrentAttachmentId() != null) {
                vo.setAttachment(attachmentMapper.selectVoById(vo.getCurrentAttachmentId()));
            }
            // 查询关联链接
            vo.setLinkList(linkMapper.selectVoList(
                    new LambdaQueryWrapper<EditorialLink>().eq(EditorialLink::getReviewId, id)));
        }
        return vo;
    }

    @Override
    public TableDataInfo<EditorialReviewVo> queryPageList(EditorialReviewBo bo, PageQuery pageQuery) {
        EditorialScopeParam scopeParam = dataScopeFactory.buildScopeParams();
        Map<String, Object> params = new HashMap<>();
        params.put("scopeParam", scopeParam);

        Page<EditorialReviewVo> result = baseMapper.customSelectPage(pageQuery.build(), bo, params);
        return TableDataInfo.build(result);
    }

    @Override
    public List<EditorialReviewVo> queryList(EditorialReviewBo bo) {
        EditorialScopeParam scopeParam = dataScopeFactory.buildScopeParams();
        Map<String, Object> params = new HashMap<>();
        params.put("scopeParam", scopeParam);

        return baseMapper.customSelectList(bo, params);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewVo insertByBo(EditorialReviewBo bo) {
        EditorialReview add = MapstructUtils.convert(bo, EditorialReview.class);
        // 新增时强制使用枚举小写值，避免前端传入大写导致后续比较失败
        add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        add.setReviewStatus(ReviewStatusEnum.DRAFT.getCode());
        // 设置发起人
        if (add.getUserId() == null) {
            add.setUserId(LoginHelper.getUserId());
        }

        baseMapper.insert(add);
        bo.setId(add.getId());

        // 处理附件
        handleAttachment(bo, add.getId());

        // 处理链接
        handleLinks(bo.getLinkList(), add.getId());

        return queryById(add.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewVo submitAndFlowStart(EditorialReviewBo bo) {
        if (bo.getId() != null) {
            EditorialReview existing = baseMapper.selectById(bo.getId());
            if (BusinessStatusEnum.BACK.getStatus().equals(existing.getStatus())
                    || ReviewStatusEnum.BACK.getCode().equals(existing.getReviewStatus())) {
                throw new ServiceException("当前申请已被退回，请通过审批组件办理重新提交");
            }
            if (!BusinessStatusEnum.DRAFT.getStatus().equals(existing.getStatus())
                    && !ReviewStatusEnum.DRAFT.getCode().equals(existing.getReviewStatus())) {
                throw new ServiceException("只有草稿状态可发起审批");
            }
            updateByBo(bo);
        } else {
            insertByBo(bo);
        }

        EditorialReview review = baseMapper.selectById(bo.getId());

        // 判断发起人是否有证
        boolean isCertified = false;
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser != null && CollUtil.isNotEmpty(loginUser.getRoles())) {
            isCertified = loginUser.getRoles().stream()
                    .anyMatch(role -> HAS_CERTIFICATE_APPLICANT_ROLE_KEY.equals(role.getRoleKey()));
        }

        // 发起流程
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setBusinessId(review.getId().toString());
        if (StringUtils.isBlank(bo.getFlowCode())) {
            throw new ServiceException("流程定义编码不能为空");
        }
        startProcess.setFlowCode(bo.getFlowCode());
        Map<String, Object> variables = new HashMap<>();
        variables.put("ignore", true);
        variables.put("isCertified", isCertified);
        startProcess.setVariables(variables); // 忽略权限校验, 传入有证标记

        boolean flag = workflowService.startCompleteTask(startProcess);
        if (!flag) {
            throw new ServiceException("流程发起异常");
        }

        // 如果该发起人属于“有证”用户，系统自动跳过一级审批时，留下一条通过记录
        if (isCertified) {
            EditorialHistory history = new EditorialHistory();
            history.setReviewId(review.getId());
            if (loginUser != null) {
                history.setOperatorId(loginUser.getUserId());
                history.setOperatorName(loginUser.getUsername());
            }
            history.setOperateTime(new Date());
            history.setOperateType("自动跳过");

            Map<String, Object> diff = new HashMap<>();
            diff.put("action", "发起人持证自动通过一级审批");
            history.setFieldDiff(diff);

            historyMapper.insert(history);
        }

        return queryById(review.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewVo updateByBo(EditorialReviewBo bo) {
        EditorialReview oldReview = baseMapper.selectById(bo.getId());
        if (oldReview == null) {
            throw new ServiceException("申请不存在");
        }

        // 校验：仅草稿和退回状态允许修改表单内容
        if (!BusinessStatusEnum.DRAFT.getStatus().equals(oldReview.getStatus())
                && !BusinessStatusEnum.BACK.getStatus().equals(oldReview.getStatus())
                && !ReviewStatusEnum.DRAFT.getCode().equals(oldReview.getReviewStatus())
                && !ReviewStatusEnum.BACK.getCode().equals(oldReview.getReviewStatus())) {
            throw new ServiceException("当前流程状态为审批中或已结束，不允许修改申请内容");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (!currentUserId.equals(oldReview.getCreateBy()) && !LoginHelper.isSuperAdmin()) {
            throw new ServiceException("只有发起人可以修改表单内容");
        }

        EditorialReview update = MapstructUtils.convert(bo, EditorialReview.class);

        // 处理附件版本
        boolean attachmentChanged = handleAttachment(bo, bo.getId());
        if (attachmentChanged) {
            update.setCurrentAttachmentId(null); // 指示重新查询或逻辑需调整
            // 重新获取最新的 attachmentId 从 DB (或者 handleAttachment 返回 ID)
            // 这里简化：handleAttachment 内部已经更新了 review 表的 currentAttachmentId 如果变化
            EditorialReview refreshed = baseMapper.selectById(bo.getId());
            update.setCurrentAttachmentId(refreshed.getCurrentAttachmentId());
        }

        // 如果处于审批中 (WAITING)，记录 diff
        if (BusinessStatusEnum.WAITING.getStatus().equals(oldReview.getStatus())) {
            recordHistory(oldReview, update, bo.getLinkList(), attachmentChanged);
        }

        baseMapper.updateById(update);

        // 处理链接 (全量替换)
        handleLinks(bo.getLinkList(), bo.getId());

        return queryById(bo.getId());
    }

    private boolean handleAttachment(EditorialReviewBo bo, Long reviewId) {
        if (StringUtils.isBlank(bo.getAttachmentOssId())) {
            // 如果没传附件，判断是否需要清理
            EditorialReview review = baseMapper.selectById(reviewId);
            if (review.getCurrentAttachmentId() != null) {
                LambdaQueryWrapper<EditorialAttachment> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(EditorialAttachment::getReviewId, reviewId);
                attachmentMapper.delete(wrapper);
                review.setCurrentAttachmentId(null);
                baseMapper.updateById(review);
                return true;
            }
            return false;
        }

        EditorialReview review = baseMapper.selectById(reviewId);
        Long currentAttachmentId = review.getCurrentAttachmentId();
        String currentOssId = null;
        int currentVersion = 0;

        if (currentAttachmentId != null) {
            EditorialAttachment currentAttachment = attachmentMapper.selectById(currentAttachmentId);
            if (currentAttachment != null) {
                currentOssId = currentAttachment.getOssId();
                currentVersion = currentAttachment.getVersion() != null ? currentAttachment.getVersion() : 0;
            }
        }

        // 如果 OSS ID 不同，说明上传了新文件
        if (!StringUtils.equals(bo.getAttachmentOssId(), currentOssId)) {
            // 不保留历史附件的话，先删除旧的
            LambdaQueryWrapper<EditorialAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EditorialAttachment::getReviewId, reviewId);
            attachmentMapper.delete(wrapper);

            EditorialAttachment newAttachment = new EditorialAttachment();
            newAttachment.setReviewId(reviewId);
            newAttachment.setOssId(bo.getAttachmentOssId());
            newAttachment.setFileName(bo.getAttachmentFileName());
            if (StringUtils.isNotBlank(bo.getAttachmentFileUrl())) {
                newAttachment.setFileUrl(bo.getAttachmentFileUrl());
            }
            if (bo.getAttachmentFileSize() != null) {
                newAttachment.setFileSize(bo.getAttachmentFileSize());
            }
            newAttachment.setVersion(currentVersion + 1);
            newAttachment.setUploaderId(LoginHelper.getUserId());
            newAttachment.setCreateTime(new Date());

            attachmentMapper.insert(newAttachment);

            // 更新主表 currentAttachmentId
            review.setCurrentAttachmentId(newAttachment.getId());
            baseMapper.updateById(review);
            return true;
        }
        return false;
    }

    private void handleLinks(List<EditorialLinkBo> linkBoList, Long reviewId) {
        if (linkBoList == null)
            return;

        // 删除旧的 (简单粗暴全量替换，或根据 ID 更新)
        // 这里采用保留 ID 的更新+新增+删除
        List<Long> inputIds = linkBoList.stream().map(EditorialLinkBo::getId).filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(inputIds)) {
            linkMapper.delete(new LambdaQueryWrapper<EditorialLink>()
                    .eq(EditorialLink::getReviewId, reviewId)
                    .notIn(EditorialLink::getId, inputIds));
        } else {
            linkMapper.delete(new LambdaQueryWrapper<EditorialLink>().eq(EditorialLink::getReviewId, reviewId));
        }

        for (EditorialLinkBo linkBo : linkBoList) {
            EditorialLink link = MapstructUtils.convert(linkBo, EditorialLink.class);
            link.setReviewId(reviewId);
            if (link.getId() == null) {
                linkMapper.insert(link);
            } else {
                linkMapper.updateById(link);
            }
        }
    }

    private void recordHistory(EditorialReview oldVal, EditorialReview newVal, List<EditorialLinkBo> links,
            boolean attachmentChanged) {
        Map<String, Object> diff = new HashMap<>();

        if (!StringUtils.equals(oldVal.getTitle(), newVal.getTitle())) {
            diff.put("title", Map.of("old", oldVal.getTitle(), "new", newVal.getTitle()));
        }
        if (!StringUtils.equals(oldVal.getContent(), newVal.getContent())) {
            diff.put("content", Map.of("old", "...", "new", "...")); // 内容太长不全存
        }
        if (attachmentChanged) {
            diff.put("attachment", "Version updated");
        }
        // Links diff logic omitted for brevity

        if (!diff.isEmpty()) {
            EditorialHistory history = new EditorialHistory();
            history.setReviewId(oldVal.getId());
            history.setOperatorId(LoginHelper.getUserId());
            try {
                history.setOperatorName(LoginHelper.getUsername()); // 可能为空
            } catch (Exception e) {
            }
            history.setOperateTime(new Date());
            history.setOperateType("MODIFY");
            history.setFieldDiff(diff);
            historyMapper.insert(history);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteWithValidByIds(List<Long> ids) {
        workflowService.deleteInstance(StreamUtils.toList(ids, Convert::toStr));
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<EditorialHistoryVo> queryHistoryList(Long reviewId) {
        return historyMapper.selectVoList(
                new LambdaQueryWrapper<EditorialHistory>()
                        .eq(EditorialHistory::getReviewId, reviewId)
                        .orderByDesc(EditorialHistory::getOperateTime));
    }

    @EventListener(condition = "#processEvent.flowCode.startsWith('editorial_review')")
    public void processHandler(ProcessEvent processEvent) {
        log.info("审校流程事件: {}", processEvent);
        EditorialReview review = baseMapper.selectById(Convert.toLong(processEvent.getBusinessId()));
        if (review != null) {
            boolean isSubmit = Boolean.TRUE.equals(processEvent.getSubmit());
            String newStatus = processEvent.getStatus();

            if (isSubmit) {
                if (StringUtils.isBlank(review.getApplyCode())) {
                    review.setApplyCode(Convert.toStr(processEvent.getParams().get(FlowConstant.BUSINESS_CODE)));
                }
                review.setStatus(BusinessStatusEnum.WAITING.getStatus());
            } else if (StringUtils.isNotBlank(newStatus)) {
                review.setStatus(newStatus);
            }

            // 流程终态与提交初始化在 ProcessEvent 同步，中间态交给 ProcessTaskEvent 同步
            if (BusinessStatusEnum.BACK.getStatus().equals(review.getStatus())) {
                review.setReviewStatus(ReviewStatusEnum.BACK.getCode());
            } else if (BusinessStatusEnum.CANCEL.getStatus().equals(review.getStatus())) {
                review.setReviewStatus(ReviewStatusEnum.CANCELED.getCode());
            } else if (BusinessStatusEnum.FINISH.getStatus().equals(review.getStatus())) {
                review.setReviewStatus(ReviewStatusEnum.APPROVED.getCode());
            } else if (BusinessStatusEnum.TERMINATION.getStatus().equals(review.getStatus())) {
                review.setReviewStatus(ReviewStatusEnum.TERMINATED.getCode());
            } else if (isSubmit && BusinessStatusEnum.WAITING.getStatus().equals(review.getStatus())) {
                review.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
            }

            baseMapper.updateById(review);

            // 记录审批历史
            EditorialHistory history = new EditorialHistory();
            history.setReviewId(review.getId());
            try {
                history.setOperatorId(LoginHelper.getUserId());
                history.setOperatorName(LoginHelper.getUsername());
            } catch (Exception e) {
                log.warn("获取历史操作人信息失败: {}", e.getMessage());
            }
            history.setOperateTime(new Date());

            String opType = "FLOW_NODE";
            if (isSubmit) {
                opType = "发起审批";
            } else if (StringUtils.isNotBlank(newStatus)) {
                opType = "FLOW_" + newStatus;
            } else {
                opType = "流程流转";
            }
            history.setOperateType(opType);
            historyMapper.insert(history);
        }
    }

    @EventListener(condition = "#processTaskEvent.flowCode.startsWith('editorial_review')")
    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
        log.info("审校流程任务事件: {}", processTaskEvent);
        EditorialReview review = baseMapper.selectById(Convert.toLong(processTaskEvent.getBusinessId()));
        if (review == null) {
            return;
        }
        if (!BusinessStatusEnum.WAITING.getStatus().equals(review.getStatus())) {
            return;
        }
        Integer reviewStatus = resolveWaitingReviewStatus(processTaskEvent.getNodeCode(), processTaskEvent.getNodeName());
        if (reviewStatus == null || Objects.equals(reviewStatus, review.getReviewStatus())) {
            return;
        }
        review.setReviewStatus(reviewStatus);
        baseMapper.updateById(review);
    }

    private Integer resolveWaitingReviewStatus(String nodeCode, String nodeName) {
        String normalizedCode = StringUtils.lowerCase(StringUtils.defaultString(nodeCode));
        String normalizedName = StringUtils.deleteWhitespace(StringUtils.defaultString(nodeName));

        if (FIRST_REVIEW_NODE_CODES.contains(normalizedCode)
                || StringUtils.containsAny(normalizedCode, "first", "level1", "dept-audit")
                || StringUtils.containsAnyIgnoreCase(normalizedName, "一审", "一级", "初审", "部门经理审批")) {
            return ReviewStatusEnum.WAITING_FIRST.getCode();
        }
        if (SECOND_REVIEW_NODE_CODES.contains(normalizedCode)
                || StringUtils.containsAny(normalizedCode, "second", "level2")
                || StringUtils.containsAnyIgnoreCase(normalizedName, "二审", "二级", "复审")) {
            return ReviewStatusEnum.WAITING_SECOND.getCode();
        }
        if (THIRD_REVIEW_NODE_CODES.contains(normalizedCode)
                || StringUtils.containsAny(normalizedCode, "third", "level3", "final")
                || StringUtils.containsAnyIgnoreCase(normalizedName, "三审", "三级", "终审", "总编室审批", "社领导审批")) {
            return ReviewStatusEnum.WAITING_FINAL.getCode();
        }
        return null;
    }

    @EventListener(condition = "#processDeleteEvent.flowCode.startsWith('editorial_review')")
    public void processDeleteHandler(ProcessDeleteEvent processDeleteEvent) {
        log.info("审校流程删除: {}", processDeleteEvent);
        EditorialReview review = baseMapper.selectById(Convert.toLong(processDeleteEvent.getBusinessId()));
        if (review != null) {
            baseMapper.deleteById(review.getId());
        }
    }
}
