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
      <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      <input
        ref={ref}
        id={inputId}
        className={`h-10 w-full rounded-md border border-border bg-white px-3 text-sm outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 ${className}`}
        {...props}
      />
      {error ? <span className="mt-1.5 block text-xs font-medium text-red-600">{error}</span> : null}
    </label>
  );
});
