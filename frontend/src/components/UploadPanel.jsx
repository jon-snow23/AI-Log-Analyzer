import React, { forwardRef, useImperativeHandle, useRef, useState } from 'react';
import { motion } from 'framer-motion';
import UploadDropzone from './UploadDropzone';
import Badge from './Badge';
import { ClockIcon, SparkIcon } from './Icons';

const sampleOptions = [
  { label: 'Payment Timeout', value: '/samples/payment-timeout.log' },
  { label: 'DB Connection Failure', value: '/samples/db-connection-failure.log' },
  { label: 'Mixed Failure', value: '/samples/mixed-microservices-failure.log' }
];

const UploadPanel = forwardRef(function UploadPanel({ onAnalyzeText, onUploadFile, loading }, ref) {
  const [rawLogs, setRawLogs] = useState('');
  const [dragging, setDragging] = useState(false);
  const [sampleLoading, setSampleLoading] = useState(false);
  const fileInputRef = useRef(null);
  const textareaRef = useRef(null);

  async function loadSample(path, autoAnalyze = false) {
    setSampleLoading(true);
    try {
      const response = await fetch(path);
      const text = await response.text();
      setRawLogs(text);
      if (autoAnalyze) {
        await onAnalyzeText(text);
      }
    } finally {
      setSampleLoading(false);
    }
  }

  useImperativeHandle(ref, () => ({
    openFilePicker: () => fileInputRef.current?.click(),
    trySampleLogs: () => loadSample(sampleOptions[0].value, true),
    focusComposer: () => textareaRef.current?.focus()
  }));

  function handleDrop(event) {
    event.preventDefault();
    setDragging(false);
    const file = event.dataTransfer.files?.[0];
    if (file) {
      onUploadFile(file);
    }
  }

  return (
    <motion.section
      className="panel upload-panel"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.38, delay: 0.08, ease: 'easeOut' }}
    >
      <div className="upload-panel__header">
        <div className="section-heading">
          <p className="eyebrow">Input workspace</p>
          <h2>Upload or paste production logs</h2>
          <p className="section-heading__subtitle">Run the same backend analysis pipeline against dragged files, pasted text, or bundled sample incidents.</p>
        </div>
        <div className="upload-panel__meta">
          <Badge tone="neutral" icon={ClockIcon}>Fast local triage</Badge>
          <Badge tone="primary" icon={SparkIcon}>Rule-based AI summary</Badge>
        </div>
      </div>

      <div className="upload-panel__grid">
        <div className="upload-panel__primary">
          <input
            ref={fileInputRef}
            type="file"
            hidden
            accept=".log,.txt"
            onChange={(event) => {
              const file = event.target.files?.[0];
              if (file) {
                onUploadFile(file);
              }
            }}
          />

          <UploadDropzone
            dragging={dragging}
            loading={loading}
            onDragEnter={(event) => {
              event.preventDefault();
              setDragging(true);
            }}
            onDragOver={(event) => event.preventDefault()}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            onOpenFilePicker={() => fileInputRef.current?.click()}
          />
        </div>

        <div className="upload-panel__secondary">
          <div className="sample-strip">
            <div>
              <p className="eyebrow">Sample incidents</p>
              <h3>Load a known failure pattern</h3>
            </div>
            <div className="sample-pills">
              {sampleOptions.map((sample) => (
                <button
                  key={sample.value}
                  className="ghost-button"
                  onClick={() => loadSample(sample.value)}
                  disabled={sampleLoading || loading}
                >
                  {sample.label}
                </button>
              ))}
            </div>
          </div>

          <div className="composer-card">
            <label className="composer-card__label" htmlFor="raw-log-textarea">Raw log composer</label>
            <textarea
              id="raw-log-textarea"
              ref={textareaRef}
              value={rawLogs}
              onChange={(event) => setRawLogs(event.target.value)}
              placeholder="Paste raw logs here..."
              rows={12}
            />
            <div className="composer-card__actions">
              <span>{rawLogs.trim() ? `${rawLogs.trim().split(/\r?\n/).length} lines loaded` : 'No logs loaded yet'}</span>
              <button className="primary-button" onClick={() => onAnalyzeText(rawLogs)} disabled={loading || !rawLogs.trim()}>
                {loading ? <span className="button-spinner" aria-hidden="true" /> : null}
                <span>{loading ? 'Analyzing...' : 'Analyze pasted logs'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </motion.section>
  );
});

export default UploadPanel;
