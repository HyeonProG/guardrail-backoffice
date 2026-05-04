import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  createUser,
  getUsers,
  issueTemporaryPassword,
  updateUserStatus
} from '@/entities/user/api/userManagementApi';
import type { User, UserStatus } from '@/entities/user/model/types';
import { userSchema, type UserFormValues } from '@/pages/users/userSchema';
import { shortId } from '@/shared/lib/format';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { TextField } from '@/shared/ui/TextField';

const statusOptions: Array<UserStatus | ''> = ['', 'ACTIVE', 'INACTIVE'];

export function UsersPage() {
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<UserStatus | ''>('');
  const [createdPassword, setCreatedPassword] = useState<string | null>(null);
  const [createdEmail, setCreatedEmail] = useState<string | null>(null);
  const [temporaryPasswordTarget, setTemporaryPasswordTarget] = useState<User | null>(null);
  const [temporaryPasswordIssuedEmail, setTemporaryPasswordIssuedEmail] = useState<string | null>(null);
  const [confirmState, setConfirmState] = useState<{
    title: string;
    description: string;
    actionLabel: string;
    onConfirm: () => void;
  } | null>(null);
  const form = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: { email: '', name: '', role: 'STAFF' }
  });

  const usersQuery = useQuery({
    queryKey: ['users', statusFilter],
    queryFn: () => getUsers({ status: statusFilter })
  });

  const createMutation = useMutation({
    mutationFn: (values: UserFormValues) => createUser(values),
    onSuccess: (result) => {
      form.reset({ email: '', name: '', role: 'STAFF' });
      setCreatedPassword(result.initialPassword);
      setCreatedEmail(result.email);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    }
  });

  const statusMutation = useMutation({
    mutationFn: ({ userId, status }: { userId: string; status: UserStatus }) => updateUserStatus(userId, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] })
  });
  const temporaryPasswordMutation = useMutation({
    mutationFn: (userId: string) => issueTemporaryPassword(userId),
    onSuccess: (result) => {
      setTemporaryPasswordTarget(null);
      setTemporaryPasswordIssuedEmail(result.email);
    }
  });

  const users = usersQuery.data?.content ?? [];

  return (
    <div className="space-y-6">
      <section>
        <h1 className="text-2xl font-semibold text-slate-950">사용자 관리</h1>
        <p className="mt-2 text-sm text-slate-600">관리자와 운영자는 사용자 계정을 생성하고 상태를 관리할 수 있습니다.</p>
      </section>

      <section className="grid gap-6 xl:grid-cols-[420px_1fr]">
        <div className="rounded-[28px] border border-slate-200/80 bg-white/90 p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-950">사용자 추가</h2>
          <form
            className="mt-5 space-y-4"
            onSubmit={form.handleSubmit((values) =>
              setConfirmState({
                title: '사용자 생성 확인',
                description: `${values.email} 계정을 생성하시겠습니까?`,
                actionLabel: '생성하기',
                onConfirm: () => createMutation.mutate(values)
              })
            )}
          >
            <TextField label="이메일" error={form.formState.errors.email?.message} {...form.register('email')} />
            <TextField label="이름" error={form.formState.errors.name?.message} {...form.register('name')} />
            <label className="block">
              <span className="mb-1.5 block text-sm font-medium text-slate-700">역할</span>
              <select className="h-10 w-full rounded-md border border-border bg-white px-3 text-sm" {...form.register('role')}>
                <option value="STAFF">STAFF</option>
                <option value="OPERATOR">OPERATOR</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </label>
            <ErrorMessage error={createMutation.error} />
            <Button disabled={createMutation.isPending} type="submit">
              사용자 생성
            </Button>
          </form>

          {createdPassword ? (
            <div className="mt-5 rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
              <p className="text-sm font-semibold text-emerald-800">초기 계정 정보</p>
              <p className="mt-2 text-sm text-emerald-700">이메일: {createdEmail}</p>
              <p className="mt-1 break-all text-sm text-emerald-700">초기 비밀번호: {createdPassword}</p>
            </div>
          ) : null}
          {temporaryPasswordIssuedEmail ? (
            <div className="mt-4 rounded-2xl border border-sky-200 bg-sky-50 p-4">
              <p className="text-sm font-semibold text-sky-800">임시 비밀번호 발급 완료</p>
              <p className="mt-2 text-sm text-sky-700">
                {temporaryPasswordIssuedEmail} 주소로 임시 비밀번호 발송을 기록했습니다.
              </p>
            </div>
          ) : null}
        </div>

        <div className="overflow-hidden rounded-[28px] border border-slate-200/80 bg-white/90 shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200/70 px-6 py-5">
            <div>
              <h2 className="text-lg font-semibold text-slate-950">사용자 목록</h2>
              <p className="mt-1 text-sm text-slate-500">현재 등록된 사용자 계정과 역할, 상태를 확인합니다.</p>
            </div>
            <select
              className="h-10 rounded-md border border-border bg-white px-3 text-sm"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value as UserStatus | '')}
            >
              {statusOptions.map((status) => (
                <option key={status || 'ALL'} value={status}>
                  {status || '전체 상태'}
                </option>
              ))}
            </select>
          </div>

          <table className="w-full min-w-[820px] text-left text-sm">
            <thead className="bg-slate-50 text-xs font-semibold uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">이메일</th>
                <th className="px-4 py-3">이름</th>
                <th className="px-4 py-3">역할</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3">액션</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {users.map((user) => (
                <tr key={user.userId}>
                  <td className="px-4 py-3 font-mono text-xs text-slate-600">{shortId(user.userId)}</td>
                  <td className="px-4 py-3 text-slate-700">{user.email}</td>
                  <td className="px-4 py-3 font-medium text-slate-950">{user.name}</td>
                  <td className="px-4 py-3">{user.role}</td>
                  <td className="px-4 py-3">{user.status}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      {user.status !== 'ACTIVE' ? (
                        <button
                          className="inline-flex h-9 items-center justify-center rounded-md border border-border px-3 text-sm font-medium text-ink hover:bg-slate-50"
                          onClick={() =>
                            setConfirmState({
                              title: '사용자 활성화 확인',
                              description: `${user.email} 계정을 활성화하시겠습니까?`,
                              actionLabel: '활성화',
                              onConfirm: () => statusMutation.mutate({ userId: user.userId, status: 'ACTIVE' })
                            })
                          }
                          type="button"
                        >
                          활성화
                        </button>
                      ) : (
                        <button
                          className="inline-flex h-9 items-center justify-center rounded-md border border-border px-3 text-sm font-medium text-ink hover:bg-slate-50"
                          onClick={() =>
                            setConfirmState({
                              title: '사용자 비활성화 확인',
                              description: `${user.email} 계정을 비활성화하시겠습니까?`,
                              actionLabel: '비활성화',
                              onConfirm: () => statusMutation.mutate({ userId: user.userId, status: 'INACTIVE' })
                            })
                          }
                          type="button"
                        >
                          비활성화
                        </button>
                      )}
                      <button
                        className="inline-flex h-9 items-center justify-center rounded-md border border-border px-3 text-sm font-medium text-ink hover:bg-slate-50"
                        onClick={() => setTemporaryPasswordTarget(user)}
                        type="button"
                      >
                        임시 비밀번호 발급
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {usersQuery.isLoading ? <div className="p-5 text-sm text-slate-600">조회 중입니다.</div> : null}
          {!usersQuery.isLoading && users.length === 0 ? <div className="p-5 text-sm text-slate-600">조회된 사용자가 없습니다.</div> : null}
          <div className="px-6 pb-5">
            <ErrorMessage error={usersQuery.error ?? statusMutation.error} />
          </div>
        </div>
      </section>

      <Modal
        open={Boolean(temporaryPasswordTarget)}
        title="임시 비밀번호 발급"
        description={
          temporaryPasswordTarget
            ? `${temporaryPasswordTarget.email} 주소로 임시 비밀번호를 발급하고 메일 발송을 기록합니다. 계속 진행하시겠습니까?`
            : ''
        }
        onClose={() => setTemporaryPasswordTarget(null)}
      >
        <div className="space-y-4">
          <ErrorMessage error={temporaryPasswordMutation.error} />
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setTemporaryPasswordTarget(null)}>
              취소
            </Button>
            <Button
              type="button"
              disabled={!temporaryPasswordTarget || temporaryPasswordMutation.isPending}
              onClick={() =>
                temporaryPasswordTarget &&
                temporaryPasswordMutation.mutate(temporaryPasswordTarget.userId)
              }
            >
              확인 후 발급
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        open={Boolean(confirmState)}
        title={confirmState?.title ?? ''}
        description={confirmState?.description ?? ''}
        onClose={() => setConfirmState(null)}
      >
        <div className="flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={() => setConfirmState(null)}>
            취소
          </Button>
          <Button
            type="button"
            onClick={() => {
              confirmState?.onConfirm();
              setConfirmState(null);
            }}
          >
            {confirmState?.actionLabel ?? '확인'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
