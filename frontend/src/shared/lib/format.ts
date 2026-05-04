export function formatDateTime(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}

export function shortId(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.slice(0, 8);
}
