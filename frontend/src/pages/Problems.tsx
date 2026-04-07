import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter } from 'lucide-react';
import { Card } from '../components/Card';
import { StatusBadge } from '../components/StatusBadge';
import { MOCK_PROBLEMS } from '../utils/mockData';
export function Problems() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const filteredProblems = MOCK_PROBLEMS.filter((p) =>
  p.title.toLowerCase().includes(searchTerm.toLowerCase())
  );
  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-8 gap-4">
        <div>
          <h1 className="text-2xl font-bold text-dark">Problems</h1>
          <p className="text-sm text-muted-dark mt-1">
            Practice and improve your coding skills.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-slate-400" />
            </div>
            <input
              type="text"
              placeholder="Search problems..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md leading-5 bg-white placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-primary-dark focus:border-primary-dark sm:text-sm" />
            
          </div>
          <button className="inline-flex items-center px-3 py-2 border border-slate-300 shadow-sm text-sm font-medium rounded-md text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-dark">
            <Filter className="h-4 w-4 mr-2 text-slate-400" />
            Filter
          </button>
        </div>
      </div>

      <Card noPadding className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider w-16">
                  
                  ID
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Title
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Difficulty
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-muted-dark uppercase tracking-wider">
                  
                  Acceptance
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-200">
              {filteredProblems.map((problem) =>
              <tr
                key={problem.id}
                onClick={() => navigate(`/problems/${problem.id}`)}
                className="hover:bg-slate-50 cursor-pointer transition-colors">
                
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark">
                    {problem.id}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-dark hover:text-primary-dark transition-colors">
                      {problem.title}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <StatusBadge difficulty={problem.difficulty} />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-dark">
                    {problem.acceptance}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          {filteredProblems.length === 0 &&
          <div className="text-center py-12">
              <p className="text-muted-dark text-sm">
                No problems found matching your search.
              </p>
            </div>
          }
        </div>
      </Card>
    </div>);

}