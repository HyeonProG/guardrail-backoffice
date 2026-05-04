import type { ReactNode } from 'react';

type ModalProps = {
  open: boolean;
  title: string;
  description?: string;
  onClose: () => void;
  children: ReactNode;
  zIndex?: number;
};

export function Modal({ open, title, description, onClose, children, zIndex = 50 }: ModalProps) {
  if (!open) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 flex items-center justify-center bg-slate-950/55 px-4 py-8 backdrop-blur-sm"
      style={{ zIndex }}
    >
      <div className="max-h-[88vh] w-full max-w-2xl overflow-hidden rounded-[28px] border border-slate-200/80 bg-white shadow-[0_30px_90px_rgba(15,23,42,0.22)]">
        <div className="flex items-start justify-between border-b border-slate-200/80 px-7 py-6">
          <div>
            <h2 className="text-2xl font-semibold tracking-tight text-slate-950">{title}</h2>
            {description ? <p className="mt-2 text-sm leading-6 text-slate-500">{description}</p> : null}
          </div>
          <button
            className="inline-flex h-11 min-w-11 items-center justify-center rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-600 transition hover:border-slate-300 hover:bg-slate-50"
            type="button"
            onClick={onClose}
          >
            닫기
          </button>
        </div>
        <div className="max-h-[calc(88vh-108px)] overflow-y-auto px-7 py-6">{children}</div>
      </div>
    </div>
  );
}
