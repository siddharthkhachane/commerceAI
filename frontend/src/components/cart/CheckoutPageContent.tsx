"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { useCart } from "@/components/cart/CartProvider";
import { ApiRequestError } from "@/lib/api";
import { formatPrice } from "@/lib/format";
import * as ordersApi from "@/lib/orders";

export function CheckoutPageContent() {
  const router = useRouter();
  const { user } = useAuth();
  const { cart } = useCart();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    const formData = new FormData(event.currentTarget);

    try {
      const order = await ordersApi.checkout({
        shippingName: String(formData.get("shippingName")),
        shippingEmail: String(formData.get("shippingEmail")),
        shippingAddress: String(formData.get("shippingAddress")),
        shippingCity: String(formData.get("shippingCity")),
        shippingState: String(formData.get("shippingState")),
        shippingPostalCode: String(formData.get("shippingPostalCode")),
      });
      router.push(`/orders/${order.id}`);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : "Checkout failed.");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (cart.items.length === 0) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-16 text-center">
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-950">Checkout</h1>
        <p className="mt-4 text-sm text-zinc-600">Your cart is empty.</p>
        <Link href="/products" className="mt-8 inline-block text-sm font-medium text-zinc-950">
          Continue shopping
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12 md:py-16">
      <h1 className="text-3xl font-semibold tracking-tight text-zinc-950">Checkout</h1>
      <p className="mt-2 text-sm text-zinc-600">
        No payment required for this demo — we&apos;ll create your order immediately.
      </p>

      <form onSubmit={handleSubmit} className="mt-10 space-y-5">
        <div className="grid gap-5 sm:grid-cols-2">
          <Field
            id="shippingName"
            label="Full name"
            defaultValue={user?.fullName ?? ""}
            required
          />
          <Field
            id="shippingEmail"
            label="Email"
            type="email"
            defaultValue={user?.email ?? ""}
            required
          />
        </div>
        <Field id="shippingAddress" label="Address" required />
        <div className="grid gap-5 sm:grid-cols-3">
          <Field id="shippingCity" label="City" required />
          <Field id="shippingState" label="State" required />
          <Field id="shippingPostalCode" label="Postal code" required />
        </div>

        <div className="rounded-2xl border border-zinc-200 bg-zinc-50 p-5">
          <p className="text-sm text-zinc-600">Order total</p>
          <p className="mt-1 text-2xl font-semibold text-zinc-950">
            {formatPrice(cart.subtotal)}
          </p>
        </div>

        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-full bg-zinc-950 px-6 py-3.5 text-sm font-medium text-white transition hover:bg-zinc-800 disabled:opacity-60 sm:w-auto"
        >
          {isSubmitting ? "Placing order…" : "Place order"}
        </button>
      </form>
    </div>
  );
}

function Field({
  id,
  label,
  type = "text",
  defaultValue,
  required = false,
}: {
  id: string;
  label: string;
  type?: string;
  defaultValue?: string;
  required?: boolean;
}) {
  return (
    <div className="space-y-2">
      <label htmlFor={id} className="block text-sm font-medium text-zinc-700">
        {label}
      </label>
      <input
        id={id}
        name={id}
        type={type}
        defaultValue={defaultValue}
        required={required}
        className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm"
      />
    </div>
  );
}
