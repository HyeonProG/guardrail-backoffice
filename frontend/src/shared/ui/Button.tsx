import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variantClassName: Record<ButtonVariant, string> = {
  primary:
    'border border-sky-900/90 bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] text-white shadow-[0_14px_32px_rgba(15,23,42,0.16)] hover:-translate-y-0.5 hover:shadow-[0_18px_36px_rgba(15,23,42,0.22)] disabled:translate-y-0 disabled:border-slate-300 disabled:bg-slate-300 disabled:text-white disabled:shadow-none',
  secondary:
    'border border-slate-200 bg-white/90 text-slate-800 shadow-sm hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md disabled:translate-y-0 disabled:text-slate-400 disabled:shadow-none',
  ghost:
    'border border-transparent bg-transparent text-slate-600 hover:bg-slate-100/80 hover:text-slate-950 disabled:text-slate-400',
  danger:
    'border border-red-200 bg-red-50 text-red-700 shadow-sm hover:-translate-y-0.5 hover:border-red-300 hover:bg-red-100 hover:shadow-md disabled:translate-y-0 disabled:border-red-100 disabled:bg-red-50 disabled:text-red-300'
};

export function Button({ className = '', variant = 'primary', ...props }: ButtonProps) {
  return (
    <button
      className={`inline-flex h-11 items-center justify-center rounded-xl px-4 text-sm font-semibold transition duration-200 ${variantClassName[variant]} ${className}`}
      {...props}
    />
  );
}
