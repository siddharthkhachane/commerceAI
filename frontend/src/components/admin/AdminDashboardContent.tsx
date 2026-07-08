"use client";

import { useEffect, useState } from "react";

import { AdminDashboardCharts } from "@/components/admin/AdminDashboardCharts";
import { AdminPageHeader, AdminStatCard } from "@/components/admin/AdminShared";
import { getAdminAnalytics, getAdminMetrics } from "@/lib/admin";
import { formatPrice } from "@/lib/format";
import type { AdminAnalytics, AdminMetrics } from "@/types/admin";

export function AdminDashboardContent() {
  const [metrics, setMetrics] = useState<AdminMetrics | null>(null);
  const [analytics, setAnalytics] = useState<AdminAnalytics | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        const [metricsData, analyticsData] = await Promise.all([
          getAdminMetrics(),
          getAdminAnalytics(30),
        ]);
        setMetrics(metricsData);
        setAnalytics(analyticsData);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load admin dashboard.");
      }
    }

    load();
  }, []);

  if (error) {
    return (
      <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-800 dark:bg-rose-950/50 dark:text-rose-300">
        {error}
      </p>
    );
  }

  if (!metrics || !analytics) {
    return <p className="text-sm text-muted-foreground">Loading dashboard...</p>;
  }

  return (
    <div className="space-y-8">
      <AdminPageHeader
        title="Business overview"
        description="Track revenue, orders, catalog health, and inventory performance in one place."
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <AdminStatCard label="Revenue" value={formatPrice(metrics.totalRevenue)} hint="Confirmed orders" />
        <AdminStatCard label="Orders" value={String(metrics.totalOrders)} hint={`${metrics.confirmedOrders} confirmed`} />
        <AdminStatCard label="Products" value={String(metrics.totalProducts)} hint={`${metrics.activeProducts} active`} />
        <AdminStatCard label="Inventory units" value={String(metrics.inventoryUnits)} hint={`${metrics.lowStockProducts} low stock`} />
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <AdminStatCard label="Customers" value={String(metrics.totalCustomers)} />
        <AdminStatCard label="Low stock alerts" value={String(metrics.lowStockProducts)} hint="Below threshold" />
      </div>

      <AdminDashboardCharts analytics={analytics} />
    </div>
  );
}
