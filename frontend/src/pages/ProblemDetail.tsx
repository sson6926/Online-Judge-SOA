import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  CheckCircle2,
  XCircle,
  Clock,
  AlertCircle } from
'lucide-react';
import { CodeEditor } from '../components/CodeEditor';
import { StatusBadge } from '../components/StatusBadge';
import { MOCK_PROBLEMS, Status } from '../utils/mockData';
export function ProblemDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const problem = MOCK_PROBLEMS.find((p) => p.id === id);
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<{
    status: Status;
    message: string;
    time?: string;
    mem?: string;
  } | null>(null);
  if (!problem) {
    return (
      <div className="p-8 text-center">
        <h2 className="text-2xl font-bold text-slate-900">Problem not found</h2>
        <button
          onClick={() => navigate('/problems')}
          className="mt-4 text-primary-dark hover:underline">
          
          Back to problems
        </button>
      </div>);

  }
  const handleRun = (code: string, lang: string) => {
    setIsRunning(true);
    setResult(null);
    setTimeout(() => {
      setIsRunning(false);
      setResult({
        status: 'Accepted',
        message: 'Sample test cases passed!',
        time: '12 ms',
        mem: '14.2 MB'
      });
    }, 1500);
  };
  const handleSubmit = (code: string, lang: string) => {
    setIsSubmitting(true);
    setResult(null);
    setTimeout(() => {
      setIsSubmitting(false);
      // Randomize result for demo purposes
      const statuses: Status[] = [
      'Accepted',
      'Wrong Answer',
      'Time Limit Exceeded'];

      const randomStatus = statuses[Math.floor(Math.random() * statuses.length)];
      if (randomStatus === 'Accepted') {
        setResult({
          status: randomStatus,
          message: 'All test cases passed!',
          time: '45 ms',
          mem: '16.4 MB'
        });
      } else if (randomStatus === 'Wrong Answer') {
        setResult({
          status: randomStatus,
          message: 'Failed at test case 14/52.',
          time: 'N/A',
          mem: 'N/A'
        });
      } else {
        setResult({
          status: randomStatus,
          message: 'Time limit exceeded on test case 42.',
          time: '> 2000 ms',
          mem: '15.1 MB'
        });
      }
    }, 2000);
  };
  const getResultIcon = (status: Status) => {
    switch (status) {
      case 'Accepted':
        return <CheckCircle2 className="w-5 h-5 text-emerald-500" />;
      case 'Wrong Answer':
        return <XCircle className="w-5 h-5 text-red-500" />;
      case 'Time Limit Exceeded':
        return <Clock className="w-5 h-5 text-amber-500" />;
      default:
        return <AlertCircle className="w-5 h-5 text-slate-500" />;
    }
  };
  return (
    <div className="flex flex-col lg:flex-row h-full w-full bg-slate-50 overflow-hidden">
      {/* Left Panel: Problem Description */}
      <div className="w-full lg:w-5/12 h-1/2 lg:h-full flex flex-col border-b lg:border-b-0 lg:border-r border-slate-200 bg-white">
        <div className="p-4 border-b border-slate-200 flex items-center space-x-3 bg-white sticky top-0 z-10">
          <button
            onClick={() => navigate('/problems')}
            className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors">
            
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-lg font-bold text-slate-900 truncate">
            {problem.id}. {problem.title}
          </h1>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          <div className="flex items-center space-x-4 mb-6">
            <StatusBadge difficulty={problem.difficulty} />
            <span className="text-sm text-slate-500">
              Acceptance: {problem.acceptance}
            </span>
          </div>

          <div className="prose prose-slate prose-sm max-w-none">
            <div className="whitespace-pre-wrap text-slate-700 leading-relaxed mb-8">
              {problem.description}
            </div>

            {problem.examples.length > 0 &&
            <div className="space-y-6 mb-8">
                {problem.examples.map((ex, idx) =>
              <div key={idx}>
                    <p className="font-semibold text-slate-900 mb-2">
                      Example {idx + 1}:
                    </p>
                    <div className="bg-slate-50 rounded-md p-4 border border-slate-200 font-mono text-sm text-slate-800">
                      <div>
                        <span className="font-semibold text-slate-500 select-none">
                          Input:{' '}
                        </span>
                        {ex.input}
                      </div>
                      <div className="mt-1">
                        <span className="font-semibold text-slate-500 select-none">
                          Output:{' '}
                        </span>
                        {ex.output}
                      </div>
                      {ex.explanation &&
                  <div className="mt-1">
                          <span className="font-semibold text-slate-500 select-none">
                            Explanation:{' '}
                          </span>
                          {ex.explanation}
                        </div>
                  }
                    </div>
                  </div>
              )}
              </div>
            }

            {problem.constraints.length > 0 &&
            <div>
                <p className="font-semibold text-slate-900 mb-2">
                  Constraints:
                </p>
                <ul className="list-disc pl-5 space-y-1 text-slate-700 bg-slate-50 p-4 rounded-md border border-slate-200">
                  {problem.constraints.map((c, idx) =>
                <li key={idx} className="font-mono text-sm">
                      {c}
                    </li>
                )}
                </ul>
              </div>
            }
          </div>
        </div>
      </div>

      {/* Right Panel: Code Editor & Results */}
      <div className="w-full lg:w-7/12 h-1/2 lg:h-full flex flex-col bg-slate-100 p-4 space-y-4">
        <div className="flex-1 min-h-[300px]">
          <CodeEditor
            onRun={handleRun}
            onSubmit={handleSubmit}
            isRunning={isRunning}
            isSubmitting={isSubmitting} />
          
        </div>

        {/* Results Area */}
        <div className="h-48 bg-white rounded-lg border border-slate-200 shadow-sm flex flex-col overflow-hidden">
          <div className="px-4 py-2 border-b border-slate-200 bg-slate-50 text-sm font-medium text-slate-700 flex items-center">
            Test Results
          </div>
          <div className="flex-1 p-4 overflow-y-auto">
            {!result && !isRunning && !isSubmitting &&
            <div className="h-full flex items-center justify-center text-slate-400 text-sm">
                Run your code or submit to see results here.
              </div>
            }

            {(isRunning || isSubmitting) &&
            <div className="h-full flex flex-col items-center justify-center text-slate-500 space-y-3">
                <div className="w-6 h-6 border-2 border-primary-dark border-t-transparent rounded-full animate-spin" />
                <span className="text-sm font-medium">
                  {isRunning ? 'Running code...' : 'Judging submission...'}
                </span>
              </div>
            }

            {result && !isRunning && !isSubmitting &&
            <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  {getResultIcon(result.status)}
                  <h3
                  className={`text-lg font-bold ${result.status === 'Accepted' ? 'text-emerald-600' : result.status === 'Wrong Answer' ? 'text-red-600' : 'text-amber-600'}`}>
                  
                    {result.status}
                  </h3>
                </div>
                <p className="text-sm text-slate-700 bg-slate-50 p-3 rounded border border-slate-100">
                  {result.message}
                </p>
                {result.time && result.mem &&
              <div className="flex space-x-6 text-sm">
                    <div>
                      <span className="text-slate-500">Runtime:</span>
                      <span className="ml-2 font-mono font-medium text-slate-900">
                        {result.time}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-500">Memory:</span>
                      <span className="ml-2 font-mono font-medium text-slate-900">
                        {result.mem}
                      </span>
                    </div>
                  </div>
              }
              </div>
            }
          </div>
        </div>
      </div>
    </div>);

}