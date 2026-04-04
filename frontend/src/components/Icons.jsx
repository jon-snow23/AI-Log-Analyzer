import React from 'react';

function createIcon(path) {
  return function Icon({ className }) {
    return (
      <svg
        className={className}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        {path}
      </svg>
    );
  };
}

export const UploadIcon = createIcon(
  <>
    <path d="M12 16V4" />
    <path d="m7 9 5-5 5 5" />
    <path d="M4 16.5v1.5A2 2 0 0 0 6 20h12a2 2 0 0 0 2-2v-1.5" />
  </>
);

export const SparkIcon = createIcon(
  <>
    <path d="m12 3 1.4 3.6L17 8l-3.6 1.4L12 13l-1.4-3.6L7 8l3.6-1.4L12 3Z" />
    <path d="m5 14 .8 2.2L8 17l-2.2.8L5 20l-.8-2.2L2 17l2.2-.8L5 14Z" />
    <path d="m19 13 .8 2.2L22 16l-2.2.8L19 19l-.8-2.2L16 16l2.2-.8.8-2.2Z" />
  </>
);

export const ActivityIcon = createIcon(
  <>
    <path d="M3 12h4l2.5-6 5 12 2.5-6H21" />
  </>
);

export const AlertIcon = createIcon(
  <>
    <path d="M12 9v4" />
    <path d="M12 17h.01" />
    <path d="M10.3 3.7 2.9 17a2 2 0 0 0 1.7 3h14.8a2 2 0 0 0 1.7-3L13.7 3.7a2 2 0 0 0-3.4 0Z" />
  </>
);

export const WarningIcon = createIcon(
  <>
    <path d="M12 8v5" />
    <path d="M12 17h.01" />
    <path d="M10.3 3.7 2.9 17a2 2 0 0 0 1.7 3h14.8a2 2 0 0 0 1.7-3L13.7 3.7a2 2 0 0 0-3.4 0Z" />
  </>
);

export const DatabaseIcon = createIcon(
  <>
    <ellipse cx="12" cy="5" rx="7" ry="3" />
    <path d="M5 5v6c0 1.7 3.1 3 7 3s7-1.3 7-3V5" />
    <path d="M5 11v6c0 1.7 3.1 3 7 3s7-1.3 7-3v-6" />
  </>
);

export const SearchIcon = createIcon(
  <>
    <circle cx="11" cy="11" r="7" />
    <path d="m20 20-3.5-3.5" />
  </>
);

export const ChevronDownIcon = createIcon(
  <>
    <path d="m6 9 6 6 6-6" />
  </>
);

export const TableIcon = createIcon(
  <>
    <rect x="3" y="4" width="18" height="16" rx="2" />
    <path d="M3 10h18" />
    <path d="M9 4v16" />
    <path d="M15 4v16" />
  </>
);

export const FileIcon = createIcon(
  <>
    <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8Z" />
    <path d="M14 3v5h5" />
  </>
);

export const ArrowRightIcon = createIcon(
  <>
    <path d="M5 12h14" />
    <path d="m13 6 6 6-6 6" />
  </>
);

export const ClockIcon = createIcon(
  <>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v6l4 2" />
  </>
);
