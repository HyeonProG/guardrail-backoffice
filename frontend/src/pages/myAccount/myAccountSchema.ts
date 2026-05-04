import { z } from 'zod';

export const myAccountSchema = z.object({
  email: z.string().min(1, '이메일을 입력해 주세요.').email('올바른 이메일 형식이 아닙니다.'),
  name: z.string().min(1, '이름을 입력해 주세요.')
});

export type MyAccountFormValues = z.infer<typeof myAccountSchema>;

export const passwordChangeSchema = z
  .object({
    currentPassword: z.string().min(1, '현재 비밀번호를 입력해 주세요.'),
    newPassword: z.string().min(8, '새 비밀번호는 8자 이상이어야 합니다.'),
    confirmPassword: z.string().min(1, '새 비밀번호 확인을 입력해 주세요.')
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    path: ['confirmPassword'],
    message: '새 비밀번호 확인이 일치하지 않습니다.'
  });

export type PasswordChangeFormValues = z.infer<typeof passwordChangeSchema>;
