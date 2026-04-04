import React from 'react';

export default function ChartTooltip({ active, label, payload, valueLabel }) {
  if (!active || !payload?.length) {
    return null;
  }

  const value = payload[0]?.value;

  return (
    <div className="chart-tooltip">
      <p className="chart-tooltip__title">{label}</p>
      <p className="chart-tooltip__value">
        <span>{valueLabel}:</span> {value}
      </p>
    </div>
  );
}
