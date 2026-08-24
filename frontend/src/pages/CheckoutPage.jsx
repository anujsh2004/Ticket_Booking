import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import confetti from 'canvas-confetti';
import { CheckCircle2, Ticket, CreditCard, ShieldCheck, Download, ArrowLeft, Clock, AlertCircle } from 'lucide-react';

export default function CheckoutPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const { show, heldSeats, totalAmount, waitlistOfferId } = location.state || {};

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [bookingConfirmed, setBookingConfirmed] = useState(null);

  if (!heldSeats || heldSeats.length === 0) {
    return (
      <div className="max-w-md mx-auto px-4 py-20 text-center glass-panel rounded-2xl mt-12">
        <AlertCircle className="w-12 h-12 text-amber-400 mx-auto mb-3" />
        <h2 className="text-lg font-bold text-white">No active seat hold found</h2>
        <p className="text-xs text-slate-400 mt-2 mb-6">
          Your hold session may have expired or you navigated directly here.
        </p>
        <Link
          to="/"
          className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold"
        >
          Return to Events
        </Link>
      </div>
    );
  }

  const handleConfirmBooking = async () => {
    try {
      setLoading(true);
      setErrorMsg('');

      const res = await api.post('/bookings', {
        showId: show.id,
        showSeatIds: heldSeats.map((s) => s.id),
        waitlistOfferId: waitlistOfferId || null,
      });

      setBookingConfirmed(res.data);

      // Trigger celebratory confetti
      confetti({
        particleCount: 120,
        spread: 70,
        origin: { y: 0.6 },
      });
    } catch (err) {
      setErrorMsg(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        {!bookingConfirmed ? (
          <div>
            <div className="flex items-center space-x-3 mb-8">
              <button
                onClick={() => navigate(-1)}
                className="p-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
              >
                <ArrowLeft className="w-4 h-4" />
              </button>
              <div>
                <h1 className="text-2xl font-extrabold text-white tracking-tight">
                  Checkout & Payment
                </h1>
                <p className="text-xs text-slate-400">Complete your booking before hold expires</p>
              </div>
            </div>

            {errorMsg && (
              <div className="mb-6 p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center space-x-3">
                <AlertCircle className="w-5 h-5 shrink-0 text-rose-400" />
                <span>{errorMsg}</span>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Left 2 Cols: Order Summary */}
              <div className="md:col-span-2 space-y-6">
                <div className="glass-panel p-6 rounded-2xl border border-slate-800">
                  <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
                    <Ticket className="w-4 h-4 text-indigo-400" />
                    <span>Booking Summary</span>
                  </h3>

                  <div className="space-y-3 pb-4 border-b border-slate-800">
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-400">Event:</span>
                      <span className="font-bold text-white">{show?.eventTitle}</span>
                    </div>
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-400">Venue:</span>
                      <span className="text-slate-300">{show?.venueName}</span>
                    </div>
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-400">Date & Time:</span>
                      <span className="text-slate-300">
                        {new Date(show?.startTime).toLocaleString()}
                      </span>
                    </div>
                  </div>

                  {/* Seat List */}
                  <div className="pt-4 space-y-2">
                    <span className="text-xs font-semibold text-slate-400">Reserved Seats:</span>
                    {heldSeats.map((seat) => (
                      <div
                        key={seat.id}
                        className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 text-xs"
                      >
                        <div className="flex items-center space-x-2">
                          <span className="font-bold text-white">
                            Row {seat.rowNumber}, Seat {seat.seatNumber}
                          </span>
                          <span className="px-2 py-0.5 rounded text-[10px] uppercase font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                            {seat.categoryName}
                          </span>
                        </div>
                        <span className="font-extrabold text-indigo-400">₹{Number(seat.price).toFixed(2)}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Simulated Payment details */}
                <div className="glass-panel p-6 rounded-2xl border border-slate-800">
                  <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
                    <CreditCard className="w-4 h-4 text-emerald-400" />
                    <span>Payment Method (Test Mode)</span>
                  </h3>
                  <div className="p-4 rounded-xl bg-slate-900/80 border border-slate-800 flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-8 h-8 rounded-lg bg-indigo-600/20 text-indigo-400 flex items-center justify-center font-bold text-xs">
                        💳
                      </div>
                      <div>
                        <p className="text-xs font-bold text-white">Instant Sandbox UPI / Card Payment</p>
                        <p className="text-[10px] text-slate-400">Zero charges • Auto-confirms order</p>
                      </div>
                    </div>
                    <span className="text-xs font-bold text-emerald-400">Ready</span>
                  </div>
                </div>
              </div>

              {/* Right Col: Price Breakdown & CTA */}
              <div className="md:col-span-1 space-y-6">
                <div className="glass-panel-glow p-6 rounded-2xl border border-indigo-500/30 space-y-4">
                  <h3 className="text-sm font-bold text-white">Order Total</h3>

                  <div className="space-y-2 text-xs pb-4 border-b border-slate-800">
                    <div className="flex justify-between text-slate-400">
                      <span>Seats ({heldSeats.length}):</span>
                      <span>₹{Number(totalAmount).toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-slate-400">
                      <span>Booking Fee:</span>
                      <span className="text-emerald-400 font-bold">FREE</span>
                    </div>
                  </div>

                  <div className="flex justify-between items-baseline pt-2">
                    <span className="text-xs font-bold text-slate-300">Amount Due:</span>
                    <span className="text-2xl font-black text-indigo-400">
                      ₹{Number(totalAmount).toFixed(2)}
                    </span>
                  </div>

                  <button
                    disabled={loading}
                    onClick={handleConfirmBooking}
                    className="w-full py-3.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-bold shadow-lg shadow-emerald-600/30 transition-all hover:scale-105 flex items-center justify-center space-x-2"
                  >
                    {loading ? (
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    ) : (
                      <>
                        <ShieldCheck className="w-4 h-4" />
                        <span>Pay & Confirm Tickets</span>
                      </>
                    )}
                  </button>

                  <p className="text-[10px] text-slate-500 text-center leading-relaxed">
                    By confirming, your booking reference and QR code ticket will be generated instantly.
                  </p>
                </div>
              </div>
            </div>
          </div>
        ) : (
          /* Confirmation & QR Ticket Card */
          <div className="glass-panel-glow p-8 rounded-3xl border border-emerald-500/30 text-center space-y-6 animate-fade-in">
            <div className="w-16 h-16 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-8 h-8" />
            </div>

            <div>
              <span className="text-xs font-bold uppercase tracking-widest text-emerald-400">
                Payment Successful
              </span>
              <h2 className="text-3xl font-extrabold text-white mt-1">
                Your Tickets are Confirmed!
              </h2>
              <p className="text-xs text-slate-400 mt-2">
                A confirmation receipt with QR ticket has been dispatched to{' '}
                <strong className="text-white">{bookingConfirmed.userEmail}</strong>.
              </p>
            </div>

            {/* Visual Digital Ticket */}
            <div className="max-w-md mx-auto bg-slate-900/90 rounded-2xl border border-slate-800 p-6 text-left space-y-4 shadow-2xl relative overflow-hidden">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="font-extrabold text-white text-base">
                    {bookingConfirmed.eventTitle}
                  </h3>
                  <p className="text-xs text-slate-400">{bookingConfirmed.venueName}</p>
                </div>
                <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  {bookingConfirmed.bookingReference}
                </span>
              </div>

              {/* QR Code Presentation */}
              <div className="flex flex-col items-center justify-center p-4 bg-white rounded-xl my-4">
                <img
                  src={bookingConfirmed.qrCodeBase64}
                  alt="Booking QR Code"
                  className="w-48 h-48 object-contain"
                />
                <span className="text-[10px] text-slate-800 font-mono font-bold mt-2">
                  {bookingConfirmed.bookingReference}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-4 text-xs pt-2 border-t border-slate-800">
                <div>
                  <span className="text-slate-500">Date & Time:</span>
                  <p className="font-bold text-white">
                    {new Date(bookingConfirmed.showStartTime).toLocaleString()}
                  </p>
                </div>
                <div>
                  <span className="text-slate-500">Seats:</span>
                  <p className="font-bold text-emerald-400">
                    {bookingConfirmed.seats?.map((s) => `${s.rowNumber}${s.seatNumber}`).join(', ')}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-3 pt-4">
              <Link
                to="/my-bookings"
                className="px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all hover:scale-105"
              >
                View in My Tickets
              </Link>
              <Link
                to="/"
                className="px-6 py-3 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-bold transition-colors"
              >
                Back to Home
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
