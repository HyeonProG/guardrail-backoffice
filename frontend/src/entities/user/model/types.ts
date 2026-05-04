export type UserRole = 'STAFF' | 'OPERATOR' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export type User = {
  userId: string;
  email: string;
  name: string;
  role: UserRole;
  status: UserStatus;
};

export type PasswordHistory = {
  passwordHistoryId: string;
  userId: string;
  temporary: boolean;
  expiredAt: string | null;
  createdAt: string;
};

export type ChangePasswordResponse = {
  userId: string;
  temporary: boolean;
  changedAt: string;
};

export type TemporaryPasswordIssueResponse = {
  userId: string;
  email: string;
  temporary: boolean;
  issuedAt: string;
};
