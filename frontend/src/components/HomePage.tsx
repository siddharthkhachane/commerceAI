"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { StatusBadge } from "@/components/StatusBadge";
import { getHealth } from "@/lib/health";
import type { HealthResponse } from "@/types";

export function HomePage() {
  const { user } = useAuth();
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getHealth()
      .then(setHealth)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <div className="flex flex-1 flex-col items-center justify-center px-6 py-24">
      <div className="w-full max-w-2xl space-y-10 text-center">
        <div className="space-y-4">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
            CommerceAI
          </p>
          <h1 className="text-4xl font-semibold tracking-tight text-zinc-950 sm:text-5xl">
            AI-native commerce, built for clarity.
          </h1>
          <p className="mx-auto max-w-xl text-lg leading-8 text-zinc-600">
            A premium shopping experience powered by conversational AI — for
            customers and the teams running the business.
          </p>
        </div>

        <div className="flex flex-col items-center gap-4">
          <StatusBadge health={health} error={error} />
          <div className="flex items-center gap-3 text-sm font-medium">
            {user ? (
              <Link
                href="/dashboard"
                className="rounded-full bg-zinc-950 px-5 py-2.5 text-white transition hover:bg-zinc-800"
              >
                Go to dashboard
              </Link>
            ) : (
              <>
                <Link
                  href="/login"
                  className="rounded-full bg-zinc-950 px-5 py-2.5 text-white transition hover:bg-zinc-800"
                >
                  Sign in
                </Link>
                <Link
                  href="/register"
                  className="rounded-full border border-zinc-300 px-5 py-2.5 text-zinc-700 transition hover:bg-zinc-50"
                >
                  Create account
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}