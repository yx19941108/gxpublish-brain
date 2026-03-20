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
import com.gxpublish.brain.editorial.domain.bo.EditorialAttachmentBo;
import com.gxpublish.brain.editorial.domain.bo.EditorialLinkBo;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.param.EditorialScopeParam;
import com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialLinkVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewTaskContextVo;
import com.gxpublish.brain.editorial.enums.EditorialRoleEnum;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import com.gxpublish.brain.editorial.mapper.EditorialAttachmentMapper;
import com.gxpublish.brain.editorial.mapper.EditorialHistoryMapper;
import com.gxpublish.brain.editorial.mapper.EditorialLinkMapper;
import com.gxpublish.brain.editorial.mapper.EditorialReviewMapper;
import com.gxpublish.brain.editorial.mapper.EditorialWorkflowRoleMapper;
import com.gxpublish.brain.editorial.service.IEditorialReviewService;
import com.gxpublish.brain.editorial.service.strategy.EditorialDataScopeFactory;
import com.gxpublish.brain.editorial.support.EditorialHistoryFactory;
import com.gxpublish.brain.editorial.support.EditorialReviewContractAssembler;
import com.gxpublish.brain.editorial.support.EditorialReviewWorkflowDefinition;
import com.gxpublish.brain.workflow.common.constant.FlowConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private final EditorialHistoryFactory historyFactory;

    @Override
    public EditorialReviewDetailVo queryById(Long id) {
        EditorialReviewDetailVo detail = baseMapper.selectVoById(id);
        if (detail == null) {
            return null;
        }
        List<EditorialAttachmentVo> attachmentList = listReviewAttachments(id);
        detail.setAttachmentList(attachmentList);
        detail.setAttachment(resolveCurrentAttachment(detail.getCurrentAttachmentId(), attachmentList));
        detail.setLinkList(listReviewLinks(id));
        LoginUser loginUser = LoginHelper.getLoginUser();
        EditorialReviewTaskContextVo taskContext = resolveCurrentTaskContext(id);
        detail.setCanEdit(canCurrentUserEdit(detail.getReviewStatus(), detail.getUserId(), taskContext, loginUser));
        EditorialReviewContractAssembler.populateDetailContract(
            detail,
            queryHistoryList(id),
            taskContext,
            canCurrentUserApprove(detail.getReviewStatus(), taskContext));
        return detail;
    }

    @Override
    public TableDataInfo<EditorialReviewPageItemVo> queryPageList(EditorialReviewBo bo, PageQuery pageQuery) {
        EditorialScopeParam scopeParam = dataScopeFactory.buildScopeParams();
        Map<String, Object> params = new HashMap<>();
        params.put("scopeParam", scopeParam);

        Page<EditorialReviewPageItemVo> result = baseMapper.customSelectPage(pageQuery.build(), bo, params);
        populatePageRecords(result.getRecords(), LoginHelper.getLoginUser());
        return TableDataInfo.build(result);
    }

    @Override
    public List<EditorialReviewPageItemVo> queryList(EditorialReviewBo bo) {
        EditorialScopeParam scopeParam = dataScopeFactory.buildScopeParams();
        Map<String, Object> params = new HashMap<>();
        params.put("scopeParam", scopeParam);

        List<EditorialReviewPageItemVo> list = baseMapper.customSelectList(bo, params);
        populatePageRecords(list, LoginHelper.getLoginUser());
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

        handleAttachments(bo, add.getId());
        handleLinks(bo.getLinkList(), add.getId());
        insertHistory(historyFactory.buildCreateHistory(add, safeGetLoginUser(), safeGetUserId(), safeGetUsername()));
        return queryById(add.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EditorialReviewDetailVo submitAndFlowStart(EditorialReviewBo bo) {
        prepareBoForWrite(bo);
        validateSubmitMaterials(bo);
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

        validateSubmitMaterials(bo);
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
                history.setOperatorName(safeGetUsername());
            }
            history.setEventType("APPROVAL");
            history.setOperateTime(new Date());
            history.setOperateType("系统自动跳过一级审批");
            Map<String, Object> diff = new HashMap<>();
            diff.put("action", "AUTO_SKIP");
            diff.put("comment", "发起人持证自动通过一级审批");
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
        LoginUser loginUser = LoginHelper.getLoginUser();
        EditorialReviewTaskContextVo taskContext = resolveCurrentTaskContext(bo.getId());
        validateUpdatePermission(oldReview, taskContext, loginUser);
        List<EditorialLinkVo> oldLinks = listReviewLinks(bo.getId());
        List<EditorialAttachmentVo> oldAttachments = listReviewAttachments(bo.getId());

        EditorialReview update = MapstructUtils.convert(bo, EditorialReview.class);
        baseMapper.updateById(update);
        handleAttachments(bo, bo.getId());
        handleLinks(bo.getLinkList(), bo.getId());
        EditorialReview refreshedReview = baseMapper.selectById(bo.getId());
        insertHistory(historyFactory.buildModifyHistory(
            oldReview,
            refreshedReview,
            oldLinks,
            listReviewLinks(bo.getId()),
            oldAttachments,
            listReviewAttachments(bo.getId()),
            loginUser,
            safeGetUserId(),
            safeGetUsername()));
        return queryById(bo.getId());
    }

    private boolean handleAttachments(EditorialReviewBo bo, Long reviewId) {
        EditorialReview review = baseMapper.selectById(reviewId);
        if (review == null) {
            return false;
        }
        List<EditorialAttachmentVo> existingAttachments = listReviewAttachments(reviewId);
        List<EditorialAttachmentBo> requestAttachments = resolveRequestedAttachments(bo);
        if (CollUtil.isEmpty(requestAttachments)) {
            return false;
        }

        Map<String, EditorialAttachmentVo> existingByKey = existingAttachments.stream()
            .filter(item -> StringUtils.isNotBlank(item.getOssId()))
            .collect(Collectors.toMap(
                EditorialAttachmentVo::getOssId,
                item -> item,
                (left, right) -> left,
                LinkedHashMap::new));
        int nextVersion = existingAttachments.stream()
            .map(EditorialAttachmentVo::getVersion)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
        boolean changed = false;
        Long latestAttachmentId = review.getCurrentAttachmentId();

        for (EditorialAttachmentBo requestAttachment : requestAttachments) {
            if (StringUtils.isBlank(requestAttachment.getOssId())) {
                continue;
            }
            if (existingByKey.containsKey(requestAttachment.getOssId())) {
                latestAttachmentId = existingByKey.get(requestAttachment.getOssId()).getId();
                continue;
            }
            EditorialAttachment newAttachment = new EditorialAttachment();
            newAttachment.setReviewId(reviewId);
            newAttachment.setOssId(requestAttachment.getOssId());
            newAttachment.setFileName(requestAttachment.getFileName());
            newAttachment.setFileUrl(requestAttachment.getFileUrl());
            newAttachment.setFileSize(requestAttachment.getFileSize());
            newAttachment.setVersion(++nextVersion);
            newAttachment.setUploaderId(LoginHelper.getUserId());
            newAttachment.setCreateTime(new Date());
            attachmentMapper.insert(newAttachment);
            latestAttachmentId = newAttachment.getId();
            changed = true;
        }

        if (!Objects.equals(review.getCurrentAttachmentId(), latestAttachmentId)) {
            review.setCurrentAttachmentId(latestAttachmentId);
            baseMapper.updateById(review);
        }
        return changed;
    }

    private void handleLinks(List<EditorialLinkBo> linkBoList, Long reviewId) {
        if (CollUtil.isEmpty(linkBoList)) {
            return;
        }
        List<EditorialLinkVo> existingLinks = listReviewLinks(reviewId);
        java.util.Set<Long> existingIds = existingLinks.stream()
            .map(EditorialLinkVo::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        java.util.Set<String> existingKeys = existingLinks.stream()
            .map(this::resolveLinkIdentity)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (EditorialLinkBo linkBo : linkBoList) {
            if (linkBo == null || StringUtils.isBlank(linkBo.getUrl())) {
                continue;
            }
            if (linkBo.getId() != null && existingIds.contains(linkBo.getId())) {
                continue;
            }
            String linkIdentity = resolveLinkIdentity(linkBo.getUrl(), linkBo.getDescription());
            if (existingKeys.contains(linkIdentity)) {
                continue;
            }
            EditorialLink link = new EditorialLink();
            link.setReviewId(reviewId);
            link.setUrl(StringUtils.trim(linkBo.getUrl()));
            link.setDescription(StringUtils.trim(linkBo.getDescription()));
            linkMapper.insert(link);
            existingKeys.add(linkIdentity);
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
        insertHistory(historyFactory.buildProcessHistory(review, processEvent, safeGetLoginUser(), safeGetUserId(), safeGetUsername()));
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

    private void populatePageRecords(List<EditorialReviewPageItemVo> records, LoginUser loginUser) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<Long, EditorialReviewTaskContextVo> taskContextMap = loadCurrentTaskContextMap(records);
        for (EditorialReviewPageItemVo record : records) {
            EditorialReviewTaskContextVo taskContext = taskContextMap.get(record.getId());
            record.setCanEdit(canCurrentUserEdit(record.getReviewStatus(), record.getUserId(), taskContext, loginUser));
            EditorialReviewContractAssembler.populatePageContract(record);
            EditorialReviewContractAssembler.populateTaskContract(
                record,
                taskContext,
                canCurrentUserApprove(record.getReviewStatus(), taskContext));
        }
    }

    private boolean canCurrentUserEdit(Integer reviewStatus,
                                       Long applicantUserId,
                                       EditorialReviewTaskContextVo taskContext,
                                       LoginUser loginUser) {
        if (reviewStatus == null || loginUser == null) {
            return false;
        }
        if (LoginHelper.isSuperAdmin()) {
            return true;
        }
        if (ReviewStatusEnum.DRAFT.getCode().equals(reviewStatus) || ReviewStatusEnum.BACK.getCode().equals(reviewStatus)) {
            return Objects.equals(applicantUserId, loginUser.getUserId());
        }
        return canCurrentUserApprove(reviewStatus, taskContext);
    }

    private boolean canCurrentUserApprove(Integer reviewStatus, EditorialReviewTaskContextVo taskContext) {
        return reviewStatus != null
            && ReviewStatusEnum.getByStatus(reviewStatus).getFlowStatus().equals(BusinessStatusEnum.WAITING.getStatus())
            && taskContext != null
            && taskContext.getTaskId() != null;
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

    private void validateSubmitMaterials(EditorialReviewBo bo) {
        boolean hasAttachment = CollUtil.isNotEmpty(resolveRequestedAttachments(bo));
        if (!hasAttachment && bo.getId() != null) {
            hasAttachment = CollUtil.isNotEmpty(listReviewAttachments(bo.getId()));
        }
        boolean hasLink = CollUtil.isNotEmpty(bo.getLinkList())
            && bo.getLinkList().stream().anyMatch(link -> StringUtils.isNotBlank(link.getUrl()));
        if (!hasLink && bo.getId() != null) {
            hasLink = CollUtil.isNotEmpty(listReviewLinks(bo.getId()));
        }
        if (!hasAttachment && !hasLink) {
            throw new ServiceException("提交审批时，附件和关联链接至少需要填写一项");
        }
    }

    private void validateUpdatePermission(EditorialReview oldReview,
                                          EditorialReviewTaskContextVo taskContext,
                                          LoginUser loginUser) {
        Integer reviewStatus = oldReview.getReviewStatus();
        if (reviewStatus == null) {
            throw new ServiceException("当前申请缺少审校状态，不允许修改");
        }
        if (ReviewStatusEnum.DRAFT.getCode().equals(reviewStatus) || ReviewStatusEnum.BACK.getCode().equals(reviewStatus)) {
            Long currentUserId = LoginHelper.getUserId();
            Long applicantUserId = oldReview.getUserId() != null ? oldReview.getUserId() : oldReview.getCreateBy();
            if (!Objects.equals(currentUserId, applicantUserId) && !LoginHelper.isSuperAdmin()) {
                throw new ServiceException("只有发起人可以修改表单内容");
            }
            return;
        }
        if (!canCurrentUserEdit(reviewStatus, oldReview.getUserId(), taskContext, loginUser)) {
            throw new ServiceException("当前流程状态下，只有当前节点责任审批人可以修改表单内容");
        }
    }

    private EditorialReviewTaskContextVo resolveCurrentTaskContext(Long reviewId) {
        if (reviewId == null || LoginHelper.getUserId() == null) {
            return null;
        }
        return baseMapper.selectCurrentTaskContext(Convert.toStr(reviewId), LoginHelper.getUserIdStr());
    }

    private Map<Long, EditorialReviewTaskContextVo> loadCurrentTaskContextMap(List<EditorialReviewPageItemVo> records) {
        if (CollUtil.isEmpty(records) || LoginHelper.getUserId() == null) {
            return Map.of();
        }
        List<String> reviewIds = records.stream()
            .map(EditorialReviewPageItemVo::getId)
            .filter(Objects::nonNull)
            .map(Convert::toStr)
            .toList();
        if (CollUtil.isEmpty(reviewIds)) {
            return Map.of();
        }
        return baseMapper.selectCurrentTaskContexts(reviewIds, LoginHelper.getUserIdStr()).stream()
            .collect(Collectors.toMap(
                EditorialReviewTaskContextVo::getReviewId,
                item -> item,
                (left, right) -> left,
                LinkedHashMap::new));
    }

    private List<EditorialAttachmentVo> listReviewAttachments(Long reviewId) {
        return attachmentMapper.selectVoList(
            new LambdaQueryWrapper<EditorialAttachment>()
                .eq(EditorialAttachment::getReviewId, reviewId)
                .orderByAsc(EditorialAttachment::getVersion)
                .orderByAsc(EditorialAttachment::getCreateTime)
                .orderByAsc(EditorialAttachment::getId));
    }

    private List<EditorialLinkVo> listReviewLinks(Long reviewId) {
        return linkMapper.selectVoList(
            new LambdaQueryWrapper<EditorialLink>()
                .eq(EditorialLink::getReviewId, reviewId)
                .orderByAsc(EditorialLink::getCreateTime)
                .orderByAsc(EditorialLink::getId));
    }

    private EditorialAttachmentVo resolveCurrentAttachment(Long currentAttachmentId, List<EditorialAttachmentVo> attachmentList) {
        if (CollUtil.isEmpty(attachmentList)) {
            return null;
        }
        if (currentAttachmentId != null) {
            for (EditorialAttachmentVo attachment : attachmentList) {
                if (Objects.equals(attachment.getId(), currentAttachmentId)) {
                    return attachment;
                }
            }
        }
        return attachmentList.get(attachmentList.size() - 1);
    }

    private List<EditorialAttachmentBo> resolveRequestedAttachments(EditorialReviewBo bo) {
        Map<String, EditorialAttachmentBo> attachmentMap = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(bo.getAttachmentList())) {
            for (EditorialAttachmentBo attachmentBo : bo.getAttachmentList()) {
                if (attachmentBo == null || StringUtils.isBlank(attachmentBo.getOssId())) {
                    continue;
                }
                attachmentMap.putIfAbsent(attachmentBo.getOssId(), attachmentBo);
            }
        }
        if (StringUtils.isNotBlank(bo.getAttachmentOssId())) {
            EditorialAttachmentBo legacyAttachment = new EditorialAttachmentBo();
            legacyAttachment.setOssId(bo.getAttachmentOssId());
            legacyAttachment.setFileName(bo.getAttachmentFileName());
            legacyAttachment.setFileUrl(bo.getAttachmentFileUrl());
            legacyAttachment.setFileSize(bo.getAttachmentFileSize());
            attachmentMap.putIfAbsent(legacyAttachment.getOssId(), legacyAttachment);
        }
        return new ArrayList<>(attachmentMap.values());
    }

    private void insertHistory(EditorialHistory history) {
        if (history != null) {
            historyMapper.insert(history);
        }
    }

    private String resolveLinkIdentity(EditorialLinkVo link) {
        return resolveLinkIdentity(link == null ? null : link.getUrl(), link == null ? null : link.getDescription());
    }

    private String resolveLinkIdentity(String url, String description) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(url)) + "|" + StringUtils.lowerCase(StringUtils.trimToEmpty(description));
    }

    private LoginUser safeGetLoginUser() {
        try {
            return LoginHelper.getLoginUser();
        } catch (Exception e) {
            log.warn("获取登录用户失败: {}", e.getMessage());
            return null;
        }
    }

    private Long safeGetUserId() {
        try {
            return LoginHelper.getUserId();
        } catch (Exception e) {
            log.warn("获取登录用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    private String safeGetUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception e) {
            log.warn("获取登录用户名失败: {}", e.getMessage());
            return null;
        }
    }
}
