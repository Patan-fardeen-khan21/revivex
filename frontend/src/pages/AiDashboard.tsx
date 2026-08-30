import React, { useEffect, useState, useRef } from 'react';
import api from '../api';

interface Stats {
  totalAtRisk: number;
  totalRecovered: number;
  totalInterventions: number;
  recoveredCount: number;
}

interface InterventionLog {
  id: number;
  user: { email: string };
  interventionType: string;
  discountCode: string;
  revenueAtRisk: number;
  status: string;
  createdAt: string;
  recoveredAt: string | null;
  riskScore: number;
  reasoning: string;
}

export const AiDashboard: React.FC = () => {
  const [stats, setStats] = useState<Stats | null>(null);
  const [logs, setLogs] = useState<InterventionLog[]>([]);
  const [consoleOutput, setConsoleOutput] = useState<string[]>([]);
  const consoleEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 4000); // Poll every 4s for real-time demo feel
    return () => clearInterval(interval);
  }, []);

  // Simulate Live Agent sweeps on the terminal
  useEffect(() => {
    if (logs.length > 0) {
      const thoughts: string[] = [];
      thoughts.push(`[${new Date().toLocaleTimeString()}] [SYSTEM] Agent online. Waiting for risk event...`);
      
      // Sort oldest to newest to print chronologically
      const sortedLogs = [...logs].reverse();
      
      sortedLogs.forEach((log) => {
        const timeStr = new Date(log.createdAt).toLocaleTimeString();
        if (log.interventionType === '15%_DISCOUNT_OFFER') {
          thoughts.push(`[${timeStr}] [RISK_ANALYZER] Checkout abandonment detected for user: ${log.user.email}`);
          thoughts.push(`[${timeStr}] [AGENT_COT] ${log.reasoning}`);
        } else if (log.interventionType === 'PAYMENT_FAILURE_RECOVERY') {
          thoughts.push(`[${timeStr}] [RISK_ANALYZER] PAYMENT CRITICAL: Transaction failed for user: ${log.user.email}`);
          thoughts.push(`[${timeStr}] [AGENT_COT] ${log.reasoning}`);
        } else if (log.interventionType === 'OVERDUE_RECEIVABLE_REMINDER') {
          thoughts.push(`[${timeStr}] [RISK_ANALYZER] RECEIVABLE WARNING: Order pending settlement > 5 minutes for user: ${log.user.email}`);
          thoughts.push(`[${timeStr}] [AGENT_COT] ${log.reasoning}`);
        }

        if (log.status === 'RECOVERED') {
          const recoveredTime = log.recoveredAt ? new Date(log.recoveredAt).toLocaleTimeString() : 'N/A';
          thoughts.push(`[${recoveredTime}] [RECOVERY_FLOW] Bounded workflow completed. Recovered: ₹${log.revenueAtRisk.toFixed(2)}.`);
        }
      });

      // Add a live scan line
      thoughts.push(`[${new Date().toLocaleTimeString()}] [LIVE_AGENT] Database scan complete. Status: SCANNING... 🟢`);
      setConsoleOutput(thoughts);
    }
  }, [logs]);

  // Scroll to bottom of terminal
  useEffect(() => {
    consoleEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [consoleOutput]);

  const fetchData = async () => {
    try {
      const statsRes = await api.get('/admin/recovery/stats');
      setStats(statsRes.data);
      const logsRes = await api.get('/admin/recovery/logs');
      setLogs(logsRes.data);
    } catch (error) {
      console.error("Error fetching AI stats", error);
    }
  };

  if (!stats) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading AI Analytics...</div>;

  // Segment stats by category
  const getStatsByCategory = (type: string) => {
    const categoryLogs = logs.filter(l => l.interventionType === type);
    const atRisk = categoryLogs.reduce((sum, l) => sum + l.revenueAtRisk, 0);
    const recovered = categoryLogs.filter(l => l.status === 'RECOVERED').reduce((sum, l) => sum + l.revenueAtRisk, 0);
    const count = categoryLogs.length;
    const recoveredCount = categoryLogs.filter(l => l.status === 'RECOVERED').length;
    return { atRisk, recovered, count, recoveredCount };
  };

  const abandonmentStats = getStatsByCategory('15%_DISCOUNT_OFFER');
  const failureStats = getStatsByCategory('PAYMENT_FAILURE_RECOVERY');
  const overdueStats = getStatsByCategory('OVERDUE_RECEIVABLE_REMINDER');

  const recoveryRate = stats.totalInterventions > 0 
    ? Math.round((stats.recoveredCount / stats.totalInterventions) * 100) 
    : 0;

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        🤖 AI Revenue Recovery Center
      </h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
        Real-time risk assessment, dynamic intervention engine, and audit analytics.
      </p>

      {/* Live AI Brain Console */}
      <div style={{ marginBottom: '3rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#1e1e1e', padding: '0.75rem 1rem', borderTopLeftRadius: '8px', borderTopRightRadius: '8px', borderBottom: '1px solid #333' }}>
          <span style={{ color: '#00ff00', fontFamily: 'monospace', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span className="live-dot" style={{ width: '10px', height: '10px', backgroundColor: '#00ff00', borderRadius: '50%', display: 'inline-block', animation: 'pulse 1.5s infinite' }}></span>
            AI AGENT LIVE MONITOR & BRAIN CONSOLE
          </span>
          <span style={{ color: '#888', fontSize: '0.8rem', fontFamily: 'monospace' }}>Oracle-Live-Agent-v1.0.0</span>
        </div>
        <div style={{ 
          backgroundColor: '#0c0c0c', 
          color: '#33ff33', 
          fontFamily: 'monospace', 
          padding: '1.5rem', 
          height: '250px', 
          overflowY: 'auto', 
          borderBottomLeftRadius: '8px', 
          borderBottomRightRadius: '8px',
          boxShadow: 'inset 0 0 10px #000',
          fontSize: '0.9rem',
          lineHeight: '1.5',
          textAlign: 'left'
        }}>
          {consoleOutput.map((line, i) => (
            <div key={i} style={{ 
              color: line.includes('[RISK_ANALYZER]') ? '#ff3333' : 
                     line.includes('[RECOVERY_FLOW]') ? '#33ff33' : 
                     line.includes('[AGENT_COT]') ? '#ffcc00' : '#888888'
            }}>
              {line}
            </div>
          ))}
          <div ref={consoleEndRef} />
        </div>
      </div>
      
      {/* Dynamic Stats Cards */}
      <h2 style={{ marginBottom: '1.5rem' }}>Core Performance Analytics</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginBottom: '3rem' }}>
        
        {/* Cart Abandonment Stats */}
        <div className="card" style={{ borderLeft: '4px solid #10b981' }}>
          <h3 style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '1rem' }}>Checkout Abandonment</h3>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <h2 style={{ fontSize: '1.75rem', color: '#10b981' }}>₹{abandonmentStats.recovered.toLocaleString()}</h2>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>of ₹{abandonmentStats.atRisk.toLocaleString()} at risk</span>
          </div>
          <p style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}>
            Interventions: <strong>{abandonmentStats.count}</strong> | Recovered: <strong>{abandonmentStats.recoveredCount}</strong>
          </p>
        </div>

        {/* Payment Failure Stats */}
        <div className="card" style={{ borderLeft: '4px solid #f87171' }}>
          <h3 style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '1rem' }}>Payment Failures</h3>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <h2 style={{ fontSize: '1.75rem', color: '#f87171' }}>₹{failureStats.recovered.toLocaleString()}</h2>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>of ₹{failureStats.atRisk.toLocaleString()} at risk</span>
          </div>
          <p style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}>
            Interventions: <strong>{failureStats.count}</strong> | Recovered: <strong>{failureStats.recoveredCount}</strong>
          </p>
        </div>

        {/* Overdue Receivables Stats */}
        <div className="card" style={{ borderLeft: '4px solid #fbbf24' }}>
          <h3 style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '1rem' }}>Overdue Receivables</h3>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <h2 style={{ fontSize: '1.75rem', color: '#fbbf24' }}>₹{overdueStats.recovered.toLocaleString()}</h2>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>of ₹{overdueStats.atRisk.toLocaleString()} at risk</span>
          </div>
          <p style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}>
            Interventions: <strong>{overdueStats.count}</strong> | Recovered: <strong>{overdueStats.recoveredCount}</strong>
          </p>
        </div>

        {/* Summary Rate Card */}
        <div className="card" style={{ borderLeft: '4px solid var(--primary-color)' }}>
          <h3 style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '1rem' }}>Overall Recovery Rate</h3>
          <h2 style={{ fontSize: '1.75rem', color: 'var(--primary-color)' }}>{recoveryRate}%</h2>
          <p style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}>
            Saved: <strong>{stats.recoveredCount}</strong> / Total: <strong>{stats.totalInterventions}</strong> Interventions
          </p>
        </div>

      </div>

      <h2>Audit Log: AI Interventions</h2>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Compliant escalation ledger, stopping rules, and decision logging.</p>
      
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'var(--card-bg)', borderRadius: '8px', overflow: 'hidden' }}>
          <thead>
            <tr style={{ backgroundColor: 'var(--border-color)', textAlign: 'left' }}>
              <th style={{ padding: '1rem' }}>User</th>
              <th style={{ padding: '1rem' }}>Intervention Type</th>
              <th style={{ padding: '1rem' }}>Discount Code</th>
              <th style={{ padding: '1rem' }}>Risk Score</th>
              <th style={{ padding: '1rem' }}>At Risk</th>
              <th style={{ padding: '1rem' }}>Status</th>
              <th style={{ padding: '1rem' }}>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {logs.map(log => (
              <tr key={log.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                <td style={{ padding: '1rem' }}>{log.user.email}</td>
                <td style={{ padding: '1rem' }}>
                  <span style={{ 
                    padding: '0.25rem 0.5rem', 
                    borderRadius: '4px', 
                    fontSize: '0.85rem',
                    backgroundColor: log.interventionType.includes('FAIL') ? '#fef2f2' : log.interventionType.includes('RECEIVABLE') ? '#fffbeb' : '#ecfdf5',
                    color: log.interventionType.includes('FAIL') ? '#991b1b' : log.interventionType.includes('RECEIVABLE') ? '#92400e' : '#065f46'
                  }}>
                    {log.interventionType}
                  </span>
                </td>
                <td style={{ padding: '1rem' }}><code>{log.discountCode}</code></td>
                <td style={{ padding: '1rem', fontWeight: 'bold' }}>{log.riskScore ? `${log.riskScore}%` : 'N/A'}</td>
                <td style={{ padding: '1rem' }}>₹{log.revenueAtRisk.toLocaleString()}</td>
                <td style={{ padding: '1rem' }}>
                  <span style={{ 
                    padding: '0.25rem 0.75rem', 
                    borderRadius: '999px', 
                    fontSize: '0.85rem',
                    backgroundColor: log.status === 'RECOVERED' ? '#d1fae5' : '#fee2e2',
                    color: log.status === 'RECOVERED' ? '#065f46' : '#991b1b',
                    fontWeight: 'bold'
                  }}>
                    {log.status}
                  </span>
                </td>
                <td style={{ padding: '1rem', color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                  {new Date(log.createdAt).toLocaleString()}
                </td>
              </tr>
            ))}
            {logs.length === 0 && (
              <tr>
                <td colSpan={7} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                  No AI interventions executed yet. Carts or orders are not active.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* CSS injection for pulsing effect */}
      <style>{`
        @keyframes pulse {
          0% { transform: scale(0.95); opacity: 0.5; }
          50% { transform: scale(1.1); opacity: 1; }
          100% { transform: scale(0.95); opacity: 0.5; }
        }
      `}</style>
    </div>
  );
};
