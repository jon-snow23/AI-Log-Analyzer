import React from 'react';
import { motion } from 'framer-motion';

export default function ChartCard({ eyebrow, title, subtitle, children, delay = 0 }) {
  return (
    <motion.section
      className="panel chart-panel"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay, ease: 'easeOut' }}
    >
      <div className="chart-card__header">
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
        <p className="chart-card__subtitle">{subtitle}</p>
      </div>
      {children}
    </motion.section>
  );
}
