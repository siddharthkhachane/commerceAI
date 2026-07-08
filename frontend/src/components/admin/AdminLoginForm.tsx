"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { AuthField } from "@/components/auth/AuthField";
import { ApiRequestError } from "@/lib/api";
import * as authApi from "@/lib/auth";
import { clearAuthToken, setAuthToken } from "@/lib/auth-storage";

export function AdminLoginForm() {
  const { logout } = useAuth();
  const [email, setEmail] = useState("admin@commerceai.com");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      const response = await authApi.login({ email, password });
      if (response.user.role !== "ADMIN") {
        clearAuthToken();
        logout();
        setError("This account does not have admin access.");
        return;
      }

      setAuthToken(response.token);
      window.location.href = "/admin";
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError("Unable to sign in. Please try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/30 px-4">
      <div className="w-full max-w-md rounded-3xl border border-border bg-card p-8 shadow-sm">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
          CommerceAI Admin
        </p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
          Admin sign in
        </h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Access revenue, orders, products, inventory, and analytics.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <AuthField
            id="admin-email"
            label="Email"
            type="email"
            value={email}
            onChange={setEmail}
            autoComplete="email"
          />
          <AuthField
            id="admin-password"
            label="Password"
            type="password"
            value={password}
            onChange={setPassword}
            autoComplete="current-password"
          />

          {error ? (
            <p className="rounded-xl border border-rose-300 bg-rose-50 px-3 py-2 text-sm text-rose-700 dark:border-rose-800 dark:bg-rose-950/50 dark:text-rose-300">
              {error}
            </p>
          ) : null}

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-xl bg-foreground px-4 py-2.5 text-sm font-medium text-background transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? "Signing in..." : "Sign in to admin"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-muted-foreground">
          <Link href="/" className="font-medium text-foreground hover:underline">
            Back to storefront
          </Link>
        </p>
      </div>
    </div>
  );
}
