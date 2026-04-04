import React from 'react';
import { motion } from 'framer-motion';
import MetricCard from './MetricCard';
import { ActivityIcon, AlertIcon, DatabaseIcon, WarningIcon } from './Icons';

export default function SummaryCards({ summary }) {
  const cards = [
    { label: 'Total Logs', value: summary.totalLogs, subtitle: 'Parsed across the uploaded incident window', icon: DatabaseIcon, tone: 'default' },
    { label: 'Info Signals', value: summary.totalInfo, subtitle: 'Low-severity entries retained for context', icon: ActivityIcon, tone: 'info' },
    { label: 'Warnings', value: summary.totalWarn, subtitle: 'Events worth watching before escalation', icon: WarningIcon, tone: 'warning' },
    { label: 'Errors', value: summary.totalError, subtitle: 'Primary failure volume across the dataset', icon: AlertIcon, tone: 'danger' }
  ];

  return (
    <motion.section
      className="summary-grid"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: 'easeOut' }}
    >
      {cards.map((card) => (
        <MetricCard key={card.label} {...card} />
      ))}
    </motion.section>
  );
}
