import type { LocationQuery } from 'vue-router';
import type { ManuscriptReviewDetailVO, ManuscriptReviewResourceItemVO } from '@/api/manuscript-review/types';

export type ManuscriptReviewFormMode = 'create' | 'edit';
export type ManuscriptReviewDraftResourceType = 'ATTACHMENT' | 'VIDEO';

export interface ManuscriptReviewDraftFormModel {
  processType: string;
  externalManuscriptCode: string;
  title: string;
  mediaChannel: string;
  submitDepartment: string;
  authorName: string;
  remark: string;
  contentBody: string;
}

export interface ManuscriptReviewDraftUploadItem {
  uid: string;
  displayName: string;
  ossId: string | number;
  url?: string;
  resourceType: ManuscriptReviewDraftResourceType;
}

export interface ManuscriptReviewDraftExternalLinkItem {
  uid: string;
  displayName: string;
  externalUrl: string;
}

export interface ManuscriptReviewFormPresentation {
  mode: ManuscriptReviewFormMode;
  title: string;
  description: string;
  primaryActionLabel: string;
  successToast: string;
}

export interface ManuscriptReviewPersistedResourceView {
  id: string;
  typeLabel: string;
  displayName: string;
  note: string;
}

export interface ManuscriptReviewEditDraftState {
  form: ManuscriptReviewDraftFormModel;
  persistedResources: ManuscriptReviewPersistedResourceView[];
  draftUploads: ManuscriptReviewDraftUploadItem[];
  draftExternalLinks: ManuscriptReviewDraftExternalLinkItem[];
}

const readQueryText = (value: LocationQuery[string]): string | undefined => {
  if (Array.isArray(value)) {
    return value[0] ? String(value[0]) : undefined;
  }
  if (value == null) {
    return undefined;
  }
  return String(value);
};

export const resolveFormMode = (query: LocationQuery): ManuscriptReviewFormMode => {
  const mode = readQueryText(query.mode);
  return mode === 'edit' ? 'edit' : 'create';
};

export const readReviewIdFromQuery = (query: LocationQuery): string => readQueryText(query.reviewId) ?? '';

export const buildFormPresentation = (mode: ManuscriptReviewFormMode): ManuscriptReviewFormPresentation => {
  if (mode === 'edit') {
    return {
      mode,
      title: '修改审校单',
      description: '修改页只负责保存资料与本次追加资源，保存后回详情，不推进 BPM。',
      primaryActionLabel: '保存',
      successToast: '保存成功，已返回详情页。'
    };
  }

  return {
    mode,
    title: '新增审校单',
    description: '新增页只有一个正式业务动作“提交”，附件与外链在提交时一并进入后端。',
    primaryActionLabel: '提交',
    successToast: '提交成功，已进入详情页。'
  };
};

export const removeDraftUploadByOssId = (
  items: ManuscriptReviewDraftUploadItem[],
  ossId: string | number
): ManuscriptReviewDraftUploadItem[] => items.filter((item) => String(item.ossId) !== String(ossId));

export const buildIntegratedSubmitPayload = (
  form: ManuscriptReviewDraftFormModel,
  uploads: ManuscriptReviewDraftUploadItem[],
  externalLinks: ManuscriptReviewDraftExternalLinkItem[]
) => ({
  ...form,
  attachmentList: uploads.map((item) => ({
    displayName: item.displayName,
    ossId: item.ossId,
    resourceType: item.resourceType
  })),
  externalLinkList: externalLinks
    .filter((item) => item.displayName.trim() && item.externalUrl.trim())
    .map((item) => ({
      displayName: item.displayName.trim(),
      externalUrl: item.externalUrl.trim(),
      resourceType: 'EXTERNAL_LINK'
    }))
});

export const buildIntegratedSavePayload = (
  reviewId: string,
  form: ManuscriptReviewDraftFormModel,
  uploads: ManuscriptReviewDraftUploadItem[],
  externalLinks: ManuscriptReviewDraftExternalLinkItem[]
) => ({
  id: reviewId,
  ...buildIntegratedSubmitPayload(form, uploads, externalLinks)
});

const mapPersistedResources = (
  resources: ManuscriptReviewResourceItemVO[] | null | undefined,
  typeLabel: string
): ManuscriptReviewPersistedResourceView[] =>
  (resources ?? []).map((item) => ({
    id: String(item.id),
    typeLabel: item.resourceTypeLabel ?? typeLabel,
    displayName: item.displayName,
    note: item.externalUrl ?? item.resourceUrl ?? ''
  }));

export const createDraftStateFromDetail = (detail: ManuscriptReviewDetailVO): ManuscriptReviewEditDraftState => ({
  form: {
    processType: detail.processType ?? '',
    externalManuscriptCode: detail.externalManuscriptCode ?? '',
    title: detail.title ?? '',
    mediaChannel: detail.mediaChannel ?? '',
    submitDepartment: detail.submitDepartment ?? '',
    authorName: detail.authorName ?? '',
    remark: detail.remark ?? '',
    contentBody: detail.contentBody ?? ''
  },
  persistedResources: [
    ...mapPersistedResources(detail.attachmentList, '附件'),
    ...mapPersistedResources(detail.externalLinkList, '外链'),
    ...mapPersistedResources(detail.videoList, '视频')
  ],
  draftUploads: [],
  draftExternalLinks: []
});
