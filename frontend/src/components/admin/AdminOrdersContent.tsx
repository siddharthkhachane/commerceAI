"use client";

import { useEffect, useState } from "react";

import { AdminPageHeader } from "@/components/admin/AdminShared";
import { getAdminOrders } from "@/lib/admin";
import { formatPrice } from "@/lib/format";
import type { AdminOrderSummary } from "@/types/admin";

export function AdminOrdersContent() {
  const [orders, setOrders] = useState<AdminOrderSummary[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getAdminOrders()
      .then((response) => setOrders(response.content))
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load orders."));
  }, []);

  return (
    <div>
      <AdminPageHeader
        title="Orders"
        description="Review customer purchases, order totals, and fulfillment status."
      />

      {error ? (
        <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>
      ) : (
        <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
          <table className="min-w-full text-left text-sm">
            <thead className="border-b border-border bg-muted/40 text-muted-foreground">
              <tr>
                <th className="px-4 py-3 font-medium">Order</th>
                <th className="px-4 py-3 font-medium">Customer</th>
                <th className="px-4 py-3 font-medium">Items</th>
                <th className="px-4 py-3 font-medium">Total</th>
                <th className="px-4 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-muted-foreground">
                    No orders yet.
                  </td>
                </tr>
              ) : (
                orders.map((order) => (
                  <tr key={order.id} className="border-b border-border/70 last:border-0">
                    <td className="px-4 py-4">
                      <p className="font-medium text-foreground">#{order.id}</p>
                      <p className="text-xs text-muted-foreground">
                        {new Date(order.createdAt).toLocaleString()}
                      </p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="font-medium text-foreground">{order.customerName}</p>
                      <p className="text-xs text-muted-foreground">{order.customerEmail}</p>
                    </td>
                    <td className="px-4 py-4 text-foreground">{order.itemCount}</td>
                    <td className="px-4 py-4 font-medium text-foreground">{formatPrice(order.totalAmount)}</td>
                    <td className="px-4 py-4">
                      <span className="rounded-full bg-muted px-2.5 py-1 text-xs font-medium text-foreground">
                        {order.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
