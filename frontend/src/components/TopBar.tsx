import React from 'react';
import { Bell, Search, LogOut, Menu } from 'lucide-react';
import { Button } from './Button';
interface TopBarProps {
  onLogout: () => void;
}
export function TopBar({ onLogout }: TopBarProps) {
  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 sm:px-6 lg:px-8">
      <div className="flex items-center flex-1">
        <button className="md:hidden p-2 -ml-2 text-muted-dark hover:text-slate-700">
          <Menu className="w-6 h-6" />
        </button>

        <div className="hidden sm:flex items-center max-w-md w-full ml-4 md:ml-0">
          <div className="relative w-full">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-slate-400" />
            </div>
            <input
              type="text"
              className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md leading-5 bg-slate-50 placeholder-slate-400 focus:outline-none focus:bg-white focus:ring-1 focus:ring-primary-dark focus:border-primary-dark sm:text-sm transition-colors"
              placeholder="Search problems, users, or articles..." />
            
          </div>
        </div>
      </div>

      <div className="flex items-center space-x-4">
        <button className="p-2 text-slate-400 hover:text-muted-dark relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-1.5 right-1.5 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white" />
        </button>

        <div className="h-8 w-px bg-slate-200 mx-2" />

        <div className="flex items-center space-x-3">
          <div className="hidden sm:block text-right">
            <p className="text-sm font-medium text-slate-700">Alex Developer</p>
            <p className="text-xs text-muted-dark">Rank: 1,432</p>
          </div>
          <img
            className="h-9 w-9 rounded-full object-cover border border-slate-200"
            src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80"
            alt="Avatar" />
          
          <Button
            variant="ghost"
            size="sm"
            onClick={onLogout}
            className="text-muted-dark hover:text-red-600 px-2">
            
            <LogOut className="w-4 h-4" />
          </Button>
        </div>
      </div>
    </header>);

}