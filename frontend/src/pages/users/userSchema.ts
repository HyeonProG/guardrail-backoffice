import { z } from 'zod';

export const userSchema = z.object({
  email: z.string().min(1, '이메일을 입력해 주세요.').email('올바른 이메일 형식이 아닙니다.'),
  name: z.string().min(1, '이름을 입력해 주세요.'),
  role: z.enum(['STAFF', 'OPERATOR', 'ADMIN'], {
    errorMap: () => ({ message: '역할을 선택해 주세요.' })
  })
});

export type UserFormValues = z.infer<typeof userSchema>;
