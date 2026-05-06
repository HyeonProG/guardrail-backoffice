import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { changePassword, getPasswordHistories, getUser, updateUser } from '@/entities/user/api/userApi';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import {
  myAccountSchema,
  passwordChangeSchema,
  type MyAccountFormValues,
  type PasswordChangeFormValues
} from '@/pages/myAccount/myAccountSchema';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { TextField } from '@/shared/ui/TextField';

export function MyAccountPage() {
  const userId = authStorage.getActorId() ?? '';
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [profileConfirmOpen, setProfileConfirmOpen] = useState(false);
  const form = useForm<MyAccountFormValues>({
    resolver: zodResolver(myAccountSchema),
    defaultValues: { email: '', name: '' }
  });
  const passwordForm = useForm<PasswordChangeFormValues>({
    resolver: zodResolver(passwordChangeSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' }
  });

  const userQuery = useQuery({
    queryKey: ['my-account', userId],
    queryFn: () => getUser(userId),
    enabled: Boolean(userId)
  });
  const passwordHistoriesQuery = useQuery({
    queryKey: ['my-account', userId, 'password-histories'],
    queryFn: () => getPasswordHistories(userId),
    enabled: Boolean(userId)
  });

  const latestPasswordHistory = useMemo(
    () => passwordHistoriesQuery.data?.[0] ?? null,
    [passwordHistoriesQuery.data]
  );
  const isTemporaryPassword = latestPasswordHistory?.temporary ?? false;

  useEffect(() => {
    if (userQuery.data) {
      form.reset({
        email: userQuery.data.email,
        name: userQuery.data.name
      });
    }
  }, [form, userQuery.data]);

  const updateMutation = useMutation({
    mutationFn: (values: MyAccountFormValues) =>
      updateUser(userId, {
        email: values.email,
          name: values.name,
          role: (userQuery.data?.role ?? (authStorage.getRole() as UserRole) ?? 'STAFF') as UserRole
        })
  });
  const passwordChangeMutation = useMutation({
    mutationFn: (values: PasswordChangeFormValues) =>
      changePassword(userId, {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword
      }),
    onSuccess: async () => {
      passwordForm.reset();
      setPasswordModalOpen(false);
      await passwordHistoriesQuery.refetch();
    }
  });

  return (
    <div className="space-y-6">
      <section className="surface-card p-8">
        <p className="section-kicker">Profile Center</p>
        <h1 className="mt-3 text-2xl font-semibold text-slate-950">내 정보</h1>
        <p className="mt-2 text-sm text-slate-600">로그인한 사용자 기본 정보를 확인하고 수정합니다.</p>

        {isTemporaryPassword ? (
          <div className="mt-5 rounded-[22px] border border-amber-200 bg-[linear-gradient(180deg,#fffbeb_0%,#fff7d6_100%)] px-4 py-3 text-sm font-medium text-amber-900 shadow-sm">
            현재 임시 비밀번호를 사용 중입니다. 아래에서 비밀번호를 변경해 주세요.
          </div>
        ) : null}

        <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_320px]">
          <div className="space-y-6">
            <form
              className="space-y-4"
              onSubmit={form.handleSubmit(() => {
                setProfileConfirmOpen(true);
              })}
            >
              <TextField label="이메일" error={form.formState.errors.email?.message} {...form.register('email')} />
              <TextField label="이름" error={form.formState.errors.name?.message} {...form.register('name')} />
              <div className="flex flex-wrap gap-3 pt-2">
                <Button disabled={updateMutation.isPending || userQuery.isLoading} type="submit">
                  내 정보 수정
                </Button>
              </div>
              <ErrorMessage error={userQuery.error ?? updateMutation.error} />
            </form>

            <div className="surface-section">
              <h2 className="text-base font-semibold text-slate-950">비밀번호 변경</h2>
              <p className="mt-2 text-sm text-slate-600">현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.</p>
              <div className="mt-5">
                <Button
                  type="button"
                  disabled={passwordHistoriesQuery.isLoading}
                  onClick={() => setPasswordModalOpen(true)}
                >
                  비밀번호 변경
                </Button>
              </div>
              <ErrorMessage error={passwordHistoriesQuery.error} />
            </div>
          </div>

          <aside className="surface-section">
            <h2 className="text-base font-semibold text-slate-950">현재 로그인 정보</h2>
            <dl className="mt-4 space-y-3 text-sm text-slate-600">
              <div>
                <dt className="font-medium text-slate-500">사용자 ID</dt>
                <dd className="mt-1 break-all font-mono text-xs text-slate-700">{userQuery.data?.userId ?? userId ?? '-'}</dd>
              </div>
              <div>
                <dt className="font-medium text-slate-500">역할</dt>
                <dd className="mt-1 text-slate-900">{userQuery.data?.role ?? authStorage.getRole() ?? '-'}</dd>
              </div>
              <div>
                <dt className="font-medium text-slate-500">상태</dt>
                <dd className="mt-1 text-slate-900">{userQuery.data?.status ?? '-'}</dd>
              </div>
              <div>
                <dt className="font-medium text-slate-500">비밀번호 상태</dt>
                <dd className="mt-1 text-slate-900">{isTemporaryPassword ? '임시 비밀번호 사용 중' : '일반 비밀번호'}</dd>
              </div>
            </dl>
          </aside>
        </div>
      </section>

      <Modal
        open={passwordModalOpen}
        title="비밀번호 변경"
        description="현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다."
        onClose={() => {
          setPasswordModalOpen(false);
          passwordForm.reset();
        }}
      >
        <form
          className="space-y-5"
          onSubmit={passwordForm.handleSubmit((values) => passwordChangeMutation.mutate(values))}
        >
          <div className="grid gap-4 rounded-[22px] border border-slate-200 bg-slate-50/80 p-5">
            <TextField
              label="현재 비밀번호"
              type="password"
              autoComplete="current-password"
              error={passwordForm.formState.errors.currentPassword?.message}
              {...passwordForm.register('currentPassword')}
            />
            <TextField
              label="새 비밀번호"
              type="password"
              autoComplete="new-password"
              error={passwordForm.formState.errors.newPassword?.message}
              {...passwordForm.register('newPassword')}
            />
            <TextField
              label="새 비밀번호 확인"
              type="password"
              autoComplete="new-password"
              error={passwordForm.formState.errors.confirmPassword?.message}
              {...passwordForm.register('confirmPassword')}
            />
          </div>
          <ErrorMessage error={passwordChangeMutation.error} />
          <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setPasswordModalOpen(false);
                passwordForm.reset();
              }}
            >
              취소
            </Button>
            <Button
              disabled={passwordChangeMutation.isPending || passwordHistoriesQuery.isLoading}
              type="submit"
            >
              변경하기
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        open={profileConfirmOpen}
        title="내 정보 수정 확인"
        description="입력한 이메일과 이름으로 내 정보를 수정합니다."
        onClose={() => setProfileConfirmOpen(false)}
      >
        <div className="space-y-5">
          <div className="rounded-[22px] border border-slate-200 bg-slate-50/80 p-5">
            <dl className="space-y-4 text-sm text-slate-600">
              <div>
                <dt className="font-medium text-slate-500">이메일</dt>
                <dd className="mt-1 text-base font-medium text-slate-900">{form.getValues('email')}</dd>
              </div>
              <div>
                <dt className="font-medium text-slate-500">이름</dt>
                <dd className="mt-1 text-base font-medium text-slate-900">{form.getValues('name')}</dd>
              </div>
            </dl>
          </div>
          <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">
            <Button type="button" variant="secondary" onClick={() => setProfileConfirmOpen(false)}>
              취소
            </Button>
            <Button
              type="button"
              disabled={updateMutation.isPending}
              onClick={() =>
                updateMutation.mutate(form.getValues(), {
                  onSuccess: () => setProfileConfirmOpen(false)
                })
              }
            >
              수정하기
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
