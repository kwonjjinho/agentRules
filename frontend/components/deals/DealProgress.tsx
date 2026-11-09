'use client';

interface DealProgressProps {
  currentQuantity: number;
  targetQuantity: number;
  maxCapacity: number;
  achievementRate: number;
  isAchieved: boolean;
}

export function DealProgress({
  currentQuantity,
  targetQuantity,
  maxCapacity,
  achievementRate,
  isAchieved,
}: DealProgressProps) {
  const percentage = Math.min(Math.round(achievementRate), 100);
  const remaining = Math.max(maxCapacity - currentQuantity, 0);

  return (
    <div className="w-full space-y-2">
      <div className="flex items-center justify-between text-sm font-semibold">
        <span className="flex items-center gap-1.5 text-indigo-600">
          달성률 {achievementRate.toFixed(1)}%
          {isAchieved && (
            <span className="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-600/20">
              공구 성사 확정!
            </span>
          )}
        </span>
        <span className="text-gray-500">
          <strong className="text-gray-900">{currentQuantity}</strong> / {targetQuantity}개 (잔여 {remaining}개)
        </span>
      </div>

      <div className="relative h-2.5 w-full overflow-hidden rounded-full bg-gray-100">
        <div
          className={`h-full rounded-full transition-all duration-500 ${
            isAchieved ? 'bg-gradient-to-r from-indigo-500 to-emerald-500' : 'bg-indigo-600'
          }`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
}
