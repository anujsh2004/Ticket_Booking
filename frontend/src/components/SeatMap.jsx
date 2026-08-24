import React from 'react';
import { Armchair, CheckCircle2, Lock, Clock, Info } from 'lucide-react';

export default function SeatMap({ seats, selectedSeatIds, onToggleSeat, currentUserId }) {
  // Group seats by row
  const rows = React.useMemo(() => {
    const map = {};
    seats.forEach((seat) => {
      if (!map[seat.rowNumber]) {
        map[seat.rowNumber] = [];
      }
      map[seat.rowNumber].push(seat);
    });
    // Sort seats in each row by seatNumber
    Object.keys(map).forEach((r) => {
      map[r].sort((a, b) => a.seatNumber - b.seatNumber);
    });
    return map;
  }, [seats]);

  const sortedRowKeys = Object.keys(rows).sort();

  const getSeatColor = (seat) => {
    const isSelected = selectedSeatIds.includes(seat.id);

    if (isSelected) {
      return 'bg-indigo-600 border-indigo-400 text-white shadow-lg shadow-indigo-500/50 scale-110 z-10';
    }

    if (seat.status === 'BOOKED') {
      return 'bg-slate-800/80 border-slate-700 text-slate-500 cursor-not-allowed opacity-50';
    }

    if (seat.status === 'HELD') {
      if (seat.isHeldByMe) {
        return 'bg-amber-500/30 border-amber-400 text-amber-300 ring-2 ring-amber-400/50 animate-pulse';
      }
      return 'bg-amber-950/60 border-amber-800/50 text-amber-500/60 cursor-not-allowed';
    }

    // Available seat styles by category
    switch (seat.categoryName?.toUpperCase()) {
      case 'VIP':
        return 'bg-purple-950/40 border-purple-500/50 text-purple-300 hover:bg-purple-600 hover:text-white hover:scale-105';
      case 'PREMIUM':
        return 'bg-blue-950/40 border-blue-500/50 text-blue-300 hover:bg-blue-600 hover:text-white hover:scale-105';
      default:
        return 'bg-emerald-950/40 border-emerald-500/50 text-emerald-300 hover:bg-emerald-600 hover:text-white hover:scale-105';
    }
  };

  return (
    <div className="flex flex-col items-center w-full max-w-4xl mx-auto select-none">
      {/* Cinema Screen Curve */}
      <div className="w-full max-w-2xl mb-12 flex flex-col items-center">
        <div className="cinema-screen w-full mb-3"></div>
        <span className="text-xs font-semibold uppercase tracking-widest text-slate-400">
          Stage / Screen
        </span>
      </div>

      {/* Seating Grid */}
      <div className="flex flex-col space-y-4 w-full items-center">
        {sortedRowKeys.map((rowKey) => (
          <div key={rowKey} className="flex items-center space-x-3 sm:space-x-4">
            {/* Row Identifier Left */}
            <span className="w-6 text-center font-bold text-xs text-slate-400">
              {rowKey}
            </span>

            {/* Row Seats */}
            <div className="flex items-center space-x-2 sm:space-x-3">
              {rows[rowKey].map((seat) => {
                const isClickable =
                  seat.status === 'AVAILABLE' ||
                  (seat.status === 'HELD' && seat.isHeldByMe) ||
                  selectedSeatIds.includes(seat.id);

                return (
                  <button
                    key={seat.id}
                    type="button"
                    disabled={!isClickable}
                    onClick={() => onToggleSeat(seat)}
                    title={`Row ${seat.rowNumber}, Seat ${seat.seatNumber} (${seat.categoryName}) - $${seat.price} [${seat.status}]`}
                    className={`relative w-8 h-8 sm:w-9 sm:h-9 rounded-lg border flex flex-col items-center justify-center text-[10px] font-bold transition-all duration-150 ${getSeatColor(
                      seat
                    )}`}
                  >
                    <span>{seat.seatNumber}</span>
                  </button>
                );
              })}
            </div>

            {/* Row Identifier Right */}
            <span className="w-6 text-center font-bold text-xs text-slate-400">
              {rowKey}
            </span>
          </div>
        ))}
      </div>

      {/* Legend & Categories */}
      <div className="mt-12 w-full p-4 rounded-xl glass-panel flex flex-wrap items-center justify-around gap-4 text-xs font-medium text-slate-300">
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 rounded border border-emerald-500/50 bg-emerald-950/40"></div>
          <span>Available</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 rounded border border-indigo-400 bg-indigo-600 text-white flex items-center justify-center text-[8px]">✓</div>
          <span>Selected</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 rounded border border-amber-400 bg-amber-500/30"></div>
          <span>Held (TTL)</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 rounded border border-slate-700 bg-slate-800/80 opacity-50"></div>
          <span>Booked</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 rounded border border-purple-500/50 bg-purple-950/40"></div>
          <span>VIP Tier</span>
        </div>
      </div>
    </div>
  );
}
