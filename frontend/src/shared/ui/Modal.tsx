import type { ReactNode } from 'react';
import { createPortal } from 'react-dom';

type ModalTone = 'default' | 'danger' | 'success';
type ModalSize = 'sm' | 'md' | 'lg';

type ModalProps = {
  open: boolean;
  title: string;
  description?: string;
  onClose: () => void;
  children: ReactNode;
  zIndex?: number;
  tone?: ModalTone;
  size?: ModalSize;
  footer?: ReactNode;
};

const toneClassName: Record<ModalTone, string> = {
  default: 'from-[#4fd1c5] to-[#2563eb]',
  danger: 'from-[#fb7185] to-[#ef4444]',
  success: 'from-[#34d399] to-[#16a34a]'
};

const sizeClassName: Record<ModalSize, string> = {
  sm: 'max-w-md',
  md: 'max-w-2xl',
  lg: 'max-w-4xl'
};

const toneLabel: Record<ModalTone, string> = {
  default: 'Dialog',
  danger: 'Confirm',
  success: 'Complete'
};

export function Modal({
  open,
  title,
  description,
  onClose,
  children,
  zIndex = 50,
  tone = 'default',
  size = 'md',
  footer
}: ModalProps) {
  if (!open) {
    return null;
  }

  return createPortal(
    <div
      className="fixed inset-0 flex items-start justify-center overflow-y-auto bg-[color:var(--color-overlay)] px-4 py-10 backdrop-blur-[2px] sm:items-center sm:py-8"
      style={{ zIndex }}
      onClick={onClose}
      role="presentation"
    >
      <div className="relative flex min-h-full w-full items-center justify-center py-2">
        <div
          aria-modal="true"
          className={`animate-modal-in relative max-h-[88vh] w-full ${sizeClassName[size]} overflow-hidden rounded-[28px] border border-[color:var(--color-border)] bg-[color:var(--color-surface)] shadow-[var(--shadow-modal)]`}
          onClick={(event) => event.stopPropagation()}
          role="dialog"
        >
          <div className={`h-1.5 w-full bg-gradient-to-r ${toneClassName[tone]}`} />
          <div className="relative overflow-hidden border-b border-[color:var(--color-border)] bg-[color:var(--color-surface-muted)] px-7 py-6">
            <div className="pointer-events-none absolute -right-12 -top-16 h-44 w-44 rounded-full bg-[color:var(--color-primary-soft)]" />
            <div className="relative flex items-start justify-between gap-6">
              <div className="min-w-0">
                <div className="flex items-center gap-3">
                  <span className={`inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br ${toneClassName[tone]} text-sm font-black text-white shadow-lg`}>
                    {tone === 'danger' ? '!' : tone === 'success' ? '✓' : 'G'}
                  </span>
                  <p className="text-xs font-black uppercase tracking-[0.24em] text-[color:var(--color-primary)]">
                    {toneLabel[tone]}
                  </p>
                </div>
                <h2 className="mt-4 text-2xl font-black tracking-tight text-[color:var(--color-text)]">{title}</h2>
                {description ? (
                  <p className="mt-2 max-w-xl text-sm leading-6 text-[color:var(--color-muted)]">{description}</p>
                ) : null}
              </div>
              <button
                className="inline-flex h-11 shrink-0 items-center justify-center whitespace-nowrap rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface)] px-4 text-sm font-bold text-[color:var(--color-muted)] shadow-sm transition hover:-translate-y-0.5 hover:bg-[color:var(--color-primary-soft)] hover:text-[color:var(--color-primary)]"
                type="button"
                onClick={onClose}
              >
                닫기
              </button>
            </div>
          </div>
          <div className="max-h-[calc(88vh-150px)] overflow-y-auto bg-[color:var(--color-surface)] px-7 py-6">
            {children}
          </div>
          {footer ? (
            <div className="flex justify-end gap-3 border-t border-[color:var(--color-border)] bg-[color:var(--color-surface-muted)] px-7 py-5">
              {footer}
            </div>
          ) : null}
        </div>
      </div>
    </div>,
    document.body
  );
}
