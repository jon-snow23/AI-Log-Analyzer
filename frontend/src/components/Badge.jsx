import React from 'react';

export default function Badge({ children, tone = 'default', icon: Icon, className = '' }) {
  return (
    <span className={`ui-badge ui-badge--${tone} ${className}`.trim()}>
      {Icon ? <Icon className="ui-badge__icon" /> : null}
      <span>{children}</span>
    </span>
  );
}
