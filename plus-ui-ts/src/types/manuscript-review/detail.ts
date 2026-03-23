export type ManuscriptReviewDetailViewRole = 'CURRENT_APPROVER' | 'RETURNED_INITIATOR' | 'HISTORY_PARTICIPANT';

export type ManuscriptReviewDetailActionKey = 'edit' | 'approve' | 'resubmit' | 'back';

export interface ManuscriptReviewDetailAction {
  key: ManuscriptReviewDetailActionKey;
  label: string;
}

export const APPROVE_BLOCKER_MESSAGE = '当前流程尚未生成可办理待办，请先确认流程实例与办理页配置已完成。';

export interface ManuscriptReviewApproveJumpLocation {
  path: string;
  query: {
    id: string;
    type: 'approval';
    taskId: string | number;
  };
}

export type ManuscriptReviewApproveActionResult =
  | {
      kind: 'jump';
      location: ManuscriptReviewApproveJumpLocation;
    }
  | {
      kind: 'blocked';
      message: string;
    };

export interface ManuscriptReviewWorkflowInstanceInfo {
  formPath?: string | null;
}

export interface ManuscriptReviewWorkflowTaskInfo {
  id?: string | number | null;
  flowStatus?: string | null;
}

export interface ManuscriptReviewWorkflowTaskPayload {
  list?: ManuscriptReviewWorkflowTaskInfo[] | null;
}

const pickCurrentApprovalTask = (taskPayload?: ManuscriptReviewWorkflowTaskPayload | null): ManuscriptReviewWorkflowTaskInfo | null => {
  const tasks = taskPayload?.list?.filter((task) => task?.id != null) ?? [];

  if (tasks.length === 0) {
    return null;
  }

  return tasks.find((task) => task.flowStatus === 'WAITING') ?? tasks[0] ?? null;
};

export const resolveManuscriptApproveAction = (
  reviewId: string,
  instanceInfo?: ManuscriptReviewWorkflowInstanceInfo | null,
  taskPayload?: ManuscriptReviewWorkflowTaskPayload | null
): ManuscriptReviewApproveActionResult => {
  const formPath = instanceInfo?.formPath?.trim();
  const currentTask = pickCurrentApprovalTask(taskPayload);

  if (!reviewId || !formPath || !currentTask?.id) {
    return {
      kind: 'blocked',
      message: APPROVE_BLOCKER_MESSAGE
    };
  }

  return {
    kind: 'jump',
    location: {
      path: formPath,
      query: {
        id: reviewId,
        type: 'approval',
        taskId: currentTask.id
      }
    }
  };
};

export interface ManuscriptReviewReadableSummaryItem {
  label: string;
  value: string;
}

export interface ManuscriptReviewDetailReadableSource {
  flowStatusLabel: string;
  currentNodeLabel: string;
  initiatorName: string;
  latestUpdatedAt: string;
  processTypeLabel: string;
  manuscriptCode: string;
  externalManuscriptCode?: string;
  title: string;
  mediaChannelLabel: string;
  submitterDeptName: string;
  authorNames?: string;
  note?: string;
  contentPreview: string;
  technicalId?: string;
  initiatorAccount?: string;
  flowStatusCode?: string;
  currentNodeCode?: string;
  processTypeCode?: string;
  roleKey?: string;
}
