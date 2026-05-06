import type { ReactNode } from 'react';
import { createPortal } from 'react-dom';

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

  return createPortal(
    <div
      className="fixed inset-0 flex items-center justify-center bg-[rgba(248,250,252,0.82)] px-4 py-8"
      style={{ zIndex }}
      onClick={onClose}
    >
      <div className="relative flex h-full items-center justify-center">
        <div
          className="max-h-[88vh] w-full max-w-2xl overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-[0_28px_80px_rgba(15,23,42,0.28)]"
          onClick={(event) => event.stopPropagation()}
        >
          <div className="h-1.5 w-full bg-[linear-gradient(90deg,#0f172a_0%,#0369a1_55%,#38bdf8_100%)]" />
          <div className="flex items-start justify-between border-b border-slate-200 px-7 py-6">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-500">Dialog</p>
            <h2 className="mt-3 text-2xl font-semibold tracking-tight text-slate-950">{title}</h2>
            {description ? <p className="mt-2 max-w-xl text-sm leading-6 text-slate-500">{description}</p> : null}
          </div>
          <button
            className="inline-flex h-11 min-w-11 items-center justify-center rounded-2xl border border-slate-200 bg-white px-3 text-sm font-semibold text-slate-600 transition hover:border-slate-300 hover:bg-slate-50"
            type="button"
            onClick={onClose}
          >
            닫기
          </button>
        </div>
          <div className="max-h-[calc(88vh-110px)] overflow-y-auto bg-white px-7 py-6">{children}</div>
        </div>
      </div>
    </div>,
    document.body
  );
}
