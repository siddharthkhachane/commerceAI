"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";

import { formatPrice } from "@/lib/format";
import * as ordersApi from "@/lib/orders";
import type { Order } from "@/types/cart";

type OrderConfirmationProps = {
  orderId: number;
};

export function OrderConfirmation({ orderId }: OrderConfirmationProps) {
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    ordersApi
      .getOrder(orderId)
      .then(setOrder)
      .catch(() => setError("Could not load order details."));
  }, [orderId]);

  if (error) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-16 text-center text-sm text-red-600">
        {error}
      </div>
    );
  }

  if (!order) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-16 text-center text-sm text-zinc-500">
        Loading order…
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12 md:py-16">
      <p className="text-sm font-medium uppercase tracking-[0.2em] text-emerald-600">
        Order confirmed
      </p>
      <h1 className="mt-3 text-3xl font-semibold tracking-tight text-zinc-950">
        Thank you, {order.shippingName}
      </h1>
      <p className="mt-3 text-sm text-zinc-600">
        Order #{order.id} · {formatPrice(order.totalAmount)}
      </p>

      <div className="mt-10 space-y-4 rounded-2xl border border-zinc-200 bg-white p-6">
        {order.items.map((item) => (
          <div key={item.productId} className="flex items-center gap-4">
            <div className="relative h-16 w-14 overflow-hidden rounded-lg bg-zinc-100">
              <Image src={item.imageUrl} alt={item.productName} fill className="object-cover" />
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-zinc-950">{item.productName}</p>
              <p className="text-xs text-zinc-600">
                Qty {item.quantity} · {formatPrice(item.unitPrice)}
              </p>
            </div>
            <p className="text-sm font-medium text-zinc-950">
              {formatPrice(item.lineTotal)}
            </p>
          </div>
        ))}
      </div>

      <div className="mt-8 rounded-2xl border border-zinc-200 bg-zinc-50 p-6 text-sm text-zinc-600">
        <p className="font-medium text-zinc-950">Shipping to</p>
        <p className="mt-2">
          {order.shippingAddress}
          <br />
          {order.shippingCity}, {order.shippingState} {order.shippingPostalCode}
        </p>
      </div>

      <div className="mt-8 flex gap-4">
        <Link
          href="/products"
          className="rounded-full bg-zinc-950 px-6 py-3 text-sm font-medium text-white"
        >
          Continue shopping
        </Link>
        <Link
          href="/orders"
          className="rounded-full border border-zinc-300 px-6 py-3 text-sm font-medium text-zinc-700"
        >
          View orders
        </Link>
      </div>
    </div>
  );
}
