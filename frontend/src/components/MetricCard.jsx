import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';

function useCountUp(target) {
  const endValue = Number(target) || 0;
  const isTestEnvironment = typeof navigator !== 'undefined' && /jsdom/i.test(navigator.userAgent);
  const [value, setValue] = useState(isTestEnvironment ? endValue : 0);

  useEffect(() => {
    if (isTestEnvironment) {
      setValue(endValue);
      return undefined;
    }

    let frame;
    let start;
    const duration = 650;

    function step(timestamp) {
      if (!start) {
        start = timestamp;
      }
      const progress = Math.min((timestamp - start) / duration, 1);
      setValue(Math.round(endValue * (1 - Math.pow(1 - progress, 3))));
      if (progress < 1) {
        frame = window.requestAnimationFrame(step);
      }
    }

    frame = window.requestAnimationFrame(step);
    return () => window.cancelAnimationFrame(frame);
  }, [endValue, isTestEnvironment]);

  return value;
}

export default function MetricCard({ icon: Icon, label, value, tone = 'default', subtitle }) {
  const animatedValue = useCountUp(value);

  return (
    <motion.article
      className={`metric-card metric-card--${tone}`}
      whileHover={{ y: -6, scale: 1.02 }}
      transition={{ duration: 0.22, ease: 'easeOut' }}
    >
      <div className="metric-card__top">
        <span className="metric-card__icon">{Icon ? <Icon /> : null}</span>
        <p>{label}</p>
      </div>
      <h3>{animatedValue}</h3>
      <span className="metric-card__subtitle">{subtitle}</span>
    </motion.article>
  );
}
