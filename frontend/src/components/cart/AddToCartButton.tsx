"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { useCart } from "@/components/cart/CartProvider";
import { ApiRequestError } from "@/lib/api";
import type { ProductDetail } from "@/types/catalog";

type AddToCartButtonProps = {
  product: ProductDetail;
};

export function AddToCartButton({ product }: AddToCartButtonProps) {
  const router = useRouter();
  const { user } = useAuth();
  const { addItem } = useCart();
  const [quantity, setQuantity] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const inStock = product.stockQuantity > 0;
  const maxQuantity = Math.max(product.stockQuantity, 1);

  async function handleAddToCart() {
    setError(null);
    setMessage(null);

    if (!user) {
      router.push(`/login?from=/products/${product.slug}`);
      return;
    }

    setIsSubmitting(true);
    try {
      await addItem(product.id, quantity);
      setMessage("Added to cart");
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError("Could not add item to cart.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mt-8 space-y-4">
      <div className="flex items-center gap-3">
        <label htmlFor="quantity" className="text-sm font-medium text-foreground">
          Quantity
        </label>
        <select
          id="quantity"
          value={quantity}
          onChange={(event) => setQuantity(Number(event.target.value))}
          disabled={!inStock}
          className="rounded-lg border border-zinc-300 px-3 py-2 text-sm"
        >
          {Array.from({ length: Math.min(maxQuantity, 10) }, (_, index) => index + 1).map(
            (value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ),
          )}
        </select>
      </div>

      <button
        type="button"
        onClick={handleAddToCart}
        disabled={!inStock || isSubmitting}
        className="w-full rounded-full bg-accent px-6 py-3.5 text-sm font-medium text-accent-foreground transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50 md:w-auto"
      >
        {inStock ? (isSubmitting ? "Adding…" : "Add to cart") : "Unavailable"}
      </button>

      {message && (
        <p className="text-sm text-emerald-700">{message}</p>
      )}
      {error && (
        <p className="text-sm text-red-600">{error}</p>
      )}
    </div>
  );
}
