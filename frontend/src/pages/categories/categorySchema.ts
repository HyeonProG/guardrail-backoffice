import { z } from 'zod';

export const categorySchema = z.object({
  parentId: z.string(),
  name: z.string().min(1, '카테고리명을 입력해 주세요.')
});

export type CategoryFormValues = z.infer<typeof categorySchema>;
