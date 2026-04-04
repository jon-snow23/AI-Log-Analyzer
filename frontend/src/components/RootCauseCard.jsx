import React from 'react';
import { motion } from 'framer-motion';
import Badge from './Badge';
import { AlertIcon, SparkIcon } from './Icons';

function getSeverity(summary) {
  const errorRatio = summary.totalLogs ? summary.totalError / summary.totalLogs : 0;
  if (summary.totalError >= 100 || errorRatio >= 0.35) return { label: 'High severity', tone: 'danger' };
  if (summary.totalWarn > 0 || summary.totalError > 0) return { label: 'Medium severity', tone: 'warning' };
  return { label: 'Low severity', tone: 'info' };
}

export default function RootCauseCard({ summary }) {
  const severity = getSeverity(summary);
  const confidence = Math.round((summary.confidenceScore || 0) * 100);

  return (
    <motion.section
      className="panel diagnosis-card"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, ease: 'easeOut' }}
    >
      <div className="diagnosis-card__header">
        <div>
          <p className="eyebrow">AI diagnosis engine</p>
          <h2>{summary.primaryRootCause || 'No clear root cause detected'}</h2>
        </div>
        <span className="diagnosis-card__icon"><SparkIcon /></span>
      </div>

      <div className="diagnosis-card__badges">
        <Badge tone={severity.tone} icon={AlertIcon}>{severity.label}</Badge>
        <Badge tone="primary">{confidence}% confidence</Badge>
      </div>

      <p className="diagnosis-card__summary">{summary.summaryText}</p>

      <div className="diagnosis-card__meta">
        <div>
          <span>Dominant error volume</span>
          <strong>{summary.totalError}</strong>
        </div>
        <div>
          <span>Warnings</span>
          <strong>{summary.totalWarn}</strong>
        </div>
        <div>
          <span>Total parsed entries</span>
          <strong>{summary.totalLogs}</strong>
        </div>
      </div>
    </motion.section>
  );
}
