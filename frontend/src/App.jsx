import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import EventDetailPage from './pages/EventDetailPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import CheckoutPage from './pages/CheckoutPage';
import MyBookingsPage from './pages/MyBookingsPage';
import MyWaitlistsPage from './pages/MyWaitlistsPage';
import OrganiserDashboardPage from './pages/OrganiserDashboardPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Protected Route Component
const ProtectedRoute = ({ children, requireOrganiser, requireAdmin }) => {
  const { isAuthenticated, isOrganiser, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (requireOrganiser && !isOrganiser) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
          <Navbar />
          <main className="flex-1">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<HomePage />} />
              <Route path="/events/:id" element={<EventDetailPage />} />
              <Route path="/shows/:showId/seats" element={<SeatSelectionPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* Authenticated Customer Routes */}
              <Route
                path="/checkout"
                element={
                  <ProtectedRoute>
                    <CheckoutPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-bookings"
                element={
                  <ProtectedRoute>
                    <MyBookingsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-waitlists"
                element={
                  <ProtectedRoute>
                    <MyWaitlistsPage />
                  </ProtectedRoute>
                }
              />

              {/* Organiser Routes */}
              <Route
                path="/organiser/dashboard"
                element={
                  <ProtectedRoute requireOrganiser>
                    <OrganiserDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Admin Routes */}
              <Route
                path="/admin/dashboard"
                element={
                  <ProtectedRoute requireAdmin>
                    <AdminDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}
