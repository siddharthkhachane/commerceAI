"use client";

import { DashboardShell, ProtectedRoute } from "@/components/auth/ProtectedRoute";

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <DashboardShell>
        <p className="text-sm leading-7 text-zinc-600">
          Your account is ready. Product browsing, cart, and AI shopping
          assistant features will appear in upcoming phases.
        </p>
      </DashboardShell>
    </ProtectedRoute>
  );
}
