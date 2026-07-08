"use client";

import Link from "next/link";
import { FormEvent, useMemo, useState } from "react";

import { getShoppingRecommendations } from "@/lib/assistant";
import { formatPrice } from "@/lib/format";
import type { ShoppingAssistantResponse } from "@/types/assistant";

export function ShoppingAssistantPanel() {
  const [message, setMessage] = useState("I'm going skiing. Budget $300.");
  const [budget, setBudget] = useState("300");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ShoppingAssistantResponse | null>(null);

  const parsedBudget = useMemo(() => {
    const raw = budget.trim();
    if (!raw) return undefined;
    const value = Number(raw);
    return Number.isFinite(value) && value > 0 ? value : undefined;
  }, [budget]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await getShoppingRecommendations({
        message,
        budget: parsedBudget,
      });
      setResult(response);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Could not get recommendations right now.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 md:py-20">
      <div className="overflow-hidden rounded-3xl border border-border bg-card p-6 shadow-sm sm:p-8">
        <div className="mb-6">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
            AI Shopping Assistant
          </p>
          <h2 className="mt-2 text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">
            Describe your plan, get a smart starter kit
          </h2>
          <p className="mt-3 max-w-2xl text-sm text-muted-foreground sm:text-base">
            Example: &quot;I&apos;m going skiing. Budget $300.&quot; We&apos;ll
            match products and explain each recommendation.
          </p>
        </div>

        <form onSubmit={onSubmit} className="grid gap-3 sm:grid-cols-[1fr_170px_auto]">
          <input
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            placeholder="I need a beach vacation outfit under $200"
            className="h-11 rounded-xl border border-input bg-background px-4 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/30"
            required
          />
          <input
            value={budget}
            onChange={(event) => setBudget(event.target.value)}
            placeholder="Budget (optional)"
            inputMode="decimal"
            className="h-11 rounded-xl border border-input bg-background px-4 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/30"
          />
          <button
            type="submit"
            disabled={loading}
            className="h-11 rounded-xl bg-foreground px-5 text-sm font-medium text-background transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? "Thinking..." : "Get picks"}
          </button>
        </form>

        {error ? (
          <p className="mt-4 rounded-xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-800 dark:bg-rose-950/50 dark:text-rose-300">
            {error}
          </p>
        ) : null}

        {result ? (
          <div className="mt-8 grid gap-4 md:grid-cols-3">
            {result.recommendations.length === 0 ? (
              <div className="md:col-span-3 rounded-2xl border border-dashed border-border bg-muted/40 px-5 py-8 text-sm text-muted-foreground">
                No matching products yet. Try a different activity, style, or budget.
              </div>
            ) : null}
            {result.recommendations.map((item) => (
              <article
                key={item.product.id}
                className="rounded-2xl border border-border bg-background/90 p-4"
              >
                <p className="text-xs uppercase tracking-[0.14em] text-muted-foreground">
                  {item.product.category.name}
                </p>
                <h3 className="mt-2 text-base font-semibold text-foreground">
                  {item.product.name}
                </h3>
                <p className="mt-1 text-sm font-medium text-foreground">
                  {formatPrice(item.product.price)}
                </p>
                <p className="mt-3 text-sm text-muted-foreground">{item.reason}</p>
                <Link
                  href={`/products/${item.product.slug}`}
                  className="mt-4 inline-flex text-sm font-medium text-foreground underline-offset-4 hover:underline"
                >
                  View product
                </Link>
              </article>
            ))}
          </div>
        ) : null}
      </div>
    </section>
  );
}
