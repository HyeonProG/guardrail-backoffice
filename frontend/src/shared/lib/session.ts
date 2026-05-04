import { authStorage } from '@/features/auth/model/authStorage';

export function requireActorId() {
  const actorId = authStorage.getActorId();

  if (!actorId) {
    throw new Error('로그인 사용자 ID를 찾을 수 없습니다. 다시 로그인해 주세요.');
  }

  return actorId;
}
