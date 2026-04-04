import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import SummaryCards from './SummaryCards';

describe('SummaryCards', () => {
  it('renders key counters', () => {
    render(<SummaryCards summary={{ totalLogs: 100, totalInfo: 70, totalWarn: 20, totalError: 10 }} />);

    expect(screen.getByText('Total Logs')).toBeInTheDocument();
    expect(screen.getByText('100')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
  });
});
