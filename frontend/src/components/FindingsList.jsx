import React from 'react';

export default function FindingsList({ issues }) {
  if (!issues?.length) {
    return (
      <section className="panel">
        <p className="eyebrow">Findings</p>
        <h2>No issues detected</h2>
        <p>This data set did not trigger any rule-based incident findings.</p>
      </section>
    );
  }

  return (
    <section className="panel">
      <p className="eyebrow">Findings</p>
      <h2>Detected issues and evidence</h2>
      <div className="findings-list">
        {issues.map((issue) => (
          <article key={`${issue.id}-${issue.issueType}`} className={`finding ${issue.severity.toLowerCase()}`}>
            <div className="finding-header">
              <strong>{issue.issueType}</strong>
              <span>{issue.severity}</span>
            </div>
            <p><strong>Service:</strong> {issue.serviceName || 'Unknown'}</p>
            <p><strong>Frequency:</strong> {issue.frequency}</p>
            <p><strong>Confidence:</strong> {Math.round(issue.confidenceScore * 100)}%</p>
            <p><strong>Evidence:</strong> {issue.evidence}</p>
            <p><strong>Recommendation:</strong> {issue.recommendation}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
