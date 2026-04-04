import React from 'react';

export default function LogTable({ entries, page, totalPages, onPageChange }) {
  return (
    <section className="panel">
      <p className="eyebrow">Explorer</p>
      <h2>Parsed log entries</h2>
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
            {entries.length ? entries.map((entry) => (
              <tr key={entry.id}>
                <td>{entry.timestamp || 'n/a'}</td>
                <td><span className={`badge ${entry.level?.toLowerCase()}`}>{entry.level}</span></td>
                <td>{entry.serviceName || 'Unknown'}</td>
                <td>{entry.message || entry.rawLine}</td>
                <td>{entry.exceptionType || 'n/a'}</td>
              </tr>
            )) : (
              <tr>
                <td colSpan="5" className="empty-cell">No entries match the current filters.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      <div className="pagination">
        <button onClick={() => onPageChange(Math.max(page - 1, 0))} disabled={page === 0}>Previous</button>
        <span>Page {page + 1} of {Math.max(totalPages, 1)}</span>
        <button onClick={() => onPageChange(Math.min(page + 1, Math.max(totalPages - 1, 0)))} disabled={page >= totalPages - 1}>Next</button>
      </div>
    </section>
  );
}
