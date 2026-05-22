import { forwardRef } from 'react';
import type { InputHTMLAttributes } from 'react';

type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export const TextField = forwardRef<HTMLInputElement, TextFieldProps>(function TextField(
  { id, label, error, className = '', ...props },
  ref
) {
  const inputId = id ?? props.name;

  return (
    <label className="block" htmlFor={inputId}>
      <span className="mb-2 block text-sm font-semibold tracking-[0.01em] text-[color:var(--color-text)]">{label}</span>
      <input
        ref={ref}
        id={inputId}
        className={`h-12 w-full rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface-alpha)] px-4 text-sm text-[color:var(--color-text)] shadow-sm outline-none transition placeholder:text-[color:var(--color-muted)] focus:border-[color:var(--color-primary)] focus:bg-[color:var(--color-surface)] focus:ring-4 focus:ring-[color:var(--color-focus)] ${className}`}
        {...props}
      />
      {error ? <span className="mt-2 block text-xs font-semibold text-red-600">{error}</span> : null}
    </label>
  );
});
