import { getApiErrorMessage } from '@/shared/api/unwrap';

type ErrorMessageProps = {
  error: unknown;
};

export function ErrorMessage({ error }: ErrorMessageProps) {
  if (!error) {
    return null;
  }

  return (
    <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {getApiErrorMessage(error)}
    </div>
  );
}
