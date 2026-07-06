import type { HealthResponse } from "@/types";

type StatusBadgeProps = {
  health: HealthResponse | null;
  error: string | null;
};

export function StatusBadge({ health, error }: StatusBadgeProps) {
  if (error) {
    return (
      <span className="inline-flex items-center rounded-full bg-red-50 px-3 py-1 text-sm font-medium text-red-700 ring-1 ring-red-200">
        Backend unreachable
      </span>
    );
  }

  if (!health) {
    return (
      <span className="inline-flex items-center rounded-full bg-zinc-100 px-3 py-1 text-sm font-medium text-zinc-600 ring-1 ring-zinc-200">
        Checking connection…
      </span>
    );
  }

  return (
    <span className="inline-flex items-center rounded-full bg-emerald-50 px-3 py-1 text-sm font-medium text-emerald-700 ring-1 ring-emerald-200">
      {health.service} · {health.status}
    </span>
  );
}
