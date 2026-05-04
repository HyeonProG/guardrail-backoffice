export type FileTargetType = 'PRODUCT';

export type FileAttachment = {
  id: string;
  targetType: FileTargetType;
  targetId: string;
  fileName: string;
  originalFileName: string;
  filePath: string;
  fileSize: number;
  contentType: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
};

export type FileAttachmentPayload = {
  targetType: FileTargetType;
  targetId: string;
  fileName: string;
  originalFileName: string;
  filePath: string;
  fileSize: number;
  contentType: string;
  sortOrder: number;
};
