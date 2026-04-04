import React from 'react';
import { Link } from 'react-router-dom';

export default function TopNav() {
  return (
    <header className="top-nav">
      <Link to="/" className="top-nav__brand">
        <span className="top-nav__brand-mark">AI</span>
        <span>Log Analyzer</span>
      </Link>

      <nav className="top-nav__links" aria-label="Welcome page sections">
        <a href="#about">About</a>
        <a href="#how-it-works">How it works</a>
        <a href="#features">Features</a>
        <Link to="/analyze" className="top-nav__cta">Start analysis</Link>
      </nav>
    </header>
  );
}
