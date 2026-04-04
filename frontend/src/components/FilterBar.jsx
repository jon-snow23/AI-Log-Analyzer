import React from 'react';
import Badge from './Badge';
import { FileIcon, SearchIcon } from './Icons';

export default function FilterBar({ filters, onChange, services, totalEntries }) {
  return (
    <section className="filter-bar">
      <div className="filter-bar__header">
        <div>
          <p className="eyebrow">Explorer controls</p>
          <h3>Filter the parsed event stream</h3>
        </div>
        <Badge tone="neutral" icon={FileIcon}>{totalEntries} visible rows</Badge>
      </div>

      <div className="filter-bar__controls">
        <label className="field">
          <span>Level</span>
          <select value={filters.level} onChange={(event) => onChange({ ...filters, level: event.target.value })}>
            <option value="">All levels</option>
            <option value="INFO">INFO</option>
            <option value="WARN">WARN</option>
            <option value="ERROR">ERROR</option>
          </select>
        </label>

        <label className="field">
          <span>Service</span>
          <select value={filters.service} onChange={(event) => onChange({ ...filters, service: event.target.value })}>
            <option value="">All services</option>
            {services.map((service) => <option key={service} value={service}>{service}</option>)}
          </select>
        </label>

        <label className="field field--search">
          <span>Search</span>
          <div className="search-input">
            <SearchIcon className="search-input__icon" />
            <input
              value={filters.search}
              onChange={(event) => onChange({ ...filters, search: event.target.value })}
              placeholder="Search message or service"
              aria-label="Search message or service"
            />
          </div>
        </label>
      </div>
    </section>
  );
}
