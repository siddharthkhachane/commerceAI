"use client";

import Image from "next/image";
import Link from "next/link";
import { useState } from "react";

import { useCompare } from "@/components/compare/CompareProvider";
import { compareProducts } from "@/lib/assistant";
import { formatPrice } from "@/lib/format";
import type { ProductCompareAssistantResponse } from "@/types/assistant";

function ScoreBar({ label, score }: { label: string; score: number }) {
  return (
    <div>
      <div className="mb-1 flex items-center justify-between text-xs text-muted-foreground">
        <span>{label}</span>
        <span>{score}/10</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-muted">
        <div
          className="h-full rounded-full bg-foreground transition-all"
          style={{ width: `${score * 10}%` }}
        />
      </div>
    </div>
  );
}

export function ComparePageContent() {
  const { items, removeItem, clear } = useCompare();
  const [question, setQuestion] = useState("Which one is better?");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ProductCompareAssistantResponse | null>(null);

  async function handleCompare() {
    if (items.length < 2) return;

    setLoading(true);
    setError(null);

    try {
      const response = await compareProducts({
        productIds: items.map((item) => item.id),
        question,
      });
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not compare products right now.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 md:py-16">
      <div className="mb-10 animate-fade-in-up space-y-4">
        <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
          AI Compare
        </p>
        <h1 className="text-4xl font-semibold tracking-tight text-foreground sm:text-5xl">
          Compare products with AI
        </h1>
        <p className="max-w-2xl text-sm leading-7 text-muted-foreground">
          Select 2 to 4 products, ask which is better, and get a breakdown across quality,
          material, price, style, pros, cons, and a final recommendation.
        </p>
      </div>

      {items.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-border bg-card px-6 py-16 text-center">
          <p className="text-base font-medium text-foreground">No products selected yet</p>
          <p className="mt-2 text-sm text-muted-foreground">
            Browse the shop and use &quot;Add to compare&quot; on product cards or detail pages.
          </p>
          <Link
            href="/products"
            className="mt-6 inline-flex rounded-full bg-foreground px-5 py-2.5 text-sm font-medium text-background transition hover:opacity-90"
          >
            Browse products
          </Link>
        </div>
      ) : (
        <>
          <div className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {items.map((item) => (
              <article
                key={item.id}
                className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm"
              >
                <div className="relative aspect-[4/5] bg-muted">
                  <Image
                    src={item.imageUrl}
                    alt={item.name}
                    fill
                    sizes="(max-width: 1024px) 50vw, 25vw"
                    className="object-cover"
                  />
                </div>
                <div className="space-y-2 p-4">
                  <p className="text-xs uppercase tracking-[0.14em] text-muted-foreground">
                    {item.category.name}
                  </p>
                  <h2 className="line-clamp-2 text-sm font-semibold text-foreground">
                    {item.name}
                  </h2>
                  <p className="text-sm font-medium">{formatPrice(item.price)}</p>
                  <div className="flex gap-2 pt-1">
                    <Link
                      href={`/products/${item.slug}`}
                      className="text-xs font-medium text-foreground underline-offset-4 hover:underline"
                    >
                      View
                    </Link>
                    <button
                      type="button"
                      onClick={() => removeItem(item.id)}
                      className="text-xs font-medium text-muted-foreground hover:text-foreground"
                    >
                      Remove
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>

          <div className="mb-8 flex flex-col gap-3 rounded-2xl border border-border bg-card p-5 sm:flex-row sm:items-center">
            <input
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Which one is better?"
              className="h-11 flex-1 rounded-xl border border-input bg-background px-4 text-sm outline-none focus:border-ring focus:ring-2 focus:ring-ring/30"
            />
            <div className="flex gap-2">
              <button
                type="button"
                onClick={handleCompare}
                disabled={loading || items.length < 2}
                className="h-11 rounded-xl bg-foreground px-5 text-sm font-medium text-background transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {loading ? "Comparing..." : "Compare with AI"}
              </button>
              <button
                type="button"
                onClick={() => {
                  clear();
                  setResult(null);
                }}
                className="h-11 rounded-xl border border-border px-4 text-sm font-medium text-foreground transition hover:bg-card-hover"
              >
                Clear
              </button>
            </div>
          </div>

          {items.length < 2 ? (
            <p className="mb-8 text-sm text-muted-foreground">
              Add at least one more product to start comparing.
            </p>
          ) : null}

          {error ? (
            <p className="mb-8 rounded-xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-800 dark:bg-rose-950/50 dark:text-rose-300">
              {error}
            </p>
          ) : null}

          {result ? (
            <div className="space-y-8 animate-fade-in-up">
              <div className="rounded-2xl border border-border bg-muted/40 p-6">
                <p className="text-sm font-medium uppercase tracking-[0.14em] text-muted-foreground">
                  Recommendation
                </p>
                <p className="mt-3 text-base leading-7 text-foreground">{result.recommendation}</p>
              </div>

              <div className="grid gap-6 lg:grid-cols-2">
                {result.comparisons.map((comparison) => {
                  const isWinner = comparison.product.id === result.recommendedProductId;
                  return (
                    <article
                      key={comparison.product.id}
                      className={`rounded-2xl border bg-card p-6 ${
                        isWinner ? "border-foreground/30 shadow-sm" : "border-border"
                      }`}
                    >
                      <div className="mb-4 flex items-start justify-between gap-3">
                        <div>
                          {isWinner ? (
                            <span className="mb-2 inline-flex rounded-full bg-foreground px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.12em] text-background">
                              Top pick
                            </span>
                          ) : null}
                          <h3 className="text-lg font-semibold text-foreground">
                            {comparison.product.name}
                          </h3>
                          <p className="mt-1 text-sm text-muted-foreground">
                            Material: {comparison.material}
                          </p>
                        </div>
                        <p className="text-sm font-medium">{formatPrice(comparison.product.price)}</p>
                      </div>

                      <div className="space-y-3">
                        <ScoreBar label="Quality" score={comparison.qualityScore} />
                        <ScoreBar label="Price value" score={comparison.priceScore} />
                        <ScoreBar label="Style" score={comparison.styleScore} />
                      </div>

                      <div className="mt-5 grid gap-4 sm:grid-cols-2">
                        <div>
                          <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
                            Pros
                          </p>
                          <ul className="mt-2 space-y-1 text-sm text-foreground">
                            {comparison.pros.map((pro) => (
                              <li key={pro}>+ {pro}</li>
                            ))}
                          </ul>
                        </div>
                        <div>
                          <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
                            Cons
                          </p>
                          <ul className="mt-2 space-y-1 text-sm text-muted-foreground">
                            {comparison.cons.map((con) => (
                              <li key={con}>- {con}</li>
                            ))}
                          </ul>
                        </div>
                      </div>
                    </article>
                  );
                })}
              </div>
            </div>
          ) : null}
        </>
      )}
    </div>
  );
}
