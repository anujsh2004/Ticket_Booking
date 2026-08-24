import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Ticket, Film, Calendar, ListOrdered, LayoutDashboard, Shield, LogOut, User, Sparkles } from 'lucide-react';

export default function Navbar() {
  const { user, logout, isAuthenticated, isAdmin, isOrganiser } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="sticky top-0 z-50 glass-panel border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand Logo */}
          <Link to="/" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-purple-500 flex items-center justify-center shadow-lg shadow-indigo-500/25 group-hover:scale-105 transition-transform duration-200">
              <Ticket className="w-5 h-5 text-white" />
            </div>
            <div className="flex flex-col">
              <span className="font-bold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                SeatPulse
              </span>
              <span className="text-[10px] text-indigo-400 font-semibold tracking-widest uppercase">
                Ticket Platform
              </span>
            </div>
          </Link>

          {/* Navigation Links */}
          <div className="hidden md:flex items-center space-x-1">
            <Link
              to="/"
              className="px-3.5 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-900 transition-colors flex items-center space-x-1.5"
            >
              <Film className="w-4 h-4 text-indigo-400" />
              <span>Explore Events</span>
            </Link>

            {isAuthenticated && (
              <>
                <Link
                  to="/my-bookings"
                  className="px-3.5 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-900 transition-colors flex items-center space-x-1.5"
                >
                  <Ticket className="w-4 h-4 text-emerald-400" />
                  <span>My Tickets</span>
                </Link>

                <Link
                  to="/my-waitlists"
                  className="px-3.5 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-900 transition-colors flex items-center space-x-1.5"
                >
                  <ListOrdered className="w-4 h-4 text-amber-400" />
                  <span>Waitlists</span>
                </Link>

                {isOrganiser && (
                  <Link
                    to="/organiser/dashboard"
                    className="px-3.5 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-900 transition-colors flex items-center space-x-1.5"
                  >
                    <LayoutDashboard className="w-4 h-4 text-purple-400" />
                    <span>Organiser Hub</span>
                  </Link>
                )}

                {isAdmin && (
                  <Link
                    to="/admin/dashboard"
                    className="px-3.5 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-900 transition-colors flex items-center space-x-1.5"
                  >
                    <Shield className="w-4 h-4 text-rose-400" />
                    <span>Admin Panel</span>
                  </Link>
                )}
              </>
            )}
          </div>

          {/* User Profile / Auth Action */}
          <div className="flex items-center space-x-3">
            {isAuthenticated ? (
              <div className="flex items-center space-x-3">
                <div className="hidden sm:flex flex-col text-right">
                  <span className="text-sm font-medium text-slate-200">{user?.name}</span>
                  <span className="text-[10px] font-semibold uppercase px-1.5 py-0.5 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 self-end">
                    {user?.role}
                  </span>
                </div>
                <button
                  onClick={handleLogout}
                  className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                  title="Log out"
                >
                  <LogOut className="w-5 h-5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-slate-300 hover:text-white transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 transition-all hover:scale-105"
                >
                  Get Started
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
