import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { DollarSign, Ticket, Calendar, TrendingUp, Plus, Film, Clock, MapPin, CheckCircle2, AlertCircle } from 'lucide-react';

export default function OrganiserDashboardPage() {
  const [stats, setStats] = useState(null);
  const [events, setEvents] = useState([]);
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modals
  const [createEventOpen, setCreateEventOpen] = useState(false);
  const [createShowOpen, setCreateShowOpen] = useState(false);
  const [selectedEventForShow, setSelectedEventForShow] = useState(null);

  // Form states
  const [eventForm, setEventForm] = useState({
    title: '',
    description: '',
    imageUrl: '',
    eventType: 'MOVIE',
    venueId: '',
  });

  const [showForm, setShowForm] = useState({
    startTime: '',
    endTime: '',
    venueId: '',
    vipPrice: '180.00',
    premiumPrice: '95.00',
    standardPrice: '45.00',
  });

  const [msg, setMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [statsRes, eventsRes, venuesRes] = await Promise.all([
        api.get('/organiser/dashboard'),
        api.get('/events'),
        api.get('/venues'),
      ]);
      setStats(statsRes.data);
      setEvents(eventsRes.data || []);
      setVenues(venuesRes.data || []);
      if (venuesRes.data?.length > 0) {
        setEventForm((prev) => ({ ...prev, venueId: venuesRes.data[0].id }));
      }
    } catch (err) {
      console.error('Failed to load organiser dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    try {
      await api.post('/events', eventForm);
      setMsg({ type: 'success', text: 'Event created successfully!' });
      setCreateEventOpen(false);
      setEventForm({
        title: '',
        description: '',
        imageUrl: '',
        eventType: 'MOVIE',
        venueId: venues[0]?.id || '',
      });
      fetchDashboardData();
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  };

  const handleCreateShow = async (e) => {
    e.preventDefault();
    try {
      const pricing = [
        { categoryName: 'VIP', price: parseFloat(showForm.vipPrice) },
        { categoryName: 'PREMIUM', price: parseFloat(showForm.premiumPrice) },
        { categoryName: 'STANDARD', price: parseFloat(showForm.standardPrice) },
      ];

      await api.post(`/events/${selectedEventForShow.id}/shows`, {
        startTime: showForm.startTime,
        endTime: showForm.endTime,
        venueId: showForm.venueId || selectedEventForShow.venueId,
        pricing,
      });

      setMsg({ type: 'success', text: 'Show scheduled and seats initialized successfully!' });
      setCreateShowOpen(false);
      fetchDashboardData();
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 py-12 px-4 sm:px-6 lg:px-8 pb-24">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Top Header */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Organiser Dashboard & Analytics
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Live ticketing revenue, event performance, and show scheduling
            </p>
          </div>
          <button
            onClick={() => setCreateEventOpen(true)}
            className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all hover:scale-105 flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Create New Event</span>
          </button>
        </div>

        {msg.text && (
          <div
            className={`p-4 rounded-xl text-xs flex items-center space-x-3 ${
              msg.type === 'success'
                ? 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border border-rose-500/30 text-rose-300'
            }`}
          >
            {msg.type === 'success' ? <CheckCircle2 className="w-5 h-5 shrink-0" /> : <AlertCircle className="w-5 h-5 shrink-0" />}
            <span>{msg.text}</span>
          </div>
        )}

        {/* Analytics KPI Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <DollarSign className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs text-slate-400">Total Revenue</span>
              <p className="text-2xl font-black text-white mt-0.5">
                ${Number(stats?.totalRevenue || 0).toFixed(2)}
              </p>
            </div>
          </div>

          <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
              <Ticket className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs text-slate-400">Confirmed Tickets</span>
              <p className="text-2xl font-black text-white mt-0.5">
                {stats?.totalTicketsSold || 0}
              </p>
            </div>
          </div>

          <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
              <Film className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs text-slate-400">Total Events</span>
              <p className="text-2xl font-black text-white mt-0.5">{events.length}</p>
            </div>
          </div>

          <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
              <Calendar className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs text-slate-400">Active Shows</span>
              <p className="text-2xl font-black text-white mt-0.5">{stats?.activeShows || 0}</p>
            </div>
          </div>
        </div>

        {/* Events Management List */}
        <div className="glass-panel rounded-2xl border border-slate-800 p-6 space-y-6">
          <h2 className="text-lg font-bold text-white">Your Events & Shows</h2>

          <div className="space-y-4">
            {events.map((event) => (
              <div
                key={event.id}
                className="p-5 rounded-xl bg-slate-900/60 border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
              >
                <div className="flex items-start space-x-4">
                  <img
                    src={event.imageUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=200&q=80'}
                    alt={event.title}
                    className="w-16 h-20 object-cover rounded-xl border border-slate-700 shrink-0"
                  />
                  <div>
                    <div className="flex items-center space-x-2">
                      <h3 className="text-base font-bold text-white">{event.title}</h3>
                      <span className="px-2 py-0.5 rounded text-[10px] uppercase font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                        {event.eventType}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-1">{event.venueName} • {event.venueLocation}</p>
                    <p className="text-xs text-purple-300 mt-2 font-semibold">
                      {event.shows?.length || 0} Shows Configured
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-3 w-full md:w-auto">
                  <button
                    onClick={() => {
                      setSelectedEventForShow(event);
                      setShowForm((prev) => ({ ...prev, venueId: event.venueId }));
                      setCreateShowOpen(true);
                    }}
                    className="w-full md:w-auto px-4 py-2 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-500 text-white shadow-md shadow-indigo-600/20 transition-all flex items-center justify-center space-x-1.5"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>Schedule Show</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Bookings Table */}
        <div className="glass-panel rounded-2xl border border-slate-800 p-6 space-y-4">
          <h2 className="text-lg font-bold text-white">Recent Customer Bookings</h2>

          {stats?.recentBookings?.length === 0 ? (
            <p className="text-xs text-slate-500 py-6 text-center">No bookings recorded yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="text-[10px] text-slate-400 uppercase bg-slate-900/80 border-b border-slate-800">
                  <tr>
                    <th className="p-3">Ref</th>
                    <th className="p-3">Customer</th>
                    <th className="p-3">Event</th>
                    <th className="p-3">Seats</th>
                    <th className="p-3">Amount</th>
                    <th className="p-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {stats?.recentBookings?.map((b) => (
                    <tr key={b.id} className="hover:bg-slate-900/40 transition-colors">
                      <td className="p-3 font-mono font-bold text-indigo-400">{b.bookingReference}</td>
                      <td className="p-3">
                        <div className="font-bold text-white">{b.userName}</div>
                        <div className="text-[10px] text-slate-400">{b.userEmail}</div>
                      </td>
                      <td className="p-3 text-white">{b.eventTitle}</td>
                      <td className="p-3">
                        {b.seats?.map((s) => `${s.rowNumber}${s.seatNumber}`).join(', ')}
                      </td>
                      <td className="p-3 font-bold text-emerald-400">${Number(b.totalAmount).toFixed(2)}</td>
                      <td className="p-3">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                            b.status === 'CONFIRMED'
                              ? 'bg-emerald-500/10 text-emerald-400'
                              : 'bg-rose-500/10 text-rose-400'
                          }`}
                        >
                          {b.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Create Event Modal */}
      {createEventOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-lg w-full glass-panel-glow p-6 rounded-3xl border border-indigo-500/30 space-y-4">
            <h3 className="text-lg font-bold text-white">Create New Event</h3>
            <form onSubmit={handleCreateEvent} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Event Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Inception Re-release"
                  value={eventForm.title}
                  onChange={(e) => setEventForm({ ...eventForm, title: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Event Type</label>
                  <select
                    value={eventForm.eventType}
                    onChange={(e) => setEventForm({ ...eventForm, eventType: e.target.value })}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                  >
                    <option value="MOVIE">Movie</option>
                    <option value="CONCERT">Concert</option>
                  </select>
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Default Venue</label>
                  <select
                    value={eventForm.venueId}
                    onChange={(e) => setEventForm({ ...eventForm, venueId: e.target.value })}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                  >
                    {venues.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Poster Image URL</label>
                <input
                  type="url"
                  placeholder="https://..."
                  value={eventForm.imageUrl}
                  onChange={(e) => setEventForm({ ...eventForm, imageUrl: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Synopsis / Description</label>
                <textarea
                  rows="3"
                  placeholder="Brief event description..."
                  value={eventForm.description}
                  onChange={(e) => setEventForm({ ...eventForm, description: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                ></textarea>
              </div>

              <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setCreateEventOpen(false)}
                  className="px-4 py-2 rounded-xl text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold shadow-lg shadow-indigo-600/30"
                >
                  Create Event
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Schedule Show Modal */}
      {createShowOpen && selectedEventForShow && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-lg w-full glass-panel-glow p-6 rounded-3xl border border-indigo-500/30 space-y-4">
            <h3 className="text-lg font-bold text-white">
              Schedule Show for {selectedEventForShow.title}
            </h3>
            <p className="text-xs text-slate-400">
              Creating a show will automatically generate all venue seats with the prices configured below.
            </p>

            <form onSubmit={handleCreateShow} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Start Date & Time</label>
                  <input
                    type="datetime-local"
                    required
                    value={showForm.startTime}
                    onChange={(e) => setShowForm({ ...showForm, startTime: e.target.value })}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">End Date & Time</label>
                  <input
                    type="datetime-local"
                    required
                    value={showForm.endTime}
                    onChange={(e) => setShowForm({ ...showForm, endTime: e.target.value })}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              {/* Category Pricing Configurator */}
              <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 space-y-3">
                <span className="font-bold text-slate-300 block">Category Tier Pricing ($)</span>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="text-[10px] text-purple-400 font-bold block mb-1">VIP Tier</label>
                    <input
                      type="number"
                      step="0.01"
                      required
                      value={showForm.vipPrice}
                      onChange={(e) => setShowForm({ ...showForm, vipPrice: e.target.value })}
                      className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-blue-400 font-bold block mb-1">PREMIUM</label>
                    <input
                      type="number"
                      step="0.01"
                      required
                      value={showForm.premiumPrice}
                      onChange={(e) => setShowForm({ ...showForm, premiumPrice: e.target.value })}
                      className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-emerald-400 font-bold block mb-1">STANDARD</label>
                    <input
                      type="number"
                      step="0.01"
                      required
                      value={showForm.standardPrice}
                      onChange={(e) => setShowForm({ ...showForm, standardPrice: e.target.value })}
                      className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-white"
                    />
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setCreateShowOpen(false)}
                  className="px-4 py-2 rounded-xl text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold shadow-lg shadow-indigo-600/30"
                >
                  Confirm & Initialize Seats
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
