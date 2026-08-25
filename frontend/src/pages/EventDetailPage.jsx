import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import { MapPin, Calendar, Clock, ArrowRight, ShieldCheck, Ticket, Users } from 'lucide-react';

export default function EventDetailPage() {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchEvent();
  }, [id]);

  const fetchEvent = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/events/${id}`);
      setEvent(res.data);
    } catch (err) {
      console.error('Failed to fetch event:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (isoString) => {
    const date = new Date(isoString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const formatTime = (isoString) => {
    const date = new Date(isoString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-16 text-center">
        <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p className="text-sm text-slate-400">Loading event details...</p>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-16 text-center glass-panel rounded-2xl">
        <h2 className="text-xl font-bold text-white">Unable to load event details</h2>
        <p className="text-sm text-slate-400 mt-2">The server may be warming up or the event is unavailable.</p>
        <div className="mt-6 flex items-center justify-center space-x-4">
          <button
            onClick={fetchEvent}
            className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-600/30 transition-all hover:scale-105"
          >
            Retry Loading
          </button>
          <Link to="/" className="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-semibold border border-slate-700 transition-all">
            Return to Events
          </Link>
        </div>
      </div>
    );
  }


  return (
    <div className="min-h-screen bg-slate-950 pb-20">
      {/* Event Header Banner */}
      <div className="relative h-96 w-full overflow-hidden bg-slate-900 border-b border-slate-800">
        <img
          src={event.imageUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1200&q=80'}
          alt={event.title}
          className="w-full h-full object-cover opacity-30 blur-sm scale-105"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/60 to-transparent"></div>

        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-full flex items-end pb-12 relative z-10">
          <div className="flex flex-col sm:flex-row items-start sm:items-end gap-6">
            <img
              src={event.imageUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=400&q=80'}
              alt={event.title}
              className="w-32 h-44 sm:w-44 sm:h-60 object-cover rounded-2xl shadow-2xl border-2 border-slate-700 shrink-0"
            />
            <div>
              <span className="px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                {event.eventType}
              </span>
              <h1 className="text-3xl sm:text-5xl font-extrabold text-white tracking-tight mt-3">
                {event.title}
              </h1>
              <div className="flex flex-wrap items-center gap-4 mt-3 text-xs text-slate-300">
                <div className="flex items-center space-x-1.5">
                  <MapPin className="w-4 h-4 text-indigo-400" />
                  <span>{event.venueName} ({event.venueLocation})</span>
                </div>
                <div className="flex items-center space-x-1.5">
                  <ShieldCheck className="w-4 h-4 text-emerald-400" />
                  <span>Organised by {event.organiserName}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-10">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left: Description */}
          <div className="lg:col-span-1 space-y-6">
            <div className="glass-panel p-6 rounded-2xl border border-slate-800">
              <h3 className="text-base font-bold text-white mb-3">About the Event</h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                {event.description || 'No description available for this event.'}
              </p>
            </div>

            <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-3">
              <h3 className="text-base font-bold text-white">Booking Guarantee</h3>
              <ul className="text-xs text-slate-400 space-y-2">
                <li className="flex items-start space-x-2">
                  <span className="text-indigo-400 font-bold">•</span>
                  <span>10-minute hold lock prevents concurrent double bookings.</span>
                </li>
                <li className="flex items-start space-x-2">
                  <span className="text-indigo-400 font-bold">•</span>
                  <span>Automatic waitlist priority if seats sell out.</span>
                </li>
                <li className="flex items-start space-x-2">
                  <span className="text-indigo-400 font-bold">•</span>
                  <span>Instant digital QR code ticket delivered to email.</span>
                </li>
              </ul>
            </div>
          </div>

          {/* Right: Available Shows */}
          <div className="lg:col-span-2 space-y-4">
            <h2 className="text-xl font-bold text-white">Select a Show Time</h2>

            {event.shows?.length === 0 ? (
              <div className="glass-panel p-10 rounded-2xl text-center">
                <Calendar className="w-10 h-10 text-slate-600 mx-auto mb-2" />
                <p className="text-sm text-slate-400">No shows scheduled yet for this event.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {event.shows.map((show) => {
                  const isSoldOut = show.availableSeats === 0;

                  return (
                    <div
                      key={show.id}
                      className="glass-panel p-5 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition-all duration-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                    >
                      <div className="flex items-start space-x-4">
                        <div className="w-12 h-12 rounded-xl bg-indigo-600/10 border border-indigo-500/20 flex flex-col items-center justify-center text-indigo-400 shrink-0">
                          <Calendar className="w-5 h-5" />
                        </div>
                        <div>
                          <h4 className="text-sm font-bold text-white">
                            {formatDate(show.startTime)}
                          </h4>
                          <div className="flex items-center space-x-3 text-xs text-slate-400 mt-1">
                            <div className="flex items-center space-x-1">
                              <Clock className="w-3.5 h-3.5 text-indigo-400" />
                              <span>{formatTime(show.startTime)} - {formatTime(show.endTime)}</span>
                            </div>
                            <span>•</span>
                            <div className="flex items-center space-x-1">
                              <Users className="w-3.5 h-3.5 text-emerald-400" />
                              <span className={isSoldOut ? 'text-rose-400 font-semibold' : 'text-emerald-400'}>
                                {isSoldOut ? 'Sold Out' : `${show.availableSeats} seats left`}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>

                      <Link
                        to={`/shows/${show.id}/seats`}
                        className={`py-2.5 px-5 rounded-xl text-xs font-bold transition-all flex items-center justify-center space-x-2 shrink-0 ${
                          isSoldOut
                            ? 'bg-amber-600 hover:bg-amber-500 text-white shadow-lg shadow-amber-600/20'
                            : 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 hover:scale-105'
                        }`}
                      >
                        <Ticket className="w-4 h-4" />
                        <span>{isSoldOut ? 'Join Waitlist' : 'Select Seats'}</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
