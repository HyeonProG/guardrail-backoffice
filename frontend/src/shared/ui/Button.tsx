import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variantClassName: Record<ButtonVariant, string> = {
  primary:
    'border border-transparent bg-[color:var(--color-primary)] text-[#172554] shadow-[0_12px_24px_rgba(79,209,197,0.26)] hover:-translate-y-0.5 hover:bg-[#3fc9bd] hover:shadow-[0_16px_32px_rgba(79,209,197,0.34)] disabled:translate-y-0 disabled:border-slate-300 disabled:bg-slate-300 disabled:text-white disabled:shadow-none',
  secondary:
    'border border-[color:var(--color-border)] bg-[color:var(--color-surface)] text-[color:var(--color-text)] shadow-sm hover:-translate-y-0.5 hover:bg-[color:var(--color-primary-soft)] hover:shadow-md disabled:translate-y-0 disabled:text-slate-400 disabled:shadow-none',
  ghost:
    'border border-transparent bg-transparent text-[color:var(--color-muted)] hover:bg-[color:var(--color-primary-soft)] hover:text-[color:var(--color-primary)] disabled:text-slate-400',
  danger:
    'border border-rose-200 bg-rose-50 text-rose-700 shadow-sm hover:-translate-y-0.5 hover:border-rose-300 hover:bg-rose-100 hover:shadow-md disabled:translate-y-0 disabled:border-rose-100 disabled:bg-rose-50 disabled:text-rose-300'
};

export function Button({ className = '', variant = 'primary', ...props }: ButtonProps) {
  return (
    <button
      className={`inline-flex h-11 items-center justify-center rounded-2xl px-4 text-sm font-bold transition duration-200 ${variantClassName[variant]} ${className}`}
      {...props}
    />
  );
}
