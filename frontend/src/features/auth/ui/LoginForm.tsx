import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { login } from '@/features/auth/api/login';
import { authStorage } from '@/features/auth/model/authStorage';
import { loginSchema, type LoginFormValues } from '@/features/auth/model/loginSchema';
import { Button } from '@/shared/ui/Button';
import { TextField } from '@/shared/ui/TextField';

export function LoginForm() {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: ''
    }
  });

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (session) => {
      authStorage.setSession(session);
      navigate('/dashboard', { replace: true });
    }
  });

  const onSubmit = (values: LoginFormValues) => {
    loginMutation.mutate({
      ...values,
      deviceType: 'WEB'
    });
  };

  return (
    <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
      <TextField
        label="이메일"
        type="email"
        autoComplete="email"
        error={errors.email?.message}
        {...register('email')}
      />
      <TextField
        label="비밀번호"
        type="password"
        autoComplete="current-password"
        error={errors.password?.message}
        {...register('password')}
      />
      {loginMutation.isError ? (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          로그인에 실패했습니다. 계정 정보를 확인해 주세요.
        </div>
      ) : null}
      <Button className="w-full" disabled={loginMutation.isPending} type="submit">
        {loginMutation.isPending ? '로그인 중' : '로그인'}
      </Button>
    </form>
  );
}
