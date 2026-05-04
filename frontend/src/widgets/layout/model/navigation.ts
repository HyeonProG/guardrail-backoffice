import type { UserRole } from '@/entities/user/model/types';

export type NavigationLeafItem = {
  label: string;
  path: string;
  description?: string;
  allowedRoles?: UserRole[];
};

export type NavigationGroup = {
  label: string;
  items: NavigationLeafItem[];
};

export const navigationGroups: NavigationGroup[] = [
  {
    label: '상품 운영',
    items: [
      {
        label: '메인',
        path: '/dashboard',
        description: '승인 완료 상품과 운영 현황'
      },
      {
        label: '상품 관리',
        path: '/products',
        description: '승인 전 단계 상품 조회와 상세 관리'
      },
      {
        label: '승인 완료 상품',
        path: '/products/approved',
        description: '최종 승인된 상품 목록'
      },
      {
        label: '승인 요청 관리',
        path: '/approval-requests',
        description: '대기 중인 상품 승인과 반려',
        allowedRoles: ['ADMIN', 'OPERATOR']
      }
    ]
  },
  {
    label: '기준 정보',
    items: [
      {
        label: '카테고리 관리',
        path: '/categories',
        description: '카테고리 계층과 상태 관리'
      },
      {
        label: '상품 옵션 관리',
        path: '/product-options',
        description: '카테고리별 옵션과 선택 항목 관리'
      },
      {
        label: '사용자 관리',
        path: '/users',
        description: '직원 계정 생성과 상태 관리',
        allowedRoles: ['ADMIN', 'OPERATOR']
      },
      {
        label: '내 정보',
        path: '/my-account',
        description: '로그인 사용자 정보 확인과 수정'
      }
    ]
  }
];

export const navigationItems = navigationGroups.flatMap((group) => group.items);
