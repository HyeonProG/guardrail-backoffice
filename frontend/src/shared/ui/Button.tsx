import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'ghost';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variantClassName: Record<ButtonVariant, string> = {
  primary: 'bg-ink text-white hover:bg-slate-800 disabled:bg-slate-400',
  secondary: 'border border-border bg-white text-ink hover:bg-slate-50 disabled:text-slate-400',
  ghost: 'text-slate-600 hover:bg-slate-100 disabled:text-slate-400'
};

export function Button({ className = '', variant = 'primary', ...props }: ButtonProps) {
  return (
    <button
      className={`inline-flex h-10 items-center justify-center rounded-md px-4 text-sm font-medium transition ${variantClassName[variant]} ${className}`}
      {...props}
    />
  );
}
