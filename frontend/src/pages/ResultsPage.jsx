import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  fetchEntries,
  fetchExport,
  fetchIssues,
  fetchSummary
} from '../api/logApi';
import PageLayout from '../components/PageLayout';
import SummaryCards from '../components/SummaryCards';
import RootCauseCard from '../components/RootCauseCard';
import RecommendationsPanel from '../components/RecommendationsPanel';
import TopErrorsChart from '../components/TopErrorsChart';
import ServiceErrorsChart from '../components/ServiceErrorsChart';
import FindingsAccordion from '../components/FindingsAccordion';
import FilterBar from '../components/FilterBar';
import DataTable from '../components/DataTable';
import Badge from '../components/Badge';
import { ArrowRightIcon, SparkIcon } from '../components/Icons';
import { downloadJson } from '../utils/formatters';

const initialFilters = { level: '', service: '', search: '' };

function SkeletonDashboard() {
  return (
    <section className="skeleton-dashboard" aria-hidden="true">
      <div className="summary-grid">
        {[0, 1, 2, 3].map((item) => <div key={item} className="skeleton-card" />)}
      </div>
      <div className="two-column">
        <div className="skeleton-card skeleton-card--tall" />
        <div className="skeleton-card skeleton-card--tall" />
      </div>
    </section>
  );
}

export default function ResultsPage() {
  const { analysisId } = useParams();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [issues, setIssues] = useState([]);
  const [entriesResponse, setEntriesResponse] = useState({ content: [], page: 0, totalPages: 0 });
  const [filters, setFilters] = useState(initialFilters);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function hydrate(nextFilters = initialFilters, page = 0) {
    if (!analysisId) {
      return;
    }

    const [freshSummary, issuesResponse, entries] = await Promise.all([
      fetchSummary(analysisId),
      fetchIssues(analysisId),
      fetchEntries(analysisId, { ...nextFilters, page, size: 12 })
    ]);

    setSummary(freshSummary);
    setIssues(issuesResponse);
    setEntriesResponse(entries);
  }

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError('');
      try {
        await hydrate();
      } catch (err) {
        if (active) {
          setError(err.message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [analysisId]);

  useEffect(() => {
    if (!analysisId || !summary) return;

    const timeout = setTimeout(async () => {
      try {
        const entries = await fetchEntries(analysisId, { ...filters, page: 0, size: 12 });
        setEntriesResponse(entries);
      } catch (err) {
        setError(err.message);
      }
    }, 250);

    return () => clearTimeout(timeout);
  }, [analysisId, filters, summary]);

  const services = [...new Set(entriesResponse.content.map((entry) => entry.serviceName).filter(Boolean))];

  return (
    <PageLayout>
      <motion.section
        className="workspace-header"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: 'easeOut' }}
      >
        <div>
          <p className="eyebrow">Analysis results</p>
          <h1>Incident diagnostics</h1>
          <p className="workspace-header__copy">Review the AI summary, service impact, recurring failures, and parsed event stream for analysis #{analysisId}.</p>
        </div>
        <div className="workspace-header__actions">
          <Badge tone="primary" icon={SparkIcon}>Results live</Badge>
          <button className="ghost-button" onClick={() => navigate('/analyze')}>
            <ArrowRightIcon className="button-icon" />
            <span>New analysis</span>
          </button>
          {summary ? (
            <button
              className="secondary-button"
              onClick={async () => {
                const payload = await fetchExport(summary.analysisId);
                downloadJson(`analysis-${summary.analysisId}.json`, payload);
              }}
            >
              <span>Export JSON</span>
            </button>
          ) : null}
        </div>
      </motion.section>

      {error ? (
        <div className="error-banner">
          {error} <Link to="/analyze">Go back to input workspace.</Link>
        </div>
      ) : null}

      {loading ? <SkeletonDashboard /> : null}

      {summary ? (
        <>
          <SummaryCards summary={summary} />
          <div className="two-column">
            <RootCauseCard summary={summary} />
            <RecommendationsPanel analysisId={analysisId} recommendations={summary.recommendations} />
          </div>
          <div className="two-column">
            <TopErrorsChart data={summary.topRecurringErrors} />
            <ServiceErrorsChart data={summary.topFailingServices} />
          </div>
          <FindingsAccordion issues={issues} analysisId={analysisId} />
          <FilterBar
            filters={filters}
            onChange={setFilters}
            services={services}
            totalEntries={entriesResponse.content.length}
          />
          <DataTable
            entries={entriesResponse.content}
            page={entriesResponse.page}
            totalPages={entriesResponse.totalPages}
            onPageChange={async (page) => {
              if (!analysisId) return;
              const entries = await fetchEntries(analysisId, { ...filters, page, size: 12 });
              setEntriesResponse(entries);
            }}
          />
        </>
      ) : null}
    </PageLayout>
  );
}
