import type { FileAttachment, FileAttachmentPayload, FileTargetType } from '@/entities/file/model/types';
import { apiClient } from '@/shared/api/apiClient';
import { unwrapResult } from '@/shared/api/unwrap';

export async function createFileAttachment(request: FileAttachmentPayload) {
  const response = await apiClient.post('/api/v1/file-attachments', request);
  return unwrapResult<FileAttachment>(response);
}

export async function uploadFileAttachment(request: {
  targetType: FileTargetType;
  targetId: string;
  sortOrder: number;
  file: File;
}) {
  const formData = new FormData();
  formData.append('targetType', request.targetType);
  formData.append('targetId', request.targetId);
  formData.append('sortOrder', String(request.sortOrder));
  formData.append('file', request.file);

  const response = await apiClient.post('/api/v1/file-attachments/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return unwrapResult<FileAttachment>(response);
}

export async function getFileAttachments(targetType: FileTargetType, targetId: string) {
  const response = await apiClient.get('/api/v1/file-attachments', {
    params: { targetType, targetId }
  });
  return unwrapResult<FileAttachment[]>(response);
}

export async function deleteFileAttachment(fileAttachmentId: string) {
  const response = await apiClient.delete(`/api/v1/file-attachments/${fileAttachmentId}`);
  return unwrapResult<null>(response);
}
