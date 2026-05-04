import { env } from '@/shared/config/env';

export function resolveFileUrl(filePath: string | null | undefined) {
  if (!filePath) {
    return null;
  }

  if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
    return filePath;
  }

  return `${env.apiBaseUrl}${filePath}`;
}
