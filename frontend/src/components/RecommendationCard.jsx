import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRightIcon } from './Icons';

export default function RecommendationCard({ children, index }) {
  return (
    <motion.article
      className="recommendation-card"
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.28, delay: index * 0.05, ease: 'easeOut' }}
      whileHover={{ y: -4 }}
    >
      <span className="recommendation-card__icon">
        <ArrowRightIcon />
      </span>
      <p>{children}</p>
    </motion.article>
  );
}
