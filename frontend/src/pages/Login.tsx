import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock } from 'lucide-react';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { motion } from 'framer-motion';
import { userApi, setToken } from '../utils/api';

interface LoginProps {
  onLogin: () => void;
}

export function Login({ onLogin }: LoginProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const form = e.currentTarget;
    const username = (form.elements.namedItem('username') as HTMLInputElement).value;
    const password = (form.elements.namedItem('password') as HTMLInputElement).value;

    try {
      const res = await userApi.login(username, password);
      setToken(res.token);
      onLogin();
      navigate('/problems');
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <div className="min-h-screen bg-white flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <motion.div
        initial={{
          opacity: 0,
          y: 20
        }}
        animate={{
          opacity: 1,
          y: 0
        }}
        className="sm:mx-auto sm:w-full sm:max-w-md">
        
        <div className="flex justify-center">
          <Link to="/" className="flex items-center">
            <span className="font-accent text-3xl font-semibold text-primary-dark">
              CodeArena
            </span>
          </Link>
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold text-dark">
          Sign in to your account
        </h2>
        <p className="mt-2 text-center text-sm text-muted-dark">
          Or{' '}
          <Link
            to="/signup"
            className="font-medium text-primary-dark hover:text-dark transition-colors underline underline-offset-2">
            
            create a new account
          </Link>
        </p>
      </motion.div>

      <motion.div
        initial={{
          opacity: 0,
          y: 20
        }}
        animate={{
          opacity: 1,
          y: 0
        }}
        transition={{
          delay: 0.1
        }}
        className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        
        <Card>
          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-dark">
                Username
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="username"
                  name="username"
                  type="text"
                  autoComplete="username"
                  required
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-primary-dark focus:border-primary-dark sm:text-sm"
                  placeholder="your_username" />
              </div>
            </div>

            <div>
              <label
                htmlFor="password"
                className="block text-sm font-medium text-dark">
                
                Password
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-primary-dark focus:border-primary-dark sm:text-sm"
                  placeholder="••••••••" />
                
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <input
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                  className="h-4 w-4 text-primary-dark focus:ring-primary-dark border-slate-300 rounded" />
                
                <label
                  htmlFor="remember-me"
                  className="ml-2 block text-sm text-dark">
                  
                  Remember me
                </label>
              </div>

              <div className="text-sm">
                <a
                  href="#"
                  className="font-medium text-primary-dark hover:text-dark underline underline-offset-2">
                  
                  Forgot your password?
                </a>
              </div>
            </div>

            <div>
              <Button type="submit" className="w-full" isLoading={isLoading}>
                Sign in
              </Button>
            </div>
            {error && (
              <p className="text-sm text-red-600 text-center">{error}</p>
            )}
          </form>
        </Card>
      </motion.div>
    </div>);

}