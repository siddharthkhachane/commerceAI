"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { formatPrice } from "@/lib/format";
import * as ordersApi from "@/lib/orders";
import type { Order } from "@/types/cart";

export default function OrdersPage() {
  return (
    <ProtectedRoute>
      <OrdersPageContent />
    </ProtectedRoute>
  );
}

function OrdersPageContent() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    ordersApi
      .getOrders()
      .then(setOrders)
      .catch(() => setError("Could not load orders."));
  }, []);

  return (
    <div className="mx-auto max-w-3xl px-6 py-12 md:py-16">
      <h1 className="text-3xl font-semibold tracking-tight text-zinc-950">Your orders</h1>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {!error && orders.length === 0 && (
        <p className="mt-4 text-sm text-zinc-600">No orders yet.</p>
      )}

      <div className="mt-8 space-y-4">
        {orders.map((order) => (
          <Link
            key={order.id}
            href={`/orders/${order.id}`}
            className="block rounded-2xl border border-zinc-200 bg-white p-5 transition hover:border-zinc-300"
          >
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-zinc-950">Order #{order.id}</p>
                <p className="mt-1 text-xs text-zinc-600">
                  {new Date(order.createdAt).toLocaleString()}
                </p>
              </div>
              <p className="text-sm font-medium text-zinc-950">
                {formatPrice(order.totalAmount)}
              </p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
