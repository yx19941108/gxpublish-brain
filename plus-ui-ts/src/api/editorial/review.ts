import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 审校申请VO
export interface EditorialReviewVo {
    id: number;
    title: string;
    content: string;
    deptId: number;
    userId: number;
    userName: string;
    deptName: string;
    status: string;
    applyCode: string;
    currentAttachmentId: number;
    remark: string;
    createTime: string;
    processType: string;
    attachment?: EditorialAttachmentVo;
    linkList?: EditorialLinkVo[];
}

// 审校申请Form
export interface EditorialReviewForm {
    id?: number;
    title: string;
    content: string;
    deptId?: number;
    status?: string;
    remark?: string;
    attachmentOssId?: string;
    attachmentFileName?: string;
    attachment?: any;
    linkList?: EditorialLinkBo[];
    flowCode?: string;
    processType?: string;
}

// 附件VO
export interface EditorialAttachmentVo {
    id: number;
    reviewId: number;
    fileName: string;
    ossId: string;
    fileUrl: string;
    fileSize: number;
    version: number;
    uploaderId: number;
    uploaderName: string;
    createTime: string;
}

// 链接VO/BO
export interface EditorialLinkVo {
    id: number;
    reviewId: number;
    url: string;
    description: string;
    createTime: string;
}

export interface EditorialLinkBo {
    id?: number | null;
    url: string;
    description: string;
}

// 历史记录VO
export interface EditorialHistoryVo {
    id: number;
    reviewId: number;
    operatorId: number;
    operatorName: string;
    operateTime: string;
    operateType: string;
    fieldDiff: Record<string, any>;
}

// 查询参数
export interface EditorialReviewQuery extends PageQuery {
    title?: string;
    status?: string;
    userId?: number;
    processType?: string;
}

/**
 * 查询审校申请列表
 */
export const listReview = (query: EditorialReviewQuery): AxiosPromise<EditorialReviewVo[]> => {
    return request({
        url: '/editorial/review/list',
        method: 'get',
        params: query
    });
};

/**
 * 获取审校申请详细信息
 */
export const getReview = (id: number | string): AxiosPromise<EditorialReviewVo> => {
    return request({
        url: '/editorial/review/' + id,
        method: 'get'
    });
};

/**
 * 新增审校申请(保存草稿)
 */
export const addReview = (data: EditorialReviewForm) => {
    return request({
        url: '/editorial/review',
        method: 'post',
        data: data
    });
};

/**
 * 修改审校申请
 */
export const updateReview = (data: EditorialReviewForm) => {
    return request({
        url: '/editorial/review',
        method: 'put',
        data: data
    });
};

/**
 * 提交并开启流程
 */
export const submitReview = (data: EditorialReviewForm) => {
    return request({
        url: '/editorial/review/submit',
        method: 'post',
        data: data
    });
};

/**
 * 删除审校申请
 */
export const delReview = (ids: string | number | Array<string | number>) => {
    return request({
        url: '/editorial/review/' + ids,
        method: 'delete'
    });
};

/**
 * 获取历史记录
 */
export const getHistory = (id: number | string): AxiosPromise<EditorialHistoryVo[]> => {
    return request({
        url: '/editorial/review/history/' + id,
        method: 'get'
    });
};
