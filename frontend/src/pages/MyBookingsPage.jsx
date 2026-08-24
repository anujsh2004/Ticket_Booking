import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Ticket, QrCode, XCircle, Calendar, MapPin, CheckCircle2, AlertTriangle, ArrowRight } from 'lucide-react';

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);
  const [cancelModalBooking, setCancelModalBooking] = useState(null);
  const [msg, setMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      const res = await api.get('/bookings/my');
      setBookings(res.data || []);
    } catch (err) {
      console.error('Failed to load bookings:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelBooking = async () => {
    if (!cancelModalBooking) return;
    try {
      setCancellingId(cancelModalBooking.id);
      await api.post(`/bookings/${cancelModalBooking.id}/cancel`);
      setMsg({
        type: 'success',
        text: 'Booking cancelled successfully. The seat has been automatically reassigned to the next waitlisted customer!',
      });
      setCancelModalBooking(null);
      fetchBookings();
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 py-12 px-4 sm:px-6 lg:px-8 pb-24">
      <div className="max-w-5xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              My Tickets & Bookings
            </h1>
            <p className="text-xs text-slate-400 mt-1">Manage your event bookings and QR passes</p>
          </div>
          <div className="px-3.5 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs font-bold flex items-center space-x-1.5">
            <Ticket className="w-3.5 h-3.5" />
            <span>{bookings.filter((b) => b.status === 'CONFIRMED').length} Active Tickets</span>
          </div>
        </div>

        {msg.text && (
          <div
            className={`mb-6 p-4 rounded-xl text-xs flex items-center space-x-3 ${
              msg.type === 'success'
                ? 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border border-rose-500/30 text-rose-300'
            }`}
          >
            {msg.type === 'success' ? <CheckCircle2 className="w-5 h-5 shrink-0" /> : <AlertTriangle className="w-5 h-5 shrink-0" />}
            <span>{msg.text}</span>
          </div>
        )}

        {loading ? (
          <div className="py-20 text-center">
            <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
            <p className="text-xs text-slate-400">Loading your tickets...</p>
          </div>
        ) : bookings.length === 0 ? (
          <div className="glass-panel p-12 rounded-3xl text-center border border-slate-800">
            <Ticket className="w-12 h-12 text-slate-600 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-white">No bookings yet</h3>
            <p className="text-xs text-slate-400 mt-1 mb-6">Explore trending events and reserve your seats today.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {bookings.map((booking) => {
              const isCancelled = booking.status === 'CANCELLED';

              return (
                <div
                  key={booking.id}
                  className={`glass-panel p-6 rounded-2xl border transition-all ${
                    isCancelled
                      ? 'border-slate-800/40 opacity-60'
                      : 'border-slate-800 hover:border-indigo-500/40 shadow-lg shadow-black/20'
                  } flex flex-col md:flex-row items-start md:items-center justify-between gap-6`}
                >
                  <div className="flex items-start space-x-4">
                    <div
                      className={`w-12 h-12 rounded-xl border flex items-center justify-center font-bold text-xs shrink-0 ${
                        isCancelled
                          ? 'bg-rose-500/10 border-rose-500/20 text-rose-400'
                          : 'bg-indigo-600/10 border-indigo-500/20 text-indigo-400'
                      }`}
                    >
                      <Ticket className="w-5 h-5" />
                    </div>

                    <div>
                      <div className="flex items-center space-x-2">
                        <h3 className="text-base font-bold text-white">{booking.eventTitle}</h3>
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                            isCancelled
                              ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                              : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          }`}
                        >
                          {booking.status}
                        </span>
                      </div>

                      <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400 mt-2">
                        <div className="flex items-center space-x-1">
                          <MapPin className="w-3.5 h-3.5 text-indigo-400" />
                          <span>{booking.venueName}</span>
                        </div>
                        <span>•</span>
                        <div className="flex items-center space-x-1">
                          <Calendar className="w-3.5 h-3.5 text-purple-400" />
                          <span>{new Date(booking.showStartTime).toLocaleString()}</span>
                        </div>
                        <span>•</span>
                        <span className="font-mono text-slate-300">Ref: {booking.bookingReference}</span>
                      </div>

                      <div className="flex flex-wrap items-center gap-2 mt-3">
                        {booking.seats?.map((seat) => (
                          <span
                            key={seat.id}
                            className="px-2.5 py-1 rounded-lg text-xs font-bold bg-slate-900 border border-slate-800 text-indigo-300"
                          >
                            Row {seat.rowNumber}, Seat {seat.seatNumber} ({seat.categoryName})
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Actions & Price */}
                  <div className="flex flex-row md:flex-col items-center md:items-end justify-between w-full md:w-auto gap-4 pt-4 md:pt-0 border-t md:border-t-0 border-slate-800">
                    <div className="text-right">
                      <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                        Total Paid
                      </span>
                      <span className="text-lg font-black text-indigo-400">
                        ₹{Number(booking.totalAmount).toFixed(2)}
                      </span>
                    </div>

                    {!isCancelled && (
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => setSelectedTicket(booking)}
                          className="px-3.5 py-2 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/20 transition-all flex items-center space-x-1.5"
                        >
                          <QrCode className="w-3.5 h-3.5" />
                          <span>QR Pass</span>
                        </button>
                        <button
                          onClick={() => setCancelModalBooking(booking)}
                          className="px-3.5 py-2 rounded-xl text-xs font-bold bg-slate-900 hover:bg-rose-500/10 text-slate-400 hover:text-rose-400 border border-slate-800 hover:border-rose-500/30 transition-all flex items-center space-x-1.5"
                        >
                          <XCircle className="w-3.5 h-3.5" />
                          <span>Cancel</span>
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* QR Pass Modal */}
      {selectedTicket && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-sm w-full glass-panel-glow p-6 rounded-3xl border border-indigo-500/30 text-center space-y-4">
            <h3 className="text-base font-extrabold text-white">{selectedTicket.eventTitle}</h3>
            <p className="text-xs text-slate-400">{selectedTicket.venueName}</p>

            <div className="p-4 bg-white rounded-2xl mx-auto my-3 flex flex-col items-center">
              <img
                src={selectedTicket.qrCodeBase64}
                alt="Ticket QR"
                className="w-52 h-52 object-contain"
              />
              <span className="text-[10px] text-slate-900 font-mono font-bold mt-2">
                {selectedTicket.bookingReference}
              </span>
            </div>

            <div className="text-xs text-slate-300">
              <p>Seats: <strong>{selectedTicket.seats?.map((s) => `${s.rowNumber}${s.seatNumber}`).join(', ')}</strong></p>
              <p className="text-slate-400 mt-1">{new Date(selectedTicket.showStartTime).toLocaleString()}</p>
            </div>

            <button
              onClick={() => setSelectedTicket(null)}
              className="w-full py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold transition-colors"
            >
              Close Pass
            </button>
          </div>
        </div>
      )}

      {/* Cancel Confirmation Modal */}
      {cancelModalBooking && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-md w-full glass-panel p-6 rounded-2xl border border-rose-500/30 space-y-4">
            <div className="flex items-center space-x-3 text-rose-400">
              <AlertTriangle className="w-6 h-6 shrink-0" />
              <h3 className="text-base font-bold text-white">Cancel this booking?</h3>
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Are you sure you want to cancel booking <strong className="text-white">{cancelModalBooking.bookingReference}</strong> for <strong className="text-white">{cancelModalBooking.eventTitle}</strong>?
            </p>
            <div className="p-3 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-[11px] text-indigo-300">
              ℹ️ Upon cancellation, these seats will be immediately offered to waiting customers in line.
            </div>

            <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
              <button
                type="button"
                onClick={() => setCancelModalBooking(null)}
                className="px-4 py-2 rounded-xl text-xs font-medium text-slate-400 hover:text-white"
              >
                Keep Booking
              </button>
              <button
                type="button"
                disabled={cancellingId === cancelModalBooking.id}
                onClick={handleCancelBooking}
                className="px-5 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 disabled:opacity-50 text-white text-xs font-bold shadow-lg shadow-rose-600/30 transition-all"
              >
                {cancellingId ? 'Cancelling...' : 'Yes, Cancel Booking'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
