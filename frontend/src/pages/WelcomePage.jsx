import React from 'react';
import { useNavigate } from 'react-router-dom';
import PageLayout from '../components/PageLayout';
import HeroSection from '../components/HeroSection';
import TopNav from '../components/TopNav';
import Badge from '../components/Badge';
import { ActivityIcon, ArrowRightIcon, FileIcon, SearchIcon, SparkIcon, TableIcon } from '../components/Icons';

const features = [
  {
    title: 'Deterministic root-cause summaries',
    body: 'Turn raw log noise into a concise incident narrative with recommendations and confidence scoring.',
    icon: SparkIcon
  },
  {
    title: 'Structured upload workspace',
    body: 'Start from uploaded files, pasted logs, or bundled samples without changing the backend pipeline.',
    icon: FileIcon
  },
  {
    title: 'Service impact and pattern discovery',
    body: 'See recurring errors, failing services, and frequency breakdowns in a single results screen.',
    icon: ActivityIcon
  },
  {
    title: 'Filterable event explorer',
    body: 'Inspect parsed entries with service filters, severity filters, search, and pagination.',
    icon: SearchIcon
  }
];

const steps = [
  'Open the input workspace from the landing page.',
  'Upload a log file, paste raw logs, or launch a sample incident.',
  'Review the generated diagnosis, charts, findings, and parsed event stream.'
];

export default function WelcomePage() {
  const navigate = useNavigate();

  return (
    <PageLayout>
      <TopNav />
      <HeroSection
        summary={null}
        onUploadLogs={() => navigate('/analyze', { state: { openFilePicker: true } })}
        onTrySampleLogs={() => navigate('/analyze', { state: { autoSample: true } })}
      />

      <section id="about" className="panel landing-section">
        <div className="section-heading">
          <p className="eyebrow">About</p>
          <h2>Built for debugging production incidents without losing context.</h2>
          <p className="section-heading__subtitle">
            AI Log Analyzer is a focused incident triage surface for engineers who need fast visibility into root causes,
            recurring failures, and noisy service behavior.
          </p>
        </div>

        <div className="landing-section__grid">
          <article className="landing-feature-card landing-feature-card--wide">
            <Badge tone="primary" icon={SparkIcon}>Why it exists</Badge>
            <p>
              Instead of dumping raw logs into a generic viewer, the product creates a guided path from ingestion to
              diagnosis. That keeps the landing page clear, the input page task-focused, and the results page useful.
            </p>
          </article>
          <article className="landing-feature-card">
            <Badge tone="neutral" icon={TableIcon}>Audience</Badge>
            <p>Developers, SREs, support engineers, and anyone investigating failures across noisy distributed systems.</p>
          </article>
        </div>
      </section>

      <section id="how-it-works" className="panel landing-section">
        <div className="section-heading">
          <p className="eyebrow">How it works</p>
          <h2>A simple three-step workflow.</h2>
        </div>
        <div className="hero-card__stats landing-steps">
          {steps.map((step, index) => (
            <div key={step} className="hero-stat landing-step">
              <span className="hero-stat__value">0{index + 1}</span>
              <span className="hero-stat__label">{step}</span>
            </div>
          ))}
        </div>
      </section>

      <section id="features" className="panel landing-section">
        <div className="section-heading section-heading--row">
          <div>
            <p className="eyebrow">Features</p>
            <h2>Everything needed to move from log ingestion to diagnosis.</h2>
          </div>
          <button className="ghost-button" onClick={() => navigate('/analyze')}>
            <ArrowRightIcon className="button-icon" />
            <span>Go to input workspace</span>
          </button>
        </div>

        <div className="landing-features-grid">
          {features.map((feature) => {
            const Icon = feature.icon;
            return (
              <article key={feature.title} className="landing-feature-card">
                <span className="landing-feature-card__icon"><Icon /></span>
                <h3>{feature.title}</h3>
                <p>{feature.body}</p>
              </article>
            );
          })}
        </div>
      </section>
    </PageLayout>
  );
}
