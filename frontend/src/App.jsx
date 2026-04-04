import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import WelcomePage from './pages/WelcomePage';
import AnalyzePage from './pages/AnalyzePage';
import ResultsPage from './pages/ResultsPage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<WelcomePage />} />
      <Route path="/analyze" element={<AnalyzePage />} />
      <Route path="/analysis/:analysisId" element={<ResultsPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
