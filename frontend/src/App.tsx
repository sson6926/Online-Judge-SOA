import React, { useState } from 'react';
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate } from
'react-router-dom';
import { Landing } from './pages/Landing';
import { Login } from './pages/Login';
import { SignUp } from './pages/SignUp';
import { Problems } from './pages/Problems';
import { ProblemDetail } from './pages/ProblemDetail';
import { Submissions } from './pages/Submissions';
import { AppLayout } from './components/AppLayout';
export function App() {
  // Simple mock auth state
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const handleLogin = () => setIsAuthenticated(true);
  const handleLogout = () => setIsAuthenticated(false);
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route
          path="/"
          element={
          isAuthenticated ? <Navigate to="/problems" replace /> : <Landing />
          } />
        
        <Route
          path="/login"
          element={
          isAuthenticated ?
          <Navigate to="/problems" replace /> :

          <Login onLogin={handleLogin} />

          } />
        
        <Route
          path="/signup"
          element={
          isAuthenticated ?
          <Navigate to="/problems" replace /> :

          <SignUp onLogin={handleLogin} />

          } />
        

        {/* Authenticated Routes wrapped in AppLayout */}
        <Route
          element={
          <AppLayout
            isAuthenticated={isAuthenticated}
            onLogout={handleLogout} />

          }>
          
          <Route path="/problems" element={<Problems />} />
          <Route path="/problems/:id" element={<ProblemDetail />} />
          <Route path="/submissions" element={<Submissions />} />
          <Route
            path="/profile"
            element={<div className="p-8">Profile Page (Coming Soon)</div>} />
          
          <Route
            path="/settings"
            element={<div className="p-8">Settings Page (Coming Soon)</div>} />
          
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>);

}