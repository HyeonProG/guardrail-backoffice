import { forwardRef } from 'react';
import type { TextareaHTMLAttributes } from 'react';

type TextAreaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  label: string;
  error?: string;
  hideLabel?: boolean;
};

export const TextArea = forwardRef<HTMLTextAreaElement, TextAreaProps>(function TextArea(
  { id, label, error, className = '', hideLabel = false, ...props },
  ref
) {
  const inputId = id ?? props.name;

  return (
    <label className="block" htmlFor={inputId}>
      <span className={hideLabel ? 'sr-only' : 'mb-1.5 block text-sm font-medium text-slate-700'}>{label}</span>
      <textarea
        ref={ref}
        id={inputId}
        className={`min-h-28 w-full rounded-md border border-border bg-white px-3 py-2 text-sm outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 ${className}`}
        {...props}
      />
      {error ? <span className="mt-1.5 block text-xs font-medium text-red-600">{error}</span> : null}
    </label>
  );
});
