import React from 'react';
import { motion } from 'framer-motion';
import { UploadIcon } from './Icons';

export default function UploadDropzone({
  dragging,
  loading,
  onDragEnter,
  onDragOver,
  onDragLeave,
  onDrop,
  onOpenFilePicker
}) {
  return (
    <motion.div
      className={`dropzone ${dragging ? 'dragging' : ''}`}
      whileHover={{ y: -2, scale: 1.01 }}
      transition={{ duration: 0.22, ease: 'easeOut' }}
      onDragEnter={onDragEnter}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}
      role="button"
      tabIndex={0}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onOpenFilePicker();
        }
      }}
      aria-label="Upload a log file"
    >
      <div className="dropzone__icon-wrap">
        <UploadIcon className="dropzone__icon" />
      </div>
      <div className="dropzone__copy">
        <h3>Drop your incident file here</h3>
        <p>Supports `.log` and `.txt` uploads with the same parser and analysis flow as pasted logs.</p>
      </div>
      <div className="dropzone__actions">
        <button className="primary-button" onClick={onOpenFilePicker} disabled={loading}>
          {loading ? <span className="button-spinner" aria-hidden="true" /> : <UploadIcon className="button-icon" />}
          <span>{loading ? 'Uploading...' : 'Choose file'}</span>
        </button>
        <span className="dropzone__hint">Drag and drop or click to browse</span>
      </div>
    </motion.div>
  );
}
