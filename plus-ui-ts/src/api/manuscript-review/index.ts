import request from '@/utils/request';
import { AxiosPromise } from 'axios';

import type {
  ManuscriptReviewCancelCommand,
  ManuscriptReviewDetailVO,
  ManuscriptReviewListPageVO,
  ManuscriptReviewListQuery,
  ManuscriptReviewResourceCreateCommand,
  ManuscriptReviewResourceDisableCommand,
  ManuscriptReviewResourceItemVO,
  ManuscriptReviewReviewIdCommand,
  ManuscriptReviewSaveCommand,
  ManuscriptReviewVideoMarkCreateCommand,
  ManuscriptReviewVideoMarkItemVO
} from '@/api/manuscript-review/types';

export const listManuscriptReview = (query?: ManuscriptReviewListQuery): AxiosPromise<ManuscriptReviewListPageVO> => {
  return request({
    url: '/workflow/manuscript-review/list',
    method: 'get',
    params: query
  });
};

export const getManuscriptReviewDetail = (id: string | number): AxiosPromise<ManuscriptReviewDetailVO> => {
  return request({
    url: `/workflow/manuscript-review/${id}`,
    method: 'get'
  });
};

export const createManuscriptReview = (data: ManuscriptReviewSaveCommand): AxiosPromise<number | string> => {
  return request({
    url: '/workflow/manuscript-review',
    method: 'post',
    data
  });
};

export const updateManuscriptReview = (data: ManuscriptReviewSaveCommand): AxiosPromise<void> => {
  return request({
    url: '/workflow/manuscript-review',
    method: 'put',
    data
  });
};

export const submitAndFlowStartManuscriptReview = (
  data: ManuscriptReviewReviewIdCommand
): AxiosPromise<ManuscriptReviewDetailVO> => {
  return request({
    url: '/workflow/manuscript-review/submitAndFlowStart',
    method: 'post',
    data
  });
};

export const resubmitManuscriptReview = (
  data: ManuscriptReviewReviewIdCommand
): AxiosPromise<ManuscriptReviewDetailVO> => {
  return request({
    url: '/workflow/manuscript-review/resubmit',
    method: 'post',
    data
  });
};

export const cancelManuscriptReviewProcess = (data: ManuscriptReviewCancelCommand): AxiosPromise<void> => {
  return request({
    url: '/workflow/manuscript-review/cancelProcessApply',
    method: 'put',
    data
  });
};

export const addManuscriptReviewResource = (
  data: ManuscriptReviewResourceCreateCommand
): AxiosPromise<ManuscriptReviewResourceItemVO> => {
  return request({
    url: '/workflow/manuscript-review/resource',
    method: 'post',
    data
  });
};

export const disableManuscriptReviewResource = (
  data: ManuscriptReviewResourceDisableCommand
): AxiosPromise<void> => {
  return request({
    url: '/workflow/manuscript-review/resource/disable',
    method: 'put',
    data
  });
};

export const addManuscriptReviewVideoMark = (
  data: ManuscriptReviewVideoMarkCreateCommand
): AxiosPromise<ManuscriptReviewVideoMarkItemVO> => {
  return request({
    url: '/workflow/manuscript-review/video-mark',
    method: 'post',
    data
  });
};
