import React from 'react';
import { NavLink } from 'react-router-dom';
import { List, Activity, User, Settings } from 'lucide-react';
export function Sidebar() {
  const navItems = [
  {
    name: 'Problems',
    path: '/problems',
    icon: List
  },
  {
    name: 'Submissions',
    path: '/submissions',
    icon: Activity
  },
  {
    name: 'Profile',
    path: '/profile',
    icon: User
  },
  {
    name: 'Settings',
    path: '/settings',
    icon: Settings
  }];

  return (
    <aside className="w-64 bg-white border-r border-slate-200 hidden md:flex flex-col h-full">
      <div className="h-16 flex items-center px-6 border-b border-slate-200">
        <span className="font-accent text-xl font-semibold text-primary-dark tracking-tight">
          CodeArena
        </span>
      </div>

      <nav className="flex-1 py-6 px-3 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.name}
              to={item.path}
              className={({ isActive }) =>
              `flex items-center px-3 py-2.5 text-sm font-medium rounded-md transition-colors ${isActive ? 'bg-primary-dark/5 text-primary-dark' : 'text-muted-dark hover:bg-slate-50 hover:text-slate-900'}`
              }>
              
              <Icon className="w-5 h-5 mr-3 flex-shrink-0" />
              {item.name}
            </NavLink>);

        })}
      </nav>

      <div className="p-4 border-t border-slate-200">
        <div className="bg-slate-50 rounded-lg p-4">
          <p className="text-xs font-medium text-muted-dark uppercase tracking-wider mb-2">
            Pro Plan
          </p>
          <p className="text-sm text-slate-700 mb-3">
            Get access to premium editorial solutions.
          </p>
          <button className="w-full text-sm bg-primary-dark text-white py-2 rounded-md hover:bg-dark transition-colors">
            Upgrade
          </button>
        </div>
      </div>
    </aside>);

}