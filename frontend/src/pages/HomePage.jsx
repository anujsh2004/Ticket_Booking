import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { Search, Film, Music, MapPin, Calendar, ArrowRight, Sparkles, TrendingUp, RefreshCw, AlertCircle, Server } from 'lucide-react';

export default function HomePage() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isWakingUp, setIsWakingUp] = useState(false);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState('');
  const wakingTimerRef = useRef(null);

  useEffect(() => {
    fetchEvents();
    return () => {
      if (wakingTimerRef.current) clearTimeout(wakingTimerRef.current);
    };
  }, [search, selectedType]);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      setIsWakingUp(false);

      // If backend takes longer than 3.5s to respond, show warming-up notice
      if (wakingTimerRef.current) clearTimeout(wakingTimerRef.current);
      wakingTimerRef.current = setTimeout(() => {
        setIsWakingUp(true);
      }, 3500);

      const params = {};
      if (search) params.search = search;
      if (selectedType) params.type = selectedType;

      const res = await api.get('/events', { params });
      setEvents(res.data || []);
    } catch (err) {
      console.error('Failed to fetch events:', err);
      setError(err.message || 'Unable to connect to the backend server.');
    } finally {
      if (wakingTimerRef.current) clearTimeout(wakingTimerRef.current);
      setIsWakingUp(false);
      setLoading(false);
    }
  };


  return (
    <div className="min-h-screen bg-slate-950 pb-20">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 px-4 sm:px-6 lg:px-8 border-b border-slate-800/60">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(99,102,241,0.25),rgba(255,255,255,0))]"></div>

        <div className="max-w-5xl mx-auto text-center relative z-10">
          <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-xs font-semibold uppercase tracking-wider mb-6">
            <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
            <span>Live Concurrency-Protected Ticketing</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold text-white tracking-tight leading-tight mb-6">
            Instant Seats, Zero Collisions,{' '}
            <span className="bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
              Automated Waitlists
            </span>
          </h1>

          <p className="max-w-2xl mx-auto text-slate-400 text-base sm:text-lg mb-10 leading-relaxed">
            Experience real-time interactive seat maps with sub-second WebSocket updates, guaranteed 10-minute hold TTLs, and instant waitlist auto-assignment on cancellations.
          </p>

          {/* Search & Filter Bar */}
          <div className="max-w-2xl mx-auto flex flex-col sm:flex-row items-center gap-3 p-2 rounded-2xl glass-panel-glow">
            <div className="relative flex-1 w-full">
              <Search className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search movies, concerts, artists..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full bg-transparent pl-11 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none"
              />
            </div>

            <div className="flex items-center space-x-1.5 w-full sm:w-auto">
              <button
                onClick={() => setSelectedType('')}
                className={`px-3.5 py-2 rounded-xl text-xs font-semibold transition-all ${
                  selectedType === ''
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                All
              </button>
              <button
                onClick={() => setSelectedType('MOVIE')}
                className={`px-3.5 py-2 rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                  selectedType === 'MOVIE'
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Film className="w-3.5 h-3.5" />
                <span>Movies</span>
              </button>
              <button
                onClick={() => setSelectedType('CONCERT')}
                className={`px-3.5 py-2 rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                  selectedType === 'CONCERT'
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Music className="w-3.5 h-3.5" />
                <span>Concerts</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Events Grid Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-16">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold text-white tracking-tight">Trending Events</h2>
            <p className="text-sm text-slate-400 mt-1">Book your spot before seats sell out</p>
          </div>
          <div className="flex items-center space-x-2 text-xs font-semibold text-indigo-400">
            <TrendingUp className="w-4 h-4" />
            <span>Live Inventory</span>
          </div>
        </div>

        {/* Server Waking Up Notice */}
        {isWakingUp && (
          <div className="mb-8 p-4 rounded-2xl bg-indigo-950/60 border border-indigo-500/30 flex items-center justify-between gap-4 animate-pulse">
            <div className="flex items-center space-x-3">
              <div className="p-2 rounded-xl bg-indigo-600/20 text-indigo-400">
                <Server className="w-5 h-5 animate-spin" />
              </div>
              <div>
                <h4 className="text-xs font-bold text-indigo-200">Cloud Backend Starting Up</h4>
                <p className="text-[11px] text-indigo-300/80 mt-0.5">
                  Free-tier instances spin down during inactivity and take ~30–50s to warm up. Please wait a moment...
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Error / Connection Issue */}
        {error && !loading && (
          <div className="mb-8 p-6 rounded-2xl bg-rose-950/40 border border-rose-500/30 text-center glass-panel">
            <AlertCircle className="w-10 h-10 text-rose-400 mx-auto mb-2" />
            <h3 className="text-sm font-bold text-white">Connection Delay</h3>
            <p className="text-xs text-rose-300/80 mt-1 max-w-md mx-auto">{error}</p>
            <button
              onClick={fetchEvents}
              className="mt-4 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold inline-flex items-center space-x-2 shadow-lg shadow-indigo-600/30 transition-all hover:scale-105"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span>Retry Connection</span>
            </button>
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((n) => (
              <div key={n} className="h-96 rounded-2xl bg-slate-900/60 animate-pulse border border-slate-800"></div>
            ))}
          </div>
        ) : events.length === 0 && !error ? (
          <div className="text-center py-20 glass-panel rounded-2xl">
            <Film className="w-12 h-12 text-slate-600 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-white">No events found</h3>
            <p className="text-sm text-slate-400 mt-1">Try adjusting your search query or filter.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">

            {events.map((event) => (
              <div
                key={event.id}
                className="group rounded-2xl glass-panel border border-slate-800 hover:border-indigo-500/40 transition-all duration-300 overflow-hidden flex flex-col hover:-translate-y-1 hover:shadow-xl hover:shadow-indigo-500/10"
              >
                {/* Image & Type Badge */}
                <div className="relative h-52 w-full overflow-hidden bg-slate-900">
                  <img
                    src={event.imageUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80'}
                    alt={event.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent"></div>
                  <span className="absolute top-3 right-3 px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider bg-slate-950/80 backdrop-blur-md border border-white/10 text-indigo-300">
                    {event.eventType}
                  </span>
                </div>

                {/* Event Details */}
                <div className="p-5 flex-1 flex flex-col justify-between">
                  <div>
                    <h3 className="text-lg font-bold text-white group-hover:text-indigo-300 transition-colors line-clamp-1">
                      {event.title}
                    </h3>
                    <p className="text-xs text-slate-400 mt-2 line-clamp-2 leading-relaxed">
                      {event.description}
                    </p>

                    <div className="mt-4 pt-4 border-t border-slate-800/80 space-y-2">
                      <div className="flex items-center space-x-2 text-xs text-slate-300">
                        <MapPin className="w-3.5 h-3.5 text-indigo-400 shrink-0" />
                        <span className="truncate">{event.venueName} • {event.venueLocation}</span>
                      </div>
                      <div className="flex items-center space-x-2 text-xs text-slate-400">
                        <Calendar className="w-3.5 h-3.5 text-purple-400 shrink-0" />
                        <span>{event.shows?.length || 0} Shows Available</span>
                      </div>
                    </div>
                  </div>

                  <Link
                    to={`/events/${event.id}`}
                    className="mt-6 w-full py-2.5 px-4 rounded-xl bg-slate-900 hover:bg-indigo-600 text-slate-200 hover:text-white text-xs font-bold transition-all flex items-center justify-center space-x-1.5 border border-slate-800 hover:border-transparent group-hover:shadow-lg group-hover:shadow-indigo-600/20"
                  >
                    <span>View Shows & Select Seats</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
