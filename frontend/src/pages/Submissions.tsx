import React, { useEffect, useState } from 'react';
import { Card } from '../components/Card';
import { StatusBadge } from '../components/StatusBadge';
import { submissionApi, Submission } from '../utils/api';

export function Submissions() {
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    submissionApi.getAll()
      .then(setSubmissions)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const verdictColor = (verdict: string | null) => {
    if (verdict === 'AC') return 'text-emerald-600 font-semibold';
    if (verdict === 'WA') return 'text-red-600';
    if (verdict === 'TLE') return 'text-amber-600';
    return 'text-slate-500';
  };

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-dark">All Submissions</h1>
        <p className="text-sm text-muted-dark mt-1">Global activity across all problems.</p>
      </div>

      <Card noPadding className="overflow-hidden">
        {loading && <div className="text-center py-12 text-slate-500 text-sm">Loading submissions...</div>}
        {error && <div className="text-center py-12 text-red-500 text-sm">Error: {error}</div>}
        {!loading && !error && (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">Time</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">Problem</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">Verdict</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">Runtime</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">Language</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {submissions.map((sub) => (
                  <tr key={sub.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark">
                      {new Date(sub.submitted_at).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-muted-dark">
                      {sub.problem_id.slice(0, 8)}...
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-700">
                      {sub.status}
                    </td>
                    <td className={`px-6 py-4 whitespace-nowrap text-sm ${verdictColor(sub.verdict)}`}>
                      {sub.verdict ?? '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark font-mono">
                      {sub.time_used != null ? `${sub.time_used} ms` : 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-800">
                        {sub.language}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {submissions.length === 0 && (
              <div className="text-center py-12">
                <p className="text-muted-dark text-sm">No submissions yet.</p>
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
}
