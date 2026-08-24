import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Ticket, Lock, Mail, ArrowRight, AlertCircle, Sparkles } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setErrorMsg('');
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      setErrorMsg(err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (demoEmail, demoPassword) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background ambient radial glow */}
      <div className="absolute w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl -top-10 -left-10 pointer-events-none"></div>
      <div className="absolute w-96 h-96 bg-purple-600/20 rounded-full blur-3xl -bottom-10 -right-10 pointer-events-none"></div>

      <div className="max-w-md w-full space-y-6 relative z-10">
        {/* Brand Header */}
        <div className="text-center">
          <div className="w-12 h-12 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-xl shadow-indigo-600/30 mx-auto mb-4">
            <Ticket className="w-6 h-6 text-white" />
          </div>
          <h2 className="text-3xl font-extrabold text-white tracking-tight">Welcome Back</h2>
          <p className="text-xs text-slate-400 mt-1">Sign in to your SlowMO account</p>
        </div>

        {/* Form Container */}
        <div className="glass-panel-glow p-8 rounded-3xl border border-slate-800 space-y-6">
          {errorMsg && (
            <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center space-x-2.5">
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
              <span>{errorMsg}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block text-slate-300 font-medium mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-10 pr-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-slate-300 font-medium mb-1.5">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-10 pr-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-bold shadow-lg shadow-indigo-600/30 transition-all hover:scale-[1.02] flex items-center justify-center space-x-2"
            >
              {loading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Quick Demo Fillers */}
          <div className="pt-4 border-t border-slate-800 space-y-2">
            <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider block text-center">
              ⚡ Quick Demo Login Credentials
            </span>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickLogin('customer@tickets.com', 'customer123')}
                className="p-2 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 text-[10px] text-emerald-400 font-bold transition-all"
              >
                Customer
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('organiser@tickets.com', 'organiser123')}
                className="p-2 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 text-[10px] text-purple-400 font-bold transition-all"
              >
                Organiser
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('admin@tickets.com', 'admin123')}
                className="p-2 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 text-[10px] text-rose-400 font-bold transition-all"
              >
                Admin
              </button>
            </div>
          </div>
        </div>

        {/* Footer link */}
        <p className="text-center text-xs text-slate-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-indigo-400 hover:text-indigo-300 font-bold">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  );
}
