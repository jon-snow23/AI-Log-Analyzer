export function truncate(text, max = 80) {
  if (!text) return 'n/a';
  return text.length > max ? `${text.slice(0, max)}...` : text;
}

export function formatTimestamp(timestamp) {
  if (!timestamp) return 'n/a';

  try {
    return new Intl.DateTimeFormat(undefined, {
      month: 'short',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(new Date(timestamp));
  } catch {
    return timestamp;
  }
}

export function downloadJson(filename, payload) {
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}
