package com.gxpublish.brain.editorial.enums;

import com.gxpublish.brain.common.core.enums.BusinessStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 稿件三审三校详细业务状态（流转进度）枚举
 * <p>
 * 用于控制精细化的审批流程可见性，与底层粗粒度的工作流引擎流转状态（business status）相隔离
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ReviewStatusEnum {

    /**
     * 草稿状态，刚创建还未提交
     * 引擎映射粗粒度状态：DRAFT (草稿)
     */
    DRAFT(0, "草稿", BusinessStatusEnum.DRAFT.getStatus()),

    /**
     * 待一级审批，已提交给该节点审批人
     * 引擎映射粗粒度状态：WAITING (流转中)
     */
    WAITING_FIRST(10, "待一审", BusinessStatusEnum.WAITING.getStatus()),

    /**
     * 待二级审批，已通过一次审批，等待二次审批人
     * 引擎映射粗粒度状态：WAITING (流转中)
     */
    WAITING_SECOND(20, "待二审", BusinessStatusEnum.WAITING.getStatus()),

    /**
     * 待终审，等待最终审批节点
     * 引擎映射粗粒度状态：WAITING (流转中)
     */
    WAITING_FINAL(30, "待终审", BusinessStatusEnum.WAITING.getStatus()),

    /**
     * 全部审批通过，流程正式完成
     * 引擎映射粗粒度状态：FINISH (已完成)
     */
    APPROVED(40, "审批通过", BusinessStatusEnum.FINISH.getStatus()),

    /**
     * 审批被退回，需要发起人重新流转或直接修改
     * 引擎映射粗粒度状态：BACK (已退回)
     */
    BACK(50, "已退回", BusinessStatusEnum.BACK.getStatus()),

    /**
     * 彻底终止或作废，不再允许继续流转
     * 引擎映射粗粒度状态：TERMINATION (已终止)
     */
    TERMINATED(60, "已彻底终止/作废", BusinessStatusEnum.TERMINATION.getStatus()),

    /**
     * 已被发起人主动撤销
     * 引擎映射粗粒度状态：CANCEL (已撤销)
     */
    CANCELED(70, "已撤销", BusinessStatusEnum.CANCEL.getStatus());

    /**
     * 三审三校专属精细化业务状态码 (原计划的 Integer 类型 status)
     */
    private final Integer code;

    /**
     * 中文状态描述说明
     */
    private final String desc;

    /**
     * Warm Flow 引擎映射的粗粒度流转大状态字符串
     */
    private final String flowStatus;
}
