import React, { useEffect, useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { useLocation, useNavigate } from 'react-router-dom';
import { analyzeText, uploadFile } from '../api/logApi';
import PageLayout from '../components/PageLayout';
import UploadPanel from '../components/UploadPanel';
import Badge from '../components/Badge';
import { ArrowRightIcon, SparkIcon } from '../components/Icons';

export default function AnalyzePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const uploadPanelRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const actionHandledRef = useRef(false);

  async function handleAnalyzeText(rawLogs) {
    setLoading(true);
    setError('');
    try {
      const analysisSummary = await analyzeText(rawLogs);
      navigate(`/analysis/${analysisSummary.analysisId}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleUploadFile(file) {
    setLoading(true);
    setError('');
    try {
      const analysisSummary = await uploadFile(file);
      navigate(`/analysis/${analysisSummary.analysisId}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (actionHandledRef.current) {
      return;
    }

    if (location.state?.openFilePicker) {
      actionHandledRef.current = true;
      uploadPanelRef.current?.openFilePicker();
      navigate(location.pathname, { replace: true, state: {} });
      return;
    }

    if (location.state?.autoSample) {
      actionHandledRef.current = true;
      uploadPanelRef.current?.trySampleLogs();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location.pathname, location.state, navigate]);

  return (
    <PageLayout>
      <motion.section
        className="workspace-header"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: 'easeOut' }}
      >
        <div>
          <p className="eyebrow">Input workspace</p>
          <h1>Start a new analysis</h1>
          <p className="workspace-header__copy">Upload a log file, paste raw entries, or launch a bundled incident sample to create a new results page.</p>
        </div>
        <div className="workspace-header__actions">
          <Badge tone="primary" icon={SparkIcon}>Analysis-ready</Badge>
          <button className="ghost-button" onClick={() => navigate('/')}>
            <ArrowRightIcon className="button-icon" />
            <span>Back to welcome</span>
          </button>
        </div>
      </motion.section>

      {error ? <div className="error-banner">{error}</div> : null}

      <UploadPanel
        ref={uploadPanelRef}
        onAnalyzeText={handleAnalyzeText}
        onUploadFile={handleUploadFile}
        loading={loading}
      />
    </PageLayout>
  );
}
