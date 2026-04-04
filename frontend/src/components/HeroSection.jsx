import React from 'react';
import { motion } from 'framer-motion';
import Badge from './Badge';
import { ActivityIcon, ArrowRightIcon, FileIcon, SparkIcon } from './Icons';

const heroStats = [
  { label: 'Root-cause summaries', value: 'Deterministic' },
  { label: 'Recurring patterns', value: 'Top clusters' },
  { label: 'Log explorer', value: 'Filterable' }
];

export default function HeroSection({ summary, onUploadLogs, onTrySampleLogs, onExport }) {
  return (
    <section className="hero-card">
      <div className="hero-card__content">
        <motion.div
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45, ease: 'easeOut' }}
        >
          <Badge tone="primary" icon={SparkIcon}>AI Log Analyzer for Production Debugging</Badge>
        </motion.div>

        <motion.div
          className="hero-card__copy"
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.06, ease: 'easeOut' }}
        >
          <h1>Production-grade incident triage for noisy logs, built for fast root-cause analysis.</h1>
          <p>
            Upload raw logs, surface recurring failures, rank likely causes, and move from symptom to diagnosis
            with a dashboard that feels closer to Datadog than a demo.
          </p>
        </motion.div>

        <motion.div
          className="hero-card__actions"
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45, delay: 0.12, ease: 'easeOut' }}
        >
          <button className="primary-button" onClick={onUploadLogs}>
            <FileIcon className="button-icon" />
            <span>Upload logs</span>
          </button>
          <button className="secondary-button" onClick={onTrySampleLogs}>
            <SparkIcon className="button-icon" />
            <span>Try sample logs</span>
          </button>
          {summary ? (
            <button className="ghost-button" onClick={onExport}>
              <ArrowRightIcon className="button-icon" />
              <span>Export analysis</span>
            </button>
          ) : null}
        </motion.div>

        <motion.div
          className="hero-card__stats"
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45, delay: 0.18, ease: 'easeOut' }}
        >
          {heroStats.map((item) => (
            <div key={item.label} className="hero-stat">
              <span className="hero-stat__value">{item.value}</span>
              <span className="hero-stat__label">{item.label}</span>
            </div>
          ))}
        </motion.div>
      </div>

      <motion.aside
        className="hero-card__insight"
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.5, delay: 0.1, ease: 'easeOut' }}
      >
        <div className="insight-card">
          <div className="insight-card__header">
            <Badge tone="neutral" icon={ActivityIcon}>Analysis engine</Badge>
            <span className="insight-card__status">Live</span>
          </div>
          <h2>{summary?.primaryRootCause || 'Waiting for incident data'}</h2>
          <p>
            {summary
              ? summary.summaryText
              : 'Load a sample incident or upload your own logs to populate findings, recommendations, and service impact.'}
          </p>
          <div className="insight-card__footer">
            <span>{summary ? `${summary.totalError} errors detected` : 'No analysis yet'}</span>
            <span>{summary ? `${Math.round((summary.confidenceScore || 0) * 100)}% confidence` : 'Ready to inspect'}</span>
          </div>
        </div>
      </motion.aside>
    </section>
  );
}
