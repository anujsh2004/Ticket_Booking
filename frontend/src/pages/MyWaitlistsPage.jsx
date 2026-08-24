import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { ListOrdered, Clock, CheckCircle2, AlertCircle, ArrowRight, Sparkles, MapPin } from 'lucide-react';

export default function MyWaitlistsPage() {
  const [waitlists, setWaitlists] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchWaitlists();
    const interval = setInterval(fetchWaitlists, 5000); // Poll for real-time waitlist offer triggers
    return () => clearInterval(interval);
  }, []);

  const fetchWaitlists = async () => {
    try {
      const res = await api.get('/waitlist/my');
      setWaitlists(res.data || []);
    } catch (err) {
      console.error('Failed to load waitlists:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClaimOffer = (entry) => {
    const offer = entry.activeOffer;
    if (!offer) return;

    navigate('/checkout', {
      state: {
        show: {
          id: entry.showId,
          eventTitle: entry.eventTitle,
          venueName: entry.venueName,
          startTime: entry.showStartTime,
        },
        heldSeats: [
          {
            id: offer.showSeatId,
            rowNumber: offer.seatRow,
            seatNumber: offer.seatNumber,
            categoryName: offer.categoryName,
            price: offer.price,
          },
        ],
        totalAmount: offer.price,
        waitlistOfferId: offer.id,
      },
    });
  };

  return (
    <div className="min-h-screen bg-slate-950 py-12 px-4 sm:px-6 lg:px-8 pb-24">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              My Waitlist Queue
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Automated FIFO waitlists with instant cancellation reallocations
            </p>
          </div>
          <div className="px-3.5 py-1.5 rounded-full bg-purple-500/10 border border-purple-500/20 text-purple-300 text-xs font-bold flex items-center space-x-1.5">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Priority Reassignment</span>
          </div>
        </div>

        {loading ? (
          <div className="py-20 text-center">
            <div className="w-10 h-10 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
            <p className="text-xs text-slate-400">Loading waitlist entries...</p>
          </div>
        ) : waitlists.length === 0 ? (
          <div className="glass-panel p-12 rounded-3xl text-center border border-slate-800">
            <ListOrdered className="w-12 h-12 text-slate-600 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-white">No active waitlists</h3>
            <p className="text-xs text-slate-400 mt-1">
              When an event category sells out, you can join the queue directly from the seat map.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {waitlists.map((entry) => {
              const hasActiveOffer = entry.status === 'OFFERED' && entry.activeOffer;

              return (
                <div
                  key={entry.id}
                  className={`p-6 rounded-2xl border transition-all ${
                    hasActiveOffer
                      ? 'glass-panel-glow border-emerald-500/50 bg-emerald-950/20 shadow-xl shadow-emerald-500/10'
                      : 'glass-panel border-slate-800'
                  }`}
                >
                  <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                    <div className="flex items-start space-x-4">
                      <div
                        className={`w-12 h-12 rounded-xl border flex items-center justify-center font-bold text-sm shrink-0 ${
                          hasActiveOffer
                            ? 'bg-emerald-500/20 border-emerald-500/30 text-emerald-400 animate-pulse'
                            : 'bg-slate-900 border-slate-800 text-purple-400'
                        }`}
                      >
                        #{entry.position}
                      </div>

                      <div>
                        <div className="flex items-center space-x-2">
                          <h3 className="text-base font-bold text-white">{entry.eventTitle}</h3>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                              hasActiveOffer
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                                : entry.status === 'COMPLETED'
                                ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
                                : entry.status === 'EXPIRED'
                                ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                                : 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
                            }`}
                          >
                            {entry.status}
                          </span>
                        </div>

                        <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400 mt-2">
                          <div className="flex items-center space-x-1">
                            <MapPin className="w-3.5 h-3.5 text-indigo-400" />
                            <span>{entry.venueName}</span>
                          </div>
                          <span>•</span>
                          <span>{new Date(entry.showStartTime).toLocaleString()}</span>
                          <span>•</span>
                          <span className="text-purple-300 font-semibold">{entry.seatCategoryName} Tier</span>
                        </div>
                      </div>
                    </div>

                    {/* Claim Offer Button */}
                    {hasActiveOffer ? (
                      <div className="flex flex-col sm:items-end w-full sm:w-auto">
                        <div className="flex items-center space-x-1 text-xs text-amber-300 mb-2 font-bold animate-pulse">
                          <Clock className="w-3.5 h-3.5" />
                          <span>Offer expires in {Math.ceil(entry.activeOffer.remainingSeconds / 60)} mins</span>
                        </div>
                        <button
                          onClick={() => handleClaimOffer(entry)}
                          className="w-full sm:w-auto px-6 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-lg shadow-emerald-600/30 transition-all hover:scale-105 flex items-center justify-center space-x-2"
                        >
                          <CheckCircle2 className="w-4 h-4" />
                          <span>Claim Seat {entry.activeOffer.seatRow}{entry.activeOffer.seatNumber} (₹{Number(entry.activeOffer.price).toFixed(2)})</span>
                        </button>
                      </div>
                    ) : (
                      <div className="text-right">
                        <span className="text-xs text-slate-400">Queue Status</span>
                        <p className="text-xs font-bold text-purple-400">
                          {entry.status === 'WAITING' ? `Position #${entry.position} in line` : entry.status}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
