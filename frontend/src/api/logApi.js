const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function handleResponse(response) {
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }
  return response.json();
}

export async function analyzeText(rawLogs) {
  const response = await fetch(`${API_BASE}/logs/analyze-text`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ rawLogs })
  });
  return handleResponse(response);
}

export async function uploadFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  const response = await fetch(`${API_BASE}/logs/upload`, {
    method: 'POST',
    body: formData
  });
  return handleResponse(response);
}

export async function fetchSummary(analysisId) {
  return handleResponse(await fetch(`${API_BASE}/logs/${analysisId}/summary`));
}

export async function fetchIssues(analysisId) {
  return handleResponse(await fetch(`${API_BASE}/logs/${analysisId}/issues`));
}

export async function generateIssueRecommendation(analysisId, issueId) {
  const response = await fetch(`${API_BASE}/logs/${analysisId}/issues/${issueId}/ai-recommendation`, {
    method: 'POST'
  });
  return handleResponse(response);
}

export async function generateOverallRecommendations(analysisId) {
  const response = await fetch(`${API_BASE}/logs/${analysisId}/ai-recommendations`, {
    method: 'POST'
  });
  return handleResponse(response);
}

export async function fetchEntries(analysisId, params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value);
    }
  });
  return handleResponse(await fetch(`${API_BASE}/logs/${analysisId}/entries?${query.toString()}`));
}

export async function fetchExport(analysisId) {
  return handleResponse(await fetch(`${API_BASE}/logs/${analysisId}/export`));
}
