import React, { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import Badge from './Badge';
import { AlertIcon, ChevronDownIcon, FileIcon, SparkIcon } from './Icons';
import { truncate } from '../utils/formatters';
import { generateIssueRecommendation } from '../api/logApi';

function severityTone(severity) {
  switch ((severity || '').toLowerCase()) {
    case 'high':
      return 'danger';
    case 'medium':
      return 'warning';
    case 'low':
      return 'info';
    default:
      return 'neutral';
  }
}

function FindingItem({ issue, analysisId, open, onToggle }) {
  const tone = severityTone(issue.severity);
  const [aiRecommendation, setAiRecommendation] = useState(issue.aiRecommendation || '');
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState('');

  async function handleGenerateRecommendation(event) {
    event.stopPropagation();
    if (aiRecommendation || aiLoading || !analysisId) {
      return;
    }

    setAiLoading(true);
    setAiError('');
    try {
      const response = await generateIssueRecommendation(analysisId, issue.id);
      setAiRecommendation(response.aiRecommendation || '');
    } catch (error) {
      setAiError(error.message || 'AI recommendation is unavailable.');
    } finally {
      setAiLoading(false);
    }
  }

  return (
    <motion.article
      layout
      className={`finding-card finding-card--${tone}`}
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.28, ease: 'easeOut' }}
    >
      <button className="finding-card__trigger" onClick={onToggle} aria-expanded={open}>
        <div className="finding-card__summary">
          <span className="finding-card__icon"><AlertIcon /></span>
          <div>
            <strong>{issue.issueType}</strong>
            <p>{truncate(issue.evidence, open ? 220 : 110)}</p>
          </div>
        </div>
        <div className="finding-card__meta">
          <Badge tone={tone}>{issue.severity}</Badge>
          <Badge tone="neutral">{Math.round(issue.confidenceScore * 100)}% confidence</Badge>
          <motion.span animate={{ rotate: open ? 180 : 0 }} transition={{ duration: 0.22 }}>
            <ChevronDownIcon className="finding-card__chevron" />
          </motion.span>
        </div>
      </button>

      <AnimatePresence initial={false}>
        {open ? (
          <motion.div
            className="finding-card__details"
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.24, ease: 'easeOut' }}
          >
            <div className="finding-card__detail-grid">
              <div>
                <span className="finding-card__label">Service</span>
                <p>{issue.serviceName || 'Unknown'}</p>
              </div>
              <div>
                <span className="finding-card__label">Frequency</span>
                <p>{issue.frequency}</p>
              </div>
            </div>
            <div className="finding-card__detail-row">
              <Badge tone="neutral" icon={FileIcon}>Evidence</Badge>
              <p>{issue.evidence}</p>
            </div>
            <div className="finding-card__detail-row">
              <Badge tone="neutral" icon={SparkIcon}>Recommendation</Badge>
              <p>{issue.recommendation}</p>
            </div>
            {!aiRecommendation ? (
              <div className="finding-card__detail-row">
                <button
                  type="button"
                  className="finding-card__action"
                  onClick={handleGenerateRecommendation}
                  disabled={aiLoading}
                >
                  <Badge tone="primary" icon={SparkIcon}>
                    {aiLoading ? 'Generating...' : 'Generate AI Recommendation'}
                  </Badge>
                </button>
              </div>
            ) : null}
            {aiError ? (
              <div className="finding-card__detail-row">
                <p className="finding-card__ai-error">{aiError}</p>
              </div>
            ) : null}
            {aiRecommendation ? (
              <div className="finding-card__detail-row">
                <Badge tone="neutral" icon={SparkIcon}>AI Recommendation</Badge>
                <p className="finding-card__ai-copy">{aiRecommendation}</p>
                <span className="finding-card__ai-note">AI-generated from the finding evidence and rule-based summary.</span>
              </div>
            ) : null}
          </motion.div>
        ) : null}
      </AnimatePresence>
    </motion.article>
  );
}

export default function FindingsAccordion({ issues, analysisId }) {
  const [openId, setOpenId] = useState(issues?.[0]?.id ?? null);

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
    <section className="panel findings-panel">
      <div className="section-heading">
        <p className="eyebrow">Findings</p>
        <h2>Detected issues and supporting evidence</h2>
        <p className="section-heading__subtitle">Expand each finding for service context, frequency, evidence, and next-step guidance.</p>
      </div>
      <div className="findings-list">
        {issues.map((issue) => (
          <FindingItem
            key={`${issue.id}-${issue.issueType}`}
            issue={issue}
            analysisId={analysisId}
            open={openId === issue.id}
            onToggle={() => setOpenId(openId === issue.id ? null : issue.id)}
          />
        ))}
      </div>
    </section>
  );
}
