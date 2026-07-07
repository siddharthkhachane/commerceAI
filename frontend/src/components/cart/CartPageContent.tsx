"use client";

import Image from "next/image";
import Link from "next/link";
import { useState } from "react";

import { useCart } from "@/components/cart/CartProvider";
import { formatPrice } from "@/lib/format";
import { ApiRequestError } from "@/lib/api";

export function CartPageContent() {
  const { cart, updateItem, removeItem, isLoading } = useCart();
  const [error, setError] = useState<string | null>(null);
  const [pendingProductId, setPendingProductId] = useState<number | null>(null);

  async function handleQuantityChange(productId: number, quantity: number) {
    setError(null);
    setPendingProductId(productId);
    try {
      await updateItem(productId, quantity);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : "Could not update cart.");
    } finally {
      setPendingProductId(null);
    }
  }

  async function handleRemove(productId: number) {
    setError(null);
    setPendingProductId(productId);
    try {
      await removeItem(productId);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : "Could not remove item.");
    } finally {
      setPendingProductId(null);
    }
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-4xl px-6 py-16 text-center text-sm text-muted-foreground">
        Loading cart…
      </div>
    );
  }

  if (cart.items.length === 0) {
    return (
      <div className="mx-auto max-w-4xl px-6 py-16 text-center">
        <h1 className="text-3xl font-semibold tracking-tight text-foreground">Your cart</h1>
        <p className="mt-4 text-sm text-muted-foreground">Your cart is empty.</p>
        <Link
          href="/products"
          className="mt-8 inline-flex rounded-full bg-accent text-accent-foreground"
        >
          Continue shopping
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl px-6 py-12 md:py-16">
      <h1 className="text-3xl font-semibold tracking-tight text-foreground">Your cart</h1>
      <p className="mt-2 text-sm text-muted-foreground">{cart.itemCount} items</p>

      <div className="mt-10 space-y-6">
        {cart.items.map((item) => (
          <div
            key={item.productId}
            className="flex flex-col gap-4 rounded-2xl border border-border bg-card p-5 sm:flex-row sm:items-center"
          >
            <div className="relative h-28 w-24 overflow-hidden rounded-xl bg-muted">
              <Image
                src={item.imageUrl}
                alt={item.productName}
                fill
                className="object-cover"
              />
            </div>

            <div className="flex-1">
              <Link
                href={`/products/${item.productSlug}`}
                className="text-base font-medium text-foreground hover:text-muted-foreground"
              >
                {item.productName}
              </Link>
              <p className="mt-1 text-sm text-muted-foreground">{formatPrice(item.unitPrice)}</p>
            </div>

            <div className="flex items-center gap-3">
              <select
                value={item.quantity}
                disabled={pendingProductId === item.productId}
                onChange={(event) =>
                  handleQuantityChange(item.productId, Number(event.target.value))
                }
                className="rounded-lg border border-border px-3 py-2 text-sm"
              >
                {Array.from(
                  { length: Math.min(item.stockQuantity, 10) },
                  (_, index) => index + 1,
                ).map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))}
              </select>
              <button
                type="button"
                onClick={() => handleRemove(item.productId)}
                disabled={pendingProductId === item.productId}
                className="text-sm font-medium text-muted-foreground hover:text-foreground"
              >
                Remove
              </button>
            </div>

            <p className="text-sm font-medium text-foreground">
              {formatPrice(item.lineTotal)}
            </p>
          </div>
        ))}
      </div>

      {error && (
        <p className="mt-6 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <div className="mt-10 flex flex-col items-end gap-4 border-t border-zinc-200 pt-8">
        <p className="text-lg font-medium text-foreground">
          Subtotal: {formatPrice(cart.subtotal)}
        </p>
        <Link
          href="/checkout"
          className="rounded-full bg-accent text-accent-foreground transition hover:opacity-90"
        >
          Proceed to checkout
        </Link>
      </div>
    </div>
  );
}
