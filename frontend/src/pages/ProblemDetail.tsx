import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, XCircle, Clock, AlertCircle, History } from 'lucide-react';
import { CodeEditor } from '../components/CodeEditor';
import { StatusBadge } from '../components/StatusBadge';
import { problemApi, submissionApi, Problem, Submission, getToken } from '../utils/api';

export function ProblemDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [problem, setProblem] = useState<Problem | null>(null);
  const [loading, setLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<Submission | null>(null);
  const [history, setHistory] = useState<Submission[]>([]);
  const [activeTab, setActiveTab] = useState<'result' | 'history'>('result');
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!id) return;
    problemApi.getById(id)
      .then(setProblem)
      .catch(() => setProblem(null))
      .finally(() => setLoading(false));
    // Load submission history for this problem
    submissionApi.getByProblemId(id).then(setHistory).catch(() => {});
  }, [id]);

  useEffect(() => {
    return () => { if (pollingRef.current) clearInterval(pollingRef.current); };
  }, []);

  const handleSubmit = async (code: string, lang: string) => {
    setIsSubmitting(true);
    setResult(null);
    setActiveTab('result');

    const token = getToken();
    let userId = 'anonymous';
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        userId = payload.sub || payload.userId || 'anonymous';
      } catch {}
    }

    try {
      const submission = await submissionApi.submit({
        problem_id: id!,
        user_id: userId,
        code,
        language: lang,
      });

      setResult(submission);
      setHistory(prev => [submission, ...prev]);

      pollingRef.current = setInterval(async () => {
        const updated = await submissionApi.getById(submission.id);
        setResult(updated);
        if (updated.status === 'FINISHED') {
          clearInterval(pollingRef.current!);
          setIsSubmitting(false);
          setHistory(prev => prev.map(s => s.id === updated.id ? updated : s));
        }
      }, 1000);
    } catch (e: any) {
      setIsSubmitting(false);
      alert(`Submit failed: ${e.message}`);
    }
  };

  const handleRun = (_code: string, _lang: string) => {
    alert('Run sample feature coming soon!');
  };

  if (loading) return <div className="p-8 text-center text-slate-500">Loading...</div>;
  if (!problem) return (
    <div className="p-8 text-center">
      <h2 className="text-2xl font-bold text-slate-900">Problem not found</h2>
      <button onClick={() => navigate('/problems')} className="mt-4 text-primary-dark hover:underline">Back to problems</button>
    </div>
  );

  const getVerdictColor = (verdict: string | null) => {
    if (verdict === 'AC') return 'text-emerald-600';
    if (verdict === 'WA') return 'text-red-600';
    return 'text-amber-600';
  };

  const getVerdictIcon = (verdict: string | null) => {
    if (verdict === 'AC') return <CheckCircle2 className="w-5 h-5 text-emerald-500" />;
    if (verdict === 'WA') return <XCircle className="w-5 h-5 text-red-500" />;
    if (verdict === 'TLE') return <Clock className="w-5 h-5 text-amber-500" />;
    return <AlertCircle className="w-5 h-5 text-slate-500" />;
  };

  return (
    <div className="flex flex-col lg:flex-row h-full w-full bg-slate-50 overflow-hidden">
      {/* Left Panel */}
      <div className="w-full lg:w-5/12 h-1/2 lg:h-full flex flex-col border-b lg:border-b-0 lg:border-r border-slate-200 bg-white">
        <div className="p-4 border-b border-slate-200 flex items-center space-x-3 bg-white sticky top-0 z-10">
          <button onClick={() => navigate('/problems')} className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-lg font-bold text-slate-900 truncate">{problem.title}</h1>
        </div>
        <div className="flex-1 overflow-y-auto p-6">
          <div className="flex items-center space-x-4 mb-6">
            <StatusBadge difficulty={problem.difficulty as any} />
            <span className="text-sm text-slate-500">Time: {problem.time_limit}ms | Memory: {problem.memory_limit}KB</span>
          </div>
          <div className="whitespace-pre-wrap text-slate-700 leading-relaxed">
            {problem.description}
          </div>
        </div>
      </div>

      {/* Right Panel */}
      <div className="w-full lg:w-7/12 h-1/2 lg:h-full flex flex-col bg-slate-100 p-4 space-y-4">
        <div className="flex-1 min-h-[300px]">
          <CodeEditor onRun={handleRun} onSubmit={handleSubmit} isRunning={false} isSubmitting={isSubmitting} />
        </div>

        {/* Results + History tabs */}
        <div className="h-48 bg-white rounded-lg border border-slate-200 shadow-sm flex flex-col overflow-hidden">
          <div className="flex border-b border-slate-200 bg-slate-50">
            <button
              onClick={() => setActiveTab('result')}
              className={`px-4 py-2 text-sm font-medium ${activeTab === 'result' ? 'text-primary-dark border-b-2 border-primary-dark' : 'text-slate-500 hover:text-slate-700'}`}
            >
              Test Results
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`px-4 py-2 text-sm font-medium flex items-center gap-1 ${activeTab === 'history' ? 'text-primary-dark border-b-2 border-primary-dark' : 'text-slate-500 hover:text-slate-700'}`}
            >
              <History className="w-3.5 h-3.5" />
              History ({history.length})
            </button>
          </div>

          <div className="flex-1 p-4 overflow-y-auto">
            {activeTab === 'result' && (
              <>
                {!result && !isSubmitting && (
                  <div className="h-full flex items-center justify-center text-slate-400 text-sm">Submit to see results here.</div>
                )}
                {isSubmitting && result?.status !== 'FINISHED' && (
                  <div className="h-full flex flex-col items-center justify-center text-slate-500 space-y-3">
                    <div className="w-6 h-6 border-2 border-primary-dark border-t-transparent rounded-full animate-spin" />
                    <span className="text-sm font-medium">Judging... ({result?.status ?? 'PENDING'})</span>
                  </div>
                )}
                {result && result.status === 'FINISHED' && (
                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      {getVerdictIcon(result.verdict)}
                      <h3 className={`text-lg font-bold ${getVerdictColor(result.verdict)}`}>{result.verdict ?? 'Unknown'}</h3>
                    </div>
                    {result.error_details && (
                      <p className="text-sm text-slate-700 bg-slate-50 p-3 rounded border border-slate-100 font-mono">{result.error_details}</p>
                    )}
                    <div className="flex space-x-6 text-sm">
                      <div><span className="text-slate-500">Runtime:</span><span className="ml-2 font-mono font-medium">{result.time_used ?? 'N/A'} ms</span></div>
                      <div><span className="text-slate-500">Memory:</span><span className="ml-2 font-mono font-medium">{result.memory_used != null ? `${(result.memory_used / 1024).toFixed(1)} MB` : 'N/A'}</span></div>
                    </div>
                  </div>
                )}
              </>
            )}

            {activeTab === 'history' && (
              <div className="space-y-1">
                {history.length === 0 && <p className="text-slate-400 text-sm text-center py-4">No submissions yet.</p>}
                {history.map(s => (
                  <div key={s.id} className="flex items-center justify-between text-sm py-1.5 border-b border-slate-100 last:border-0">
                    <span className={`font-semibold w-16 ${s.verdict === 'AC' ? 'text-emerald-600' : s.verdict ? 'text-red-600' : 'text-slate-500'}`}>
                      {s.status === 'FINISHED' ? (s.verdict ?? '—') : s.status}
                    </span>
                    <span className="text-slate-500 font-mono text-xs">{s.language}</span>
                    <span className="text-slate-400 font-mono text-xs">{s.time_used != null ? `${s.time_used}ms` : '—'}</span>
                    <span className="text-slate-400 text-xs">{new Date(s.submitted_at).toLocaleTimeString()}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
