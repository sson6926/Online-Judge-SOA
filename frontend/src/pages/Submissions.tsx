import React, { memo } from 'react';
import { Card } from '../components/Card';
import { StatusBadge } from '../components/StatusBadge';
import { MOCK_SUBMISSIONS } from '../utils/mockData';
import { ChevronLeft, ChevronRight } from 'lucide-react';
export function Submissions() {
  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-dark">All Submissions</h1>
        <p className="text-sm text-muted-dark mt-1">
          Global activity across all problems.
        </p>
      </div>

      <Card noPadding className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Time Submitted
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  User
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Problem
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Status
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Runtime
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Memory
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Language
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-200">
              {MOCK_SUBMISSIONS.map((sub) =>
              <tr
                key={sub.id}
                className="hover:bg-slate-50 transition-colors">
                
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark">
                    {sub.timestamp}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-dark hover:text-primary-dark cursor-pointer">
                      {sub.username}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-dark hover:text-primary-dark cursor-pointer">
                      {sub.problemTitle}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <StatusBadge status={sub.status} />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark font-mono">
                    {sub.runtime}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark font-mono">
                    {sub.memory}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark">
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-800">
                      {sub.language}
                    </span>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="bg-white px-4 py-3 border-t border-slate-200 flex items-center justify-between sm:px-6">
          <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-slate-700">
                Showing <span className="font-medium">1</span> to{' '}
                <span className="font-medium">6</span> of{' '}
                <span className="font-medium">97</span> results
              </p>
            </div>
            <div>
              <nav
                className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
                aria-label="Pagination">
                
                <button className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-slate-300 bg-white text-sm font-medium text-muted-dark hover:bg-slate-50">
                  <span className="sr-only">Previous</span>
                  <ChevronLeft className="h-5 w-5" aria-hidden="true" />
                </button>
                <button className="relative inline-flex items-center px-4 py-2 border border-slate-300 bg-primary-dark/5 text-sm font-medium text-primary-dark">
                  1
                </button>
                <button className="relative inline-flex items-center px-4 py-2 border border-slate-300 bg-white text-sm font-medium text-slate-700 hover:bg-slate-50">
                  2
                </button>
                <button className="relative inline-flex items-center px-4 py-2 border border-slate-300 bg-white text-sm font-medium text-slate-700 hover:bg-slate-50">
                  3
                </button>
                <span className="relative inline-flex items-center px-4 py-2 border border-slate-300 bg-white text-sm font-medium text-slate-700">
                  ...
                </span>
                <button className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-slate-300 bg-white text-sm font-medium text-muted-dark hover:bg-slate-50">
                  <span className="sr-only">Next</span>
                  <ChevronRight className="h-5 w-5" aria-hidden="true" />
                </button>
              </nav>
            </div>
          </div>
        </div>
      </Card>
    </div>);

}