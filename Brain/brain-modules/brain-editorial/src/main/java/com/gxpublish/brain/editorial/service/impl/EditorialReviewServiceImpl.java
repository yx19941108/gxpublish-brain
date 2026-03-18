package com.gxpublish.brain.editorial.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessDeleteEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessTaskEvent;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
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
import com.gxpublish.brain.editorial.domain.param.EditorialScopeParam;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;
import com.gxpublish.brain.editorial.enums.EditorialRoleEnum;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import com.gxpublish.brain.editorial.mapper.EditorialAttachmentMapper;
import com.gxpublish.brain.editorial.mapper.EditorialHistoryMapper;
import com.gxpublish.brain.editorial.mapper.EditorialLinkMapper;
import com.gxpublish.brain.editorial.mapper.EditorialReviewMapper;
import com.gxpublish.brain.editorial.mapper.EditorialWorkflowRoleMapper;
import com.gxpublish.brain.editorial.service.IEditorialReviewService;
import com.gxpublish.brain.editorial.service.strategy.EditorialDataScopeFactory;
import com.gxpublish.brain.editorial.support.EditorialReviewContractAssembler;
import com.gxpublish.brain.editorial.support.EditorialReviewWorkflowDefinition;
import com.gxpublish.brain.workflow.common.constant.FlowConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 编辑部审校 Service 业务层处理。
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class EditorialReviewServiceImpl implements IEditorialReviewService {

    private final EditorialReviewMapper baseMapper;
    private final EditorialAttachmentMapper attachmentMapper;
    private final EditorialLinkMapper linkMapper;
    private final EditorialHistoryMapper historyMapper;
    private final WorkflowService workflowService;
    private final EditorialDataScopeFactory dataScopeFactory;
    private final EditorialWorkflowRoleMapper workflowRoleMapper;

    @Override
    public EditorialReviewDetailVo queryById(Long id) {
        EditorialReviewDetailVo detail = baseMapper.selectVoById(id);
        if (detail == null) {
            return null;
        }
        if (detail.getCurrentAttachmentId() != null) {
            detail.setAttachment(attachmentMapper.selectVoById(detail.getCurrentAttachmentId()));
        }
        detail.setLinkList(linkMapper.selectVoList(
            new LambdaQueryWrapper<EditorialLink>().eq(EditorialLink::getReviewId, id)));
        detail.setCanEdit(canCurrentUserEdit(detail.getReviewStatus(), getCurrentUserRoleKeys()));
        EditorialReviewContractAssembler.populateDetailContract(detail, queryHistoryList(id));
        return detail;
    }

    @Override
    public TableDataInfo<EditorialReviewPageItemVo> queryPageList(EditorialReviewBo bo, PageQuery pageQuery) {
        EditorialScopeParam scopeParam = dataScopeFactory.buildScopeParams();
        Map<String, Object> params = new HashMap<>();
        params.put("scopeParam", scopeParam);

        Page<EditorialReviewPageItemVo> result = baseMapper.customSelectPage(pageQuery.build(), bo, params);
        populatePageRecords(result.getRecords(), getCurrentUserRoleKeys());
        return TableDataInfo.build(result);
    }

    @Override
    public List<EditorialReviewPageItemVo> queryList(EditorialReviewBo bo) {
        EditorialScopeParam scopeParam = dataScopeFactory.buildScopeParams();
        Map<String, Object> params = new HashMap<>();
        params.put("scopeParam", scopeParam);

        List<EditorialReviewPageItemVo> list = baseMapper.customSelectList(bo, params);
        populatePageRecords(list, getCurrentUserRoleKeys());
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewDetailVo insertByBo(EditorialReviewBo bo) {
        prepareBoForWrite(bo);
        EditorialReview add = MapstructUtils.convert(bo, EditorialReview.class);
        add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        add.setReviewStatus(ReviewStatusEnum.DRAFT.getCode());
        if (add.getUserId() == null) {
            add.setUserId(LoginHelper.getUserId());
        }

        baseMapper.insert(add);
        bo.setId(add.getId());

        handleAttachment(bo, add.getId());
        handleLinks(bo.getLinkList(), add.getId());
        return queryById(add.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewDetailVo submitAndFlowStart(EditorialReviewBo bo) {
        prepareBoForWrite(bo);
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
        return startWorkflow(review);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewDetailVo resubmitAndFlowStart(EditorialReviewBo bo) {
        EditorialReview existing = baseMapper.selectById(bo.getId());
        if (existing == null) {
            throw new ServiceException("申请不存在");
        }
        if (!BusinessStatusEnum.BACK.getStatus().equals(existing.getStatus())
            && !ReviewStatusEnum.BACK.getCode().equals(existing.getReviewStatus())) {
            throw new ServiceException("只有退回状态可重新提交");
        }

        updateByBo(bo);
        EditorialReview review = baseMapper.selectById(bo.getId());
        return startWorkflow(review);
    }

    private EditorialReviewDetailVo startWorkflow(EditorialReview review) {
        boolean certifiedApplicant = isCertifiedApplicant(LoginHelper.getLoginUser());
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setBusinessId(review.getId().toString());
        startProcess.setFlowCode(EditorialReviewWorkflowDefinition.FLOW_CODE);
        Map<String, Object> variables = new HashMap<>();
        variables.put("ignore", true);
        variables.put("isCertified", certifiedApplicant);
        variables.putAll(buildApproverPermissionVariables());
        startProcess.setVariables(variables);

        boolean flag = workflowService.startCompleteTask(startProcess);
        if (!flag) {
            throw new ServiceException("流程发起异常");
        }

        if (certifiedApplicant) {
            EditorialHistory history = new EditorialHistory();
            history.setReviewId(review.getId());
            LoginUser loginUser = LoginHelper.getLoginUser();
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
    public EditorialReviewDetailVo updateByBo(EditorialReviewBo bo) {
        prepareBoForWrite(bo);
        EditorialReview oldReview = baseMapper.selectById(bo.getId());
        if (oldReview == null) {
            throw new ServiceException("申请不存在");
        }

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

        boolean attachmentChanged = handleAttachment(bo, bo.getId());
        if (attachmentChanged) {
            EditorialReview refreshed = baseMapper.selectById(bo.getId());
            update.setCurrentAttachmentId(refreshed.getCurrentAttachmentId());
        }

        if (BusinessStatusEnum.WAITING.getStatus().equals(oldReview.getStatus())) {
            recordHistory(oldReview, update, bo.getLinkList(), attachmentChanged);
        }

        baseMapper.updateById(update);
        handleLinks(bo.getLinkList(), bo.getId());
        return queryById(bo.getId());
    }

    private boolean handleAttachment(EditorialReviewBo bo, Long reviewId) {
        if (StringUtils.isBlank(bo.getAttachmentOssId())) {
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

        if (!StringUtils.equals(bo.getAttachmentOssId(), currentOssId)) {
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

            review.setCurrentAttachmentId(newAttachment.getId());
            baseMapper.updateById(review);
            return true;
        }
        return false;
    }

    private void handleLinks(List<EditorialLinkBo> linkBoList, Long reviewId) {
        if (linkBoList == null) {
            return;
        }

        List<Long> inputIds = linkBoList.stream()
            .map(EditorialLinkBo::getId)
            .filter(Objects::nonNull)
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
            diff.put("content", Map.of("old", "...", "new", "..."));
        }
        if (attachmentChanged) {
            diff.put("attachment", "Version updated");
        }
        if (CollUtil.isNotEmpty(links)) {
            diff.put("linkCount", links.size());
        }

        if (!diff.isEmpty()) {
            EditorialHistory history = new EditorialHistory();
            history.setReviewId(oldVal.getId());
            history.setOperatorId(LoginHelper.getUserId());
            try {
                history.setOperatorName(LoginHelper.getUsername());
            } catch (Exception ignored) {
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
        if (review == null) {
            return;
        }
        boolean submit = Boolean.TRUE.equals(processEvent.getSubmit());
        String newStatus = processEvent.getStatus();
        Map<String, Object> params = processEvent.getParams() == null ? Map.of() : processEvent.getParams();

        if (submit) {
            if (StringUtils.isBlank(review.getApplyCode())) {
                review.setApplyCode(Convert.toStr(params.get(FlowConstant.BUSINESS_CODE)));
            }
            review.setStatus(BusinessStatusEnum.WAITING.getStatus());
        } else if (StringUtils.isNotBlank(newStatus)) {
            review.setStatus(newStatus);
        }

        if (BusinessStatusEnum.BACK.getStatus().equals(review.getStatus())) {
            review.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        } else if (BusinessStatusEnum.CANCEL.getStatus().equals(review.getStatus())) {
            review.setReviewStatus(ReviewStatusEnum.CANCELED.getCode());
        } else if (BusinessStatusEnum.FINISH.getStatus().equals(review.getStatus())) {
            review.setReviewStatus(ReviewStatusEnum.APPROVED.getCode());
        } else if (BusinessStatusEnum.TERMINATION.getStatus().equals(review.getStatus())) {
            review.setReviewStatus(ReviewStatusEnum.TERMINATED.getCode());
        } else if (submit && BusinessStatusEnum.WAITING.getStatus().equals(review.getStatus())) {
            review.setReviewStatus(EditorialReviewWorkflowDefinition.resolveSubmitWaitingStatus(
                Convert.toBool(params.get("isCertified"), false)));
        }

        baseMapper.updateById(review);

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
        if (submit) {
            opType = "发起审批";
        } else if (StringUtils.isNotBlank(newStatus)) {
            opType = "FLOW_" + newStatus;
        }
        history.setOperateType(opType);
        historyMapper.insert(history);
    }

    @EventListener(condition = "#processTaskEvent.flowCode.startsWith('editorial_review')")
    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
        log.info("审校流程任务事件: {}", processTaskEvent);
        EditorialReview review = baseMapper.selectById(Convert.toLong(processTaskEvent.getBusinessId()));
        if (review == null || !BusinessStatusEnum.WAITING.getStatus().equals(review.getStatus())) {
            return;
        }
        Integer reviewStatus = EditorialReviewWorkflowDefinition.resolveWaitingStatus(processTaskEvent.getNodeCode());
        if (reviewStatus == null || Objects.equals(reviewStatus, review.getReviewStatus())) {
            return;
        }
        review.setReviewStatus(reviewStatus);
        baseMapper.updateById(review);
    }

    @EventListener(condition = "#processDeleteEvent.flowCode.startsWith('editorial_review')")
    public void processDeleteHandler(ProcessDeleteEvent processDeleteEvent) {
        log.info("审校流程删除: {}", processDeleteEvent);
        EditorialReview review = baseMapper.selectById(Convert.toLong(processDeleteEvent.getBusinessId()));
        if (review != null) {
            baseMapper.deleteById(review.getId());
        }
    }

    private void prepareBoForWrite(EditorialReviewBo bo) {
        bo.setProcessType(EditorialReviewWorkflowDefinition.normalizeProcessType(bo.getProcessType()));
        bo.setFlowCode(EditorialReviewWorkflowDefinition.FLOW_CODE);
    }

    private void populatePageRecords(List<EditorialReviewPageItemVo> records, List<String> currentRoleKeys) {
        for (EditorialReviewPageItemVo record : records) {
            record.setCanEdit(canCurrentUserEdit(record.getReviewStatus(), currentRoleKeys));
            EditorialReviewContractAssembler.populatePageContract(record);
        }
    }

    private boolean canCurrentUserEdit(Integer reviewStatus, List<String> currentRoleKeys) {
        if (reviewStatus == null) {
            return false;
        }
        List<String> canEditRoleKeyList = ReviewStatusEnum.getByStatus(reviewStatus).getCanEditRoleKeyList();
        return CollUtil.containsAny(canEditRoleKeyList, currentRoleKeys);
    }

    private List<String> getCurrentUserRoleKeys() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || CollUtil.isEmpty(loginUser.getRoles())) {
            return List.of();
        }
        return loginUser.getRoles().stream().map(RoleDTO::getRoleKey).toList();
    }

    private boolean isCertifiedApplicant(LoginUser loginUser) {
        if (loginUser == null || CollUtil.isEmpty(loginUser.getRoles())) {
            return false;
        }
        return loginUser.getRoles().stream()
            .anyMatch(role -> EditorialRoleEnum.APPLICANT_CER.getRoleKey().equals(role.getRoleKey()));
    }

    private Map<String, Object> buildApproverPermissionVariables() {
        List<String> roleKeys = List.of(
            EditorialRoleEnum.FIRST_APPROVER.getRoleKey(),
            EditorialRoleEnum.SECOND_APPROVER.getRoleKey(),
            EditorialRoleEnum.FINAL_APPROVER.getRoleKey());
        Map<String, Long> roleIdMap = workflowRoleMapper.selectRoleRefs(roleKeys).stream()
            .collect(Collectors.toMap(
                roleRef -> roleRef.getRoleKey(),
                roleRef -> roleRef.getRoleId(),
                (left, right) -> left));

        Map<String, Object> variables = new HashMap<>();
        variables.put(EditorialReviewWorkflowDefinition.FIRST_APPROVER_PERMISSION_VAR,
            toPermissionFlag(roleIdMap, EditorialRoleEnum.FIRST_APPROVER.getRoleKey()));
        variables.put(EditorialReviewWorkflowDefinition.SECOND_APPROVER_PERMISSION_VAR,
            toPermissionFlag(roleIdMap, EditorialRoleEnum.SECOND_APPROVER.getRoleKey()));
        variables.put(EditorialReviewWorkflowDefinition.FINAL_APPROVER_PERMISSION_VAR,
            toPermissionFlag(roleIdMap, EditorialRoleEnum.FINAL_APPROVER.getRoleKey()));
        return variables;
    }

    private String toPermissionFlag(Map<String, Long> roleIdMap, String roleKey) {
        Long roleId = roleIdMap.get(roleKey);
        if (roleId == null) {
            throw new ServiceException("缺少审批角色 seed: " + roleKey);
        }
        return "role:" + roleId;
    }
}
