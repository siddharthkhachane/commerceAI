"use client";

import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/components/auth/AuthProvider";
import Link from "next/link";

export default function AdminPage() {
  const { user } = useAuth();

  return (
    <ProtectedRoute requiredRole="ADMIN">
      <div className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-8 px-6 py-16">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
            CommerceAI Admin
          </p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-zinc-950">
            Admin panel
          </h1>
        </div>

        <div className="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
          <p className="text-sm text-zinc-600">Welcome, {user?.fullName}</p>
          <p className="mt-4 text-sm leading-7 text-zinc-600">
            Admin tools for products, orders, and analytics will be added in
            later phases.
          </p>
        </div>

        <Link href="/dashboard" className="text-sm font-medium text-zinc-950 hover:underline">
          Back to dashboard
        </Link>
      </div>
    </ProtectedRoute>
  );
}
