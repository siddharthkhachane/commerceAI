"use client";

import { FormEvent, useState } from "react";

import { AdminPageHeader } from "@/components/admin/AdminShared";
import { askBusinessAssistant } from "@/lib/admin";
import { formatPrice } from "@/lib/format";
import type { BusinessAssistantResponse } from "@/types/admin";

const starterQuestions = [
  "Why did sales decrease?",
  "How is inventory health?",
  "Give me a business summary for this week.",
];

function severityClass(severity: string) {
  switch (severity) {
    case "warning":
      return "border-amber-300 bg-amber-50 text-amber-800 dark:border-amber-800 dark:bg-amber-950/40 dark:text-amber-200";
    case "success":
      return "border-emerald-300 bg-emerald-50 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-200";
    default:
      return "border-border bg-muted/40 text-foreground";
  }
}

export function AdminBusinessAssistantContent() {
  const [question, setQuestion] = useState("Why did sales decrease?");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [report, setReport] = useState<BusinessAssistantResponse | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await askBusinessAssistant(question);
      setReport(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not generate report.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-6">
      <AdminPageHeader
        title="AI Business Assistant"
        description="Ask business questions and get reports grounded in orders, inventory, products, and analytics."
      />

      <div className="flex flex-wrap gap-2">
        {starterQuestions.map((prompt) => (
          <button
            key={prompt}
            type="button"
            onClick={() => setQuestion(prompt)}
            className="rounded-full border border-border bg-card px-3 py-1.5 text-xs font-medium text-muted-foreground transition hover:text-foreground"
          >
            {prompt}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-3 rounded-2xl border border-border bg-card p-5 sm:flex-row">
        <input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="Why did sales decrease?"
          className="h-11 flex-1 rounded-xl border border-input bg-background px-4 text-sm outline-none focus:border-ring focus:ring-2 focus:ring-ring/30"
          required
        />
        <button
          type="submit"
          disabled={loading}
          className="h-11 rounded-xl bg-foreground px-5 text-sm font-medium text-background disabled:opacity-60"
        >
          {loading ? "Analyzing..." : "Generate report"}
        </button>
      </form>

      {error ? (
        <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>
      ) : null}

      {report ? (
        <div className="space-y-6 animate-fade-in-up">
          <section className="rounded-2xl border border-border bg-card p-6 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
              Executive summary
            </p>
            <p className="mt-3 text-base leading-7 text-foreground">{report.summary}</p>
          </section>

          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <MetricCard label="Current revenue" value={formatPrice(report.snapshot.currentPeriodRevenue)} />
            <MetricCard
              label="Revenue change"
              value={`${report.snapshot.revenueChangePercent.toFixed(1)}%`}
            />
            <MetricCard label="Current orders" value={String(report.snapshot.currentPeriodOrders)} />
            <MetricCard label="Low stock SKUs" value={String(report.snapshot.lowStockProducts)} />
          </section>

          <section className="grid gap-4 lg:grid-cols-2">
            {report.insights.map((insight) => (
              <article
                key={insight.title}
                className={`rounded-2xl border p-5 ${severityClass(insight.severity)}`}
              >
                <h3 className="text-sm font-semibold">{insight.title}</h3>
                <p className="mt-2 text-sm leading-6 opacity-90">{insight.detail}</p>
              </article>
            ))}
          </section>

          <section className="rounded-2xl border border-border bg-card p-6 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
              Recommendations
            </p>
            <ul className="mt-4 space-y-2 text-sm text-foreground">
              {report.recommendations.map((item) => (
                <li key={item}>• {item}</li>
              ))}
            </ul>
          </section>
        </div>
      ) : null}
    </div>
  );
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
      <p className="text-sm text-muted-foreground">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-foreground">{value}</p>
    </div>
  );
}
