import React from 'react';
import { Outlet, Navigate, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';
import { motion, AnimatePresence } from 'framer-motion';
interface AppLayoutProps {
  isAuthenticated: boolean;
  onLogout: () => void;
}
export function AppLayout({ isAuthenticated, onLogout }: AppLayoutProps) {
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <TopBar onLogout={onLogout} />
        <main className="flex-1 overflow-auto relative">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{
                opacity: 0,
                y: 10
              }}
              animate={{
                opacity: 1,
                y: 0
              }}
              exit={{
                opacity: 0,
                y: -10
              }}
              transition={{
                duration: 0.2
              }}
              className="h-full">
              
              <Outlet />
            </motion.div>
          </AnimatePresence>
        </main>
      </div>
    </div>);

}