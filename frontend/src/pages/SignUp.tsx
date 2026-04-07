import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User } from 'lucide-react';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { motion } from 'framer-motion';
import { userApi, setToken } from '../utils/api';

interface SignUpProps {
  onLogin: () => void;
}

export function SignUp({ onLogin }: SignUpProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const form = e.currentTarget;
    const username = (form.elements.namedItem('username') as HTMLInputElement).value;
    const email = (form.elements.namedItem('email') as HTMLInputElement).value;
    const password = (form.elements.namedItem('password') as HTMLInputElement).value;

    try {
      await userApi.register(username, email, password);
      // Sau khi signup thành công, tự động login để lấy token
      const res = await userApi.login(username, password);
      setToken(res.token);
      onLogin();
      navigate('/problems');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
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
          Create your account
        </h2>
        <p className="mt-2 text-center text-sm text-muted-dark">
          Already have an account?{' '}
          <Link
            to="/login"
            className="font-medium text-primary-dark hover:text-dark transition-colors underline underline-offset-2">
            
            Sign in
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
              <label
                htmlFor="username"
                className="block text-sm font-medium text-dark">
                
                Username
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <User className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="username"
                  name="username"
                  type="text"
                  required
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-primary-dark focus:border-primary-dark sm:text-sm"
                  placeholder="johndoe" />
                
              </div>
            </div>

            <div>
              <label
                htmlFor="email"
                className="block text-sm font-medium text-dark">
                
                Email address
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-primary-dark focus:border-primary-dark sm:text-sm"
                  placeholder="you@example.com" />
                
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
                  autoComplete="new-password"
                  required
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-primary-dark focus:border-primary-dark sm:text-sm"
                  placeholder="••••••••" />
                
              </div>
            </div>

            <div>
              <Button type="submit" className="w-full" isLoading={isLoading}>
                Sign up
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