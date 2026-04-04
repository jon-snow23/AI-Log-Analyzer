import React from 'react';
import { motion } from 'framer-motion';

export default function PageLayout({ children }) {
  return (
    <div className="page-shell">
      <div className="page-shell__glow page-shell__glow--left" />
      <div className="page-shell__glow page-shell__glow--right" />
      <motion.main
        className="app-shell"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45, ease: 'easeOut' }}
      >
        {children}
      </motion.main>
    </div>
  );
}
