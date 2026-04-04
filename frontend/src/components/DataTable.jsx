import React from 'react';
import { motion } from 'framer-motion';
import { formatTimestamp } from '../utils/formatters';
import Badge from './Badge';
import { TableIcon } from './Icons';

export default function DataTable({ entries, page, totalPages, onPageChange }) {
  return (
    <section className="panel data-table-panel">
      <div className="section-heading section-heading--row">
        <div>
          <p className="eyebrow">Explorer</p>
          <h2>Parsed log entries</h2>
          <p className="section-heading__subtitle">Inspect timestamps, service attribution, message payloads, and exception traces.</p>
        </div>
        <Badge tone="neutral" icon={TableIcon}>Page {page + 1} of {Math.max(totalPages, 1)}</Badge>
      </div>

      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Level</th>
              <th>Service</th>
              <th>Message</th>
              <th>Exception</th>
            </tr>
          </thead>
          <tbody>
            {entries.length ? entries.map((entry, index) => (
              <motion.tr
                key={entry.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.22, delay: Math.min(index * 0.015, 0.12), ease: 'easeOut' }}
              >
                <td>{formatTimestamp(entry.timestamp)}</td>
                <td><span className={`badge ${entry.level?.toLowerCase()}`}>{entry.level}</span></td>
                <td>{entry.serviceName || 'Unknown'}</td>
                <td className="table-message">{entry.message || entry.rawLine}</td>
                <td>{entry.exceptionType || 'n/a'}</td>
              </motion.tr>
            )) : (
              <tr>
                <td colSpan="5" className="empty-cell">No entries match the current filters.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="pagination pagination--card">
        <button onClick={() => onPageChange(Math.max(page - 1, 0))} disabled={page === 0}>Previous</button>
        <span>Showing page {page + 1} of {Math.max(totalPages, 1)}</span>
        <button onClick={() => onPageChange(Math.min(page + 1, Math.max(totalPages - 1, 0)))} disabled={page >= totalPages - 1}>Next</button>
      </div>
    </section>
  );
}
