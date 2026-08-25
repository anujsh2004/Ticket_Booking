import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WebSocketClient } from '../services/websocket';
import SeatMap from '../components/SeatMap';
import { Clock, ShieldAlert, ArrowRight, ArrowLeft, Users, CheckCircle2, AlertCircle, Sparkles } from 'lucide-react';

export default function SeatSelectionPage() {
  const { showId } = useParams();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [show, setShow] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState([]);
  const [heldSeatsData, setHeldSeatsData] = useState(null);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [loading, setLoading] = useState(true);
  const [holdingLoading, setHoldingLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [waitlistModalOpen, setWaitlistModalOpen] = useState(false);
  const [selectedCategoryForWaitlist, setSelectedCategoryForWaitlist] = useState('');
  const [waitlistSuccessMsg, setWaitlistSuccessMsg] = useState('');

  const timerRef = useRef(null);

  useEffect(() => {
    fetchShowAndSeats();

    // Connect WebSocket STOMP client for real-time seat status updates
    const ws = new WebSocketClient(showId, (message) => {
      if (message.updatedSeats) {
        setSeats((prevSeats) => {
          const updateMap = new Map();
          message.updatedSeats.forEach((u) => updateMap.set(u.showSeatId, u));

          return prevSeats.map((seat) => {
            if (updateMap.has(seat.id)) {
              const updated = updateMap.get(seat.id);
              const isHeldByMe = user ? updated.heldByUserId === user.id : false;
              return {
                ...seat,
                status: updated.status,
                heldByUserId: updated.heldByUserId,
                isHeldByMe,
              };
            }
            return seat;
          });
        });
      }
    });

    ws.connect();

    return () => {
      ws.disconnect();
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [showId, user?.id]);

  const fetchShowAndSeats = async () => {
    try {
      setLoading(true);
      const [showRes, seatsRes] = await Promise.all([
        api.get(`/shows/${showId}`),
        api.get(`/shows/${showId}/seats`),
      ]);
      setShow(showRes.data);
      setSeats(seatsRes.data || []);
    } catch (err) {
      setErrorMsg(err.message || 'Failed to load show data');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleSeat = (seat) => {
    setErrorMsg('');
    if (selectedSeatIds.includes(seat.id)) {
      setSelectedSeatIds(selectedSeatIds.filter((id) => id !== seat.id));
    } else {
      setSelectedSeatIds([...selectedSeatIds, seat.id]);
    }
  };

  // Start hold countdown timer
  const startHoldTimer = (seconds) => {
    if (timerRef.current) clearInterval(timerRef.current);
    setRemainingSeconds(seconds);

    timerRef.current = setInterval(() => {
      setRemainingSeconds((prev) => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          setHeldSeatsData(null);
          setSelectedSeatIds([]);
          setErrorMsg('Hold time expired! Seats have been automatically released.');
          fetchShowAndSeats();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const handleHoldSeats = async () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/shows/${showId}/seats` } });
      return;
    }

    if (selectedSeatIds.length === 0) {
      setErrorMsg('Please select at least one seat to proceed.');
      return;
    }

    try {
      setHoldingLoading(true);
      setErrorMsg('');
      const res = await api.post(`/shows/${showId}/holds`, {
        showSeatIds: selectedSeatIds,
      });

      setHeldSeatsData(res.data);
      startHoldTimer(res.data.remainingSeconds || 600);
    } catch (err) {
      setErrorMsg(err.message);
      fetchShowAndSeats();
    } finally {
      setHoldingLoading(false);
    }
  };

  const handleJoinWaitlist = async (categoryId) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/shows/${showId}/seats` } });
      return;
    }

    try {
      setErrorMsg('');
      const res = await api.post(`/shows/${showId}/waitlist`, {
        seatCategoryId: categoryId,
      });
      setWaitlistSuccessMsg(`You are position #${res.data.position} on the waitlist! We will notify you via email if a seat opens.`);
      setTimeout(() => {
        setWaitlistModalOpen(false);
        setWaitlistSuccessMsg('');
      }, 3000);
    } catch (err) {
      setErrorMsg(err.message);
    }
  };

  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const selectedSeatsSummary = seats.filter((s) => selectedSeatIds.includes(s.id));
  const totalAmount = selectedSeatsSummary.reduce((sum, s) => sum + Number(s.price), 0);

  // Group unique categories for waitlist option
  const categories = React.useMemo(() => {
    const map = new Map();
    seats.forEach((s) => {
      if (s.categoryId && !map.has(s.categoryId)) {
        map.set(s.categoryId, { id: s.categoryId, name: s.categoryName });
      }
    });
    return Array.from(map.values());
  }, [seats]);

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center">
        <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p className="text-sm text-slate-400">Loading live seating map...</p>
      </div>
    );
  }

  if (!show) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center glass-panel rounded-2xl">
        <h2 className="text-xl font-bold text-white">Unable to load seating map</h2>
        <p className="text-sm text-slate-400 mt-2">{errorMsg || 'The server may be warming up. Please try again.'}</p>
        <div className="mt-6 flex items-center justify-center space-x-4">
          <button
            onClick={fetchShowAndSeats}
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
    <div className="min-h-screen bg-slate-950 pb-32">
      {/* Top Sticky Header */}
      <div className="glass-panel border-b border-slate-800/80 sticky top-16 z-40 py-4 px-4 sm:px-6 lg:px-8 bg-slate-950/90 backdrop-blur-xl">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center space-x-3">
            <Link
              to={show?.eventId ? `/events/${show.eventId}` : '/'}
              className="p-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
            </Link>
            <div>
              <h1 className="text-base sm:text-lg font-bold text-white tracking-tight">
                {show?.eventTitle}
              </h1>
              <p className="text-xs text-slate-400">
                {show?.venueName} • {show?.venueLocation}
              </p>
            </div>
          </div>

          {/* Active Hold Countdown Badge */}
          {heldSeatsData && (
            <div className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 animate-pulse">
              <Clock className="w-4 h-4 text-amber-400" />
              <span className="text-xs font-bold uppercase tracking-wider">
                Hold Reserved: {formatTimer(remainingSeconds)}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Error Alert */}
      {errorMsg && (
        <div className="max-w-4xl mx-auto px-4 mt-6">
          <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 shrink-0 text-rose-400" />
            <span>{errorMsg}</span>
          </div>
        </div>
      )}

      {/* Main Seat Map Area */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-10">
        <SeatMap
          seats={seats}
          selectedSeatIds={selectedSeatIds}
          onToggleSeat={handleToggleSeat}
          currentUserId={user?.id}
        />
      </div>

      {/* Waitlist Banner if seats are scarce */}
      <div className="max-w-4xl mx-auto px-4 mt-10">
        <div className="glass-panel p-5 rounded-2xl border border-indigo-500/20 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-3 text-left">
            <div className="w-10 h-10 rounded-xl bg-purple-600/10 border border-purple-500/20 flex items-center justify-center text-purple-400 shrink-0">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Category Sold Out?</h4>
              <p className="text-xs text-slate-400">Join the automated queue to receive cancellation offers.</p>
            </div>
          </div>
          <button
            onClick={() => setWaitlistModalOpen(true)}
            className="px-4 py-2 rounded-xl text-xs font-bold bg-slate-900 hover:bg-slate-800 text-indigo-300 border border-indigo-500/30 transition-all hover:scale-105"
          >
            Join Waitlist
          </button>
        </div>
      </div>

      {/* Bottom Floating Booking Bar */}
      <div className="fixed bottom-0 left-0 right-0 z-40 glass-panel border-t border-slate-800/80 bg-slate-950/95 backdrop-blur-2xl py-4 px-4 sm:px-6 lg:px-8">
        <div className="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-6">
            <div>
              <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                Selected Seats ({selectedSeatIds.length})
              </span>
              <div className="text-sm font-bold text-white flex items-center space-x-1.5 mt-0.5">
                {selectedSeatsSummary.length > 0 ? (
                  selectedSeatsSummary.map((s) => `${s.rowNumber}${s.seatNumber}`).join(', ')
                ) : (
                  <span className="text-slate-500 font-normal">None selected</span>
                )}
              </div>
            </div>

            <div className="border-l border-slate-800 pl-6">
              <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                Total Price
              </span>
              <div className="text-lg font-extrabold text-indigo-400">
                ₹{totalAmount.toFixed(2)}
              </div>
            </div>
          </div>

          <div className="flex items-center space-x-3 w-full sm:w-auto">
            {!heldSeatsData ? (
              <button
                disabled={selectedSeatIds.length === 0 || holdingLoading}
                onClick={handleHoldSeats}
                className="w-full sm:w-auto px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all hover:scale-105 flex items-center justify-center space-x-2"
              >
                {holdingLoading ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  <>
                    <span>Hold Selected Seats (10 min)</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            ) : (
              <button
                onClick={() =>
                  navigate('/checkout', {
                    state: {
                      show,
                      heldSeats: heldSeatsData.heldSeats,
                      totalAmount: heldSeatsData.totalAmount,
                      expiresAt: heldSeatsData.expiresAt,
                    },
                  })
                }
                className="w-full sm:w-auto px-8 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-lg shadow-emerald-600/30 transition-all hover:scale-105 flex items-center justify-center space-x-2 animate-bounce"
              >
                <CheckCircle2 className="w-4 h-4" />
                <span>Proceed to Checkout</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Waitlist Modal */}
      {waitlistModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="max-w-md w-full glass-panel-glow p-6 rounded-2xl border border-indigo-500/30 relative">
            <h3 className="text-lg font-bold text-white mb-2">Join Event Waitlist</h3>
            <p className="text-xs text-slate-400 mb-6">
              When another customer cancels a ticket in your selected category, you will automatically receive a 15-minute priority offer.
            </p>

            {waitlistSuccessMsg ? (
              <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs">
                {waitlistSuccessMsg}
              </div>
            ) : (
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-2">
                    Select Desired Seat Category
                  </label>
                  <div className="space-y-2">
                    {categories.map((cat) => (
                      <button
                        key={cat.id}
                        type="button"
                        onClick={() => setSelectedCategoryForWaitlist(cat.id)}
                        className={`w-full p-3 rounded-xl border text-xs font-bold text-left transition-all ${
                          selectedCategoryForWaitlist === cat.id
                            ? 'bg-indigo-600/20 border-indigo-500 text-indigo-300'
                            : 'bg-slate-900/60 border-slate-800 text-slate-300 hover:bg-slate-800'
                        }`}
                      >
                        {cat.name} Tier
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
                  <button
                    type="button"
                    onClick={() => setWaitlistModalOpen(false)}
                    className="px-4 py-2 rounded-xl text-xs font-medium text-slate-400 hover:text-white"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    disabled={!selectedCategoryForWaitlist}
                    onClick={() => handleJoinWaitlist(selectedCategoryForWaitlist)}
                    className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all"
                  >
                    Confirm Waitlist Entry
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
