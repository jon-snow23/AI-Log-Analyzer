import React, { useState } from 'react';
import { motion } from 'framer-motion';
import RecommendationCard from './RecommendationCard';
import { generateOverallRecommendations } from '../api/logApi';
import Badge from './Badge';
import { SparkIcon } from './Icons';

export default function RecommendationsPanel({ analysisId, recommendations }) {
  const [aiRecommendations, setAiRecommendations] = useState([]);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState('');

  async function handleGenerateAiRecommendations() {
    if (!analysisId || aiLoading || aiRecommendations.length) {
      return;
    }

    setAiLoading(true);
    setAiError('');
    try {
      const response = await generateOverallRecommendations(analysisId);
      setAiRecommendations(response.recommendations || []);
    } catch (error) {
      setAiError(error.message || 'AI recommendations are unavailable.');
    } finally {
      setAiLoading(false);
    }
  }

  return (
    <motion.section
      className="panel recommendations-panel"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay: 0.05, ease: 'easeOut' }}
    >
      <div className="section-heading">
        <p className="eyebrow">Next steps</p>
        <h2>Recommendations</h2>
        <p className="section-heading__subtitle">Operational guidance generated from the strongest recurring patterns in the current dataset.</p>
      </div>
      {!aiRecommendations.length ? (
        <div className="recommendations-grid">
          {recommendations?.map((item, index) => <RecommendationCard key={item} index={index}>{item}</RecommendationCard>)}
        </div>
      ) : null}
      {!aiRecommendations.length ? (
        <div className="recommendations-panel__actions">
          <button type="button" className="ghost-button" onClick={handleGenerateAiRecommendations} disabled={aiLoading}>
            <SparkIcon className="button-icon" />
            <span>{aiLoading ? 'Generating AI recommendations...' : 'Generate AI Recommendations'}</span>
          </button>
        </div>
      ) : null}
      {aiError ? <p className="recommendations-panel__error">{aiError}</p> : null}
      {aiRecommendations.length ? (
        <div className="recommendations-panel__ai">
          <div className="recommendations-panel__ai-header">
            <Badge tone="neutral" icon={SparkIcon}>AI Recommendations</Badge>
            <p className="recommendations-panel__ai-copy">Prioritized next steps generated from the structured analysis context.</p>
          </div>
          <div className="recommendations-grid">
            {aiRecommendations.map((item, index) => (
              <RecommendationCard key={`${item}-${index}`} index={index}>{item}</RecommendationCard>
            ))}
          </div>
        </div>
      ) : null}
    </motion.section>
  );
}
