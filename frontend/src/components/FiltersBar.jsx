import React from 'react';

export default function FiltersBar({ filters, onChange, services }) {
  return (
    <div className="filters-bar">
      <select value={filters.level} onChange={(event) => onChange({ ...filters, level: event.target.value })}>
        <option value="">All levels</option>
        <option value="INFO">INFO</option>
        <option value="WARN">WARN</option>
        <option value="ERROR">ERROR</option>
      </select>
      <select value={filters.service} onChange={(event) => onChange({ ...filters, service: event.target.value })}>
        <option value="">All services</option>
        {services.map((service) => <option key={service} value={service}>{service}</option>)}
      </select>
      <input
        value={filters.search}
        onChange={(event) => onChange({ ...filters, search: event.target.value })}
        placeholder="Search message or service"
      />
    </div>
  );
}
