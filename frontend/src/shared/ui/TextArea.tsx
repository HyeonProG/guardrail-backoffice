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
      <span className={hideLabel ? 'sr-only' : 'mb-2 block text-sm font-semibold tracking-[0.01em] text-[color:var(--color-text)]'}>{label}</span>
      <textarea
        ref={ref}
        id={inputId}
        className={`min-h-32 w-full rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface-alpha)] px-4 py-3 text-sm text-[color:var(--color-text)] outline-none transition placeholder:text-[color:var(--color-muted)] focus:border-[color:var(--color-primary)] focus:bg-[color:var(--color-surface)] focus:ring-4 focus:ring-[color:var(--color-focus)] ${className}`}
        {...props}
      />
      {error ? <span className="mt-2 block text-xs font-semibold text-red-600">{error}</span> : null}
    </label>
  );
});
