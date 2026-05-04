export type Session = {
  accessToken: string;
  refreshToken: string;
  sessionId: string;
  accessTokenExpiredAt: string;
  refreshTokenExpiredAt: string;
  userId: string;
  role: string;
  temporaryPassword: boolean;
};
