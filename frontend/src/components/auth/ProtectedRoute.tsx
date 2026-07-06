"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { useAuth } from "@/components/auth/AuthProvider";

type ProtectedRouteProps = {
  children: React.ReactNode;
  requiredRole?: "ADMIN";
};

export function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const router = useRouter();
  const { user, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading && !user) {
      router.replace("/login");
    }

    if (!isLoading && user && requiredRole && user.role !== requiredRole) {
      router.replace("/dashboard");
    }
  }, [isLoading, user, requiredRole, router]);

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <p className="text-sm text-zinc-500">Loading…</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  if (requiredRole && user.role !== requiredRole) {
    return null;
  }

  return children;
}

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { user, logout } = useAuth();

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <div className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-8 px-6 py-16">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
            CommerceAI
          </p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-zinc-950">
            Dashboard
          </h1>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 transition hover:bg-zinc-50"
        >
          Sign out
        </button>
      </div>

      <div className="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
        <p className="text-sm text-zinc-600">Signed in as</p>
        <p className="mt-1 text-lg font-medium text-zinc-950">{user?.fullName}</p>
        <p className="text-sm text-zinc-600">{user?.email}</p>
        <span className="mt-4 inline-flex rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-700 ring-1 ring-zinc-200">
          {user?.role}
        </span>
      </div>

      {children}

      <div className="text-sm text-zinc-600">
        <Link href="/" className="font-medium text-zinc-950 hover:underline">
          Back to home
        </Link>
        {user?.role === "ADMIN" && (
          <>
            {" · "}
            <Link href="/admin" className="font-medium text-zinc-950 hover:underline">
              Admin panel
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
