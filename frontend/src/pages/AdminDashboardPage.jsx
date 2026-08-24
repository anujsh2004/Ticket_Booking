import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Shield, Plus, Building2, Grid, Armchair, CheckCircle2, AlertCircle } from 'lucide-react';

export default function AdminDashboardPage() {
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [createVenueOpen, setCreateVenueOpen] = useState(false);
  const [configureSeatsVenue, setConfigureSeatsVenue] = useState(null);

  const [venueForm, setVenueForm] = useState({ name: '', location: '' });
  const [rowsConfig, setRowsConfig] = useState([
    { rowNumber: 'A', startSeatNumber: 1, endSeatNumber: 10, categoryName: 'VIP' },
    { rowNumber: 'B', startSeatNumber: 1, endSeatNumber: 10, categoryName: 'PREMIUM' },
    { rowNumber: 'C', startSeatNumber: 1, endSeatNumber: 10, categoryName: 'STANDARD' },
  ]);

  const [msg, setMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchVenues();
  }, []);

  const fetchVenues = async () => {
    try {
      setLoading(true);
      const res = await api.get('/venues');
      setVenues(res.data || []);
    } catch (err) {
      console.error('Failed to load venues:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateVenue = async (e) => {
    e.preventDefault();
    try {
      await api.post('/venues', venueForm);
      setMsg({ type: 'success', text: 'Venue created successfully!' });
      setCreateVenueOpen(false);
      setVenueForm({ name: '', location: '' });
      fetchVenues();
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  };

  const handleConfigureSeats = async (e) => {
    e.preventDefault();
    if (!configureSeatsVenue) return;
    try {
      await api.post(`/venues/${configureSeatsVenue.id}/seats`, {
        rows: rowsConfig,
      });
      setMsg({ type: 'success', text: 'Seats batch created successfully!' });
      setConfigureSeatsVenue(null);
      fetchVenues();
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 py-12 px-4 sm:px-6 lg:px-8 pb-24">
      <div className="max-w-6xl mx-auto space-y-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2">
              <Shield className="w-6 h-6 text-rose-400" />
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Admin Venue & Layout Management
              </h1>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Configure physical stadium/auditorium seating layouts and tiers
            </p>
          </div>
          <button
            onClick={() => setCreateVenueOpen(true)}
            className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all hover:scale-105 flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Create New Venue</span>
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

        {/* Venues Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {venues.map((venue) => (
            <div
              key={venue.id}
              className="glass-panel p-6 rounded-2xl border border-slate-800 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 rounded-xl bg-indigo-600/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
                      <Building2 className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-base font-bold text-white">{venue.name}</h3>
                      <p className="text-xs text-slate-400">{venue.location}</p>
                    </div>
                  </div>
                  <span className="px-2.5 py-1 rounded-lg text-xs font-bold bg-slate-900 border border-slate-800 text-emerald-400">
                    {venue.totalSeats} Total Seats
                  </span>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-500">
                  Configured: {new Date(venue.createdAt).toLocaleDateString()}
                </span>
                <button
                  onClick={() => setConfigureSeatsVenue(venue)}
                  className="px-4 py-2 rounded-xl text-xs font-bold bg-slate-900 hover:bg-slate-800 text-indigo-300 border border-indigo-500/30 transition-all flex items-center space-x-1.5"
                >
                  <Grid className="w-3.5 h-3.5" />
                  <span>Configure Seat Grid</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Create Venue Modal */}
      {createVenueOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-md w-full glass-panel-glow p-6 rounded-3xl border border-indigo-500/30 space-y-4">
            <h3 className="text-lg font-bold text-white">Create Venue</h3>
            <form onSubmit={handleCreateVenue} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Venue Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Dolby Theatre, Hollywood"
                  value={venueForm.name}
                  onChange={(e) => setVenueForm({ ...venueForm, name: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Location / Address</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Los Angeles, CA"
                  value={venueForm.location}
                  onChange={(e) => setVenueForm({ ...venueForm, location: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setCreateVenueOpen(false)}
                  className="px-4 py-2 rounded-xl text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold shadow-lg shadow-indigo-600/30"
                >
                  Save Venue
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Configure Seats Modal */}
      {configureSeatsVenue && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-lg w-full glass-panel-glow p-6 rounded-3xl border border-indigo-500/30 space-y-4">
            <h3 className="text-lg font-bold text-white">
              Batch Configure Layout for {configureSeatsVenue.name}
            </h3>
            <p className="text-xs text-slate-400">
              Define row ranges and category tiers to generate physical venue seating.
            </p>

            <form onSubmit={handleConfigureSeats} className="space-y-3 text-xs">
              {rowsConfig.map((row, idx) => (
                <div
                  key={idx}
                  className="p-3 rounded-xl bg-slate-900 border border-slate-800 grid grid-cols-4 gap-2 items-center"
                >
                  <div>
                    <label className="text-[10px] text-slate-400 block mb-1">Row</label>
                    <input
                      type="text"
                      value={row.rowNumber}
                      onChange={(e) => {
                        const newRows = [...rowsConfig];
                        newRows[idx].rowNumber = e.target.value;
                        setRowsConfig(newRows);
                      }}
                      className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-800 text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-400 block mb-1">Start #</label>
                    <input
                      type="number"
                      value={row.startSeatNumber}
                      onChange={(e) => {
                        const newRows = [...rowsConfig];
                        newRows[idx].startSeatNumber = parseInt(e.target.value);
                        setRowsConfig(newRows);
                      }}
                      className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-800 text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-400 block mb-1">End #</label>
                    <input
                      type="number"
                      value={row.endSeatNumber}
                      onChange={(e) => {
                        const newRows = [...rowsConfig];
                        newRows[idx].endSeatNumber = parseInt(e.target.value);
                        setRowsConfig(newRows);
                      }}
                      className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-800 text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-400 block mb-1">Category</label>
                    <select
                      value={row.categoryName}
                      onChange={(e) => {
                        const newRows = [...rowsConfig];
                        newRows[idx].categoryName = e.target.value;
                        setRowsConfig(newRows);
                      }}
                      className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-800 text-white"
                    >
                      <option value="VIP">VIP</option>
                      <option value="PREMIUM">PREMIUM</option>
                      <option value="STANDARD">STANDARD</option>
                    </select>
                  </div>
                </div>
              ))}

              <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setConfigureSeatsVenue(null)}
                  className="px-4 py-2 rounded-xl text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold shadow-lg shadow-indigo-600/30"
                >
                  Generate Seats Batch
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
