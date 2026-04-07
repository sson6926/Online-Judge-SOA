import React from 'react';
import { Status, Difficulty } from '../utils/mockData';
interface StatusBadgeProps {
  status?: Status;
  difficulty?: Difficulty;
  className?: string;
}
export function StatusBadge({
  status,
  difficulty,
  className = ''
}: StatusBadgeProps) {
  if (difficulty) {
    const diffStyles = {
      Easy: 'text-emerald-700 bg-emerald-50 ring-emerald-600/20',
      Medium: 'text-amber-700 bg-amber-50 ring-amber-600/20',
      Hard: 'text-red-700 bg-red-50 ring-red-600/20'
    };
    return (
      <span
        className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${diffStyles[difficulty]} ${className}`}>
        
        {difficulty}
      </span>);

  }
  if (status) {
    const statusStyles: Record<Status, string> = {
      Accepted: 'text-emerald-700 bg-emerald-50 ring-emerald-600/20',
      'Wrong Answer': 'text-red-700 bg-red-50 ring-red-600/20',
      'Time Limit Exceeded': 'text-amber-700 bg-amber-50 ring-amber-600/20',
      'Runtime Error': 'text-red-700 bg-red-50 ring-red-600/20',
      'Compile Error': 'text-sky-700 bg-sky-50 ring-sky-600/20',
      Pending: 'text-slate-700 bg-slate-50 ring-slate-600/20'
    };
    return (
      <span
        className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${statusStyles[status]} ${className}`}>
        
        {status}
      </span>);

  }
  return null;
}