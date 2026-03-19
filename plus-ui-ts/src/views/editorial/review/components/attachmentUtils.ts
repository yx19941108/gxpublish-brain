import { getToken } from '@/utils/auth';

import type { ReviewAttachmentFormItem } from '../model';

export type AttachmentPreviewKind = 'image' | 'pdf' | 'video' | 'other';

const IMAGE_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg']);
const VIDEO_EXTENSIONS = new Set(['mp4', 'webm', 'ogg', 'mov', 'm4v']);

const resolveExtension = (attachment: Pick<ReviewAttachmentFormItem, 'fileName' | 'fileUrl'>) => {
  const source = attachment.fileName || attachment.fileUrl || '';
  const normalized = source.split('?')[0].toLowerCase();
  const index = normalized.lastIndexOf('.');
  return index >= 0 ? normalized.slice(index + 1) : '';
};

export const resolveAttachmentPreviewKind = (attachment: Pick<ReviewAttachmentFormItem, 'fileName' | 'fileUrl'>): AttachmentPreviewKind => {
  const extension = resolveExtension(attachment);
  if (IMAGE_EXTENSIONS.has(extension)) {
    return 'image';
  }
  if (extension === 'pdf') {
    return 'pdf';
  }
  if (VIDEO_EXTENSIONS.has(extension)) {
    return 'video';
  }
  return 'other';
};

export const fetchAttachmentBlob = async (attachment: Pick<ReviewAttachmentFormItem, 'ossId' | 'fileUrl'>): Promise<Blob> => {
  if (attachment.ossId) {
    const response = await fetch(import.meta.env.VITE_APP_BASE_API + '/resource/oss/download/' + attachment.ossId, {
      headers: {
        Authorization: 'Bearer ' + getToken(),
        clientid: import.meta.env.VITE_APP_CLIENT_ID
      }
    });
    const blob = await response.blob();
    const contentType = response.headers.get('content-type') || '';
    if (!response.ok || contentType.includes('application/json')) {
      const errorText = await blob.text();
      throw new Error(errorText || '文件访问失败');
    }
    return blob;
  }

  if (!attachment.fileUrl) {
    throw new Error('缺少可访问的文件地址');
  }

  const response = await fetch(attachment.fileUrl);
  if (!response.ok) {
    throw new Error('文件访问失败');
  }
  return response.blob();
};

export const openBlobDownload = (blob: Blob, fileName: string) => {
  const blobUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = blobUrl;
  a.download = fileName;
  a.target = '_blank';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(blobUrl);
};

export const fallbackPreviewUrl = (attachment: Pick<ReviewAttachmentFormItem, 'fileUrl'>) => attachment.fileUrl || '';
