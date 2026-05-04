import { Navigate, useLocation } from 'react-router-dom';
import type { PropsWithChildren } from 'react';
import { authStorage } from '@/features/auth/model/authStorage';
import type { UserRole } from '@/entities/user/model/types';

type ProtectedRouteProps = PropsWithChildren<{
  allowedRoles?: UserRole[];
}>;

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const location = useLocation();
  const token = authStorage.getAccessToken();
  const role = authStorage.getRole() as UserRole | null;

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (allowedRoles && (!role || !allowedRoles.includes(role))) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
