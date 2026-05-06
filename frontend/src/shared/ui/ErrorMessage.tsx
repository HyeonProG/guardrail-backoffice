import { getApiErrorMessage } from '@/shared/api/unwrap';

type ErrorMessageProps = {
  error: unknown;
};

export function ErrorMessage({ error }: ErrorMessageProps) {
  if (!error) {
    return null;
  }

  return (
    <div className="rounded-2xl border border-red-200/90 bg-[linear-gradient(180deg,#fef2f2_0%,#fff7f7_100%)] px-4 py-3 text-sm font-medium text-red-700 shadow-sm">
      {getApiErrorMessage(error)}
    </div>
  );
}
