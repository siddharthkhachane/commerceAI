"use client";

import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import type { AdminAnalytics } from "@/types/admin";

function formatShortDate(value: string) {
  const date = new Date(`${value}T00:00:00`);
  return date.toLocaleDateString(undefined, { month: "short", day: "numeric" });
}

export function AdminDashboardCharts({ analytics }: { analytics: AdminAnalytics }) {
  const revenueData = analytics.revenueSeries.map((point) => ({
    date: formatShortDate(point.date),
    revenue: Number(point.value),
  }));

  const ordersData = analytics.ordersSeries.map((point) => ({
    date: formatShortDate(point.date),
    orders: point.count,
  }));

  const categoryData = analytics.categoryBreakdown.map((item) => ({
    name: item.categoryName,
    units: item.inventoryUnits,
  }));

  const topProductsData = analytics.topProducts.map((item) => ({
    name: item.productName.length > 18 ? `${item.productName.slice(0, 18)}...` : item.productName,
    revenue: Number(item.revenue),
  }));

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <ChartCard title="Revenue trend" subtitle="Confirmed order revenue over time">
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={revenueData}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip />
            <Line type="monotone" dataKey="revenue" stroke="var(--foreground)" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </ChartCard>

      <ChartCard title="Orders trend" subtitle="Daily confirmed orders">
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={ordersData}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
            <Tooltip />
            <Bar dataKey="orders" fill="var(--foreground)" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>

      <ChartCard title="Top products" subtitle="Revenue leaders in selected period">
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={topProductsData} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
            <XAxis type="number" tick={{ fontSize: 12 }} />
            <YAxis type="category" dataKey="name" width={110} tick={{ fontSize: 12 }} />
            <Tooltip />
            <Bar dataKey="revenue" fill="var(--foreground)" radius={[0, 8, 8, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>

      <ChartCard title="Inventory by category" subtitle="Units on hand across collections">
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={categoryData}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
            <XAxis dataKey="name" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
            <Tooltip />
            <Bar dataKey="units" fill="var(--foreground)" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>
    </div>
  );
}

function ChartCard({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <section className="rounded-2xl border border-border bg-card p-5 shadow-sm">
      <div className="mb-4">
        <h3 className="text-lg font-semibold text-foreground">{title}</h3>
        <p className="text-sm text-muted-foreground">{subtitle}</p>
      </div>
      {children}
    </section>
  );
}
