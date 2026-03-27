import { describe, expect, it } from 'vitest';

import {
  BUSINESS_STATUS_OPTIONS,
  buildFormPresentation,
  buildIntegratedSavePayload,
  buildIntegratedSubmitPayload,
  createDraftStateFromDetail,
  PROCESS_TYPE_OPTIONS,
  readReviewIdFromQuery,
  removeDraftUploadByOssId,
  resolveFormMode,
  type ManuscriptReviewDraftExternalLinkItem,
  type ManuscriptReviewDraftFormModel,
  type ManuscriptReviewDraftUploadItem
} from './components/formState';

describe('T09_ManuscriptReview_FormStateSpec', () => {
  const baseForm: ManuscriptReviewDraftFormModel = {
    processType: 'AUDIT',
    externalManuscriptCode: 'EXT-20260326-01',
    title: '审校稿件',
    mediaChannel: '新华社/要闻',
    submitDepartment: '总编室',
    authorName: '张三',
    remark: '备注',
    contentBody: '正文内容'
  };

  const uploads: ManuscriptReviewDraftUploadItem[] = [
    {
      uid: 'upload-1',
      displayName: '送审单.pdf',
      ossId: 8001,
      resourceType: 'ATTACHMENT'
    },
    {
      uid: 'upload-2',
      displayName: '样片.mp4',
      ossId: 8002,
      resourceType: 'VIDEO'
    }
  ];

  const links: ManuscriptReviewDraftExternalLinkItem[] = [
    {
      uid: 'link-1',
      displayName: '素材参考',
      externalUrl: ' https://example.com/ref '
    }
  ];

  it('resolves create mode by default and edit mode when explicitly requested', () => {
    expect(resolveFormMode({})).toBe('create');
    expect(resolveFormMode({ mode: 'create' })).toBe('create');
    expect(resolveFormMode({ mode: 'edit' })).toBe('edit');
    expect(readReviewIdFromQuery({ reviewId: '9001' })).toBe('9001');
  });

  it('exposes only frozen process type and business status option values', () => {
    expect(PROCESS_TYPE_OPTIONS).toEqual([
      { label: '审核流程', value: 'AUDIT' },
      { label: '校对流程', value: 'PROOFREAD' }
    ]);

    expect(BUSINESS_STATUS_OPTIONS).toEqual([
      { label: '审批中', value: 'WAITING' },
      { label: '退回待修改', value: 'BACK' },
      { label: '已完成', value: 'FINISH' },
      { label: '发起人撤销', value: 'CANCEL' },
      { label: '三级驳回终止', value: 'REJECT' }
    ]);
  });

  it('builds create and edit presentations without approve or resubmit as primary actions', () => {
    expect(buildFormPresentation('create')).toEqual(
      expect.objectContaining({
        title: '新增审校单',
        primaryActionLabel: '提交'
      })
    );
    expect(buildFormPresentation('edit')).toEqual(
      expect.objectContaining({
        title: '修改审校单',
        primaryActionLabel: '保存'
      })
    );
    expect(buildFormPresentation('create').primaryActionLabel).not.toMatch(/审批|重新提交/);
    expect(buildFormPresentation('edit').primaryActionLabel).not.toMatch(/审批|重新提交/);
  });

  it('builds integrated create payload with main fields, attachments, and external links', () => {
    expect(buildIntegratedSubmitPayload(baseForm, uploads, links)).toEqual({
      ...baseForm,
      attachmentList: [
        {
          displayName: '送审单.pdf',
          ossId: 8001,
          resourceType: 'ATTACHMENT'
        },
        {
          displayName: '样片.mp4',
          ossId: 8002,
          resourceType: 'VIDEO'
        }
      ],
      externalLinkList: [
        {
          displayName: '素材参考',
          externalUrl: 'https://example.com/ref',
          resourceType: 'EXTERNAL_LINK'
        }
      ]
    });
  });

  it('builds integrated edit payload with review id and only current draft resources', () => {
    expect(buildIntegratedSavePayload('9001', baseForm, uploads.slice(0, 1), links)).toEqual({
      id: '9001',
      ...baseForm,
      attachmentList: [
        {
          displayName: '送审单.pdf',
          ossId: 8001,
          resourceType: 'ATTACHMENT'
        }
      ],
      externalLinkList: [
        {
          displayName: '素材参考',
          externalUrl: 'https://example.com/ref',
          resourceType: 'EXTERNAL_LINK'
        }
      ]
    });
  });

  it('removes pending uploads by ossId before submit or save', () => {
    expect(removeDraftUploadByOssId(uploads, 8001)).toEqual([
      {
        uid: 'upload-2',
        displayName: '样片.mp4',
        ossId: 8002,
        resourceType: 'VIDEO'
      }
    ]);
  });

  it('creates edit draft state from detail payload without polluting pending resources', () => {
    expect(
      createDraftStateFromDetail({
        id: 9001,
        manuscriptCode: 'MR-20260326-0018',
        submitDepartment: '总编室',
        businessStatusLabel: '已退回',
        currentNodeLabel: '待发起人处理',
        title: '已存在稿件',
        processType: 'AUDIT',
        externalManuscriptCode: 'EXT-002',
        mediaChannel: '新华社/要闻',
        authorName: '李四',
        remark: '原备注',
        contentBody: '原正文',
        attachmentList: [
          {
            id: 1,
            displayName: '已入库附件.pdf'
          }
        ],
        externalLinkList: [
          {
            id: 2,
            displayName: '已入库外链',
            externalUrl: 'https://example.com/online'
          }
        ],
        videoList: null,
        videoMarkList: null,
        timelineItems: null,
        permissionMatrix: null
      })
    ).toEqual({
      form: {
        processType: 'AUDIT',
        externalManuscriptCode: 'EXT-002',
        title: '已存在稿件',
        mediaChannel: '新华社/要闻',
        submitDepartment: '总编室',
        authorName: '李四',
        remark: '原备注',
        contentBody: '原正文'
      },
      persistedResources: [
        {
          id: '1',
          typeLabel: '附件',
          displayName: '已入库附件.pdf',
          note: ''
        },
        {
          id: '2',
          typeLabel: '外链',
          displayName: '已入库外链',
          note: 'https://example.com/online'
        }
      ],
      draftUploads: [],
      draftExternalLinks: []
    });
  });
});
