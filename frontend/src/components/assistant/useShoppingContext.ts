"use client";

import { useMemo } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { useCart } from "@/components/cart/CartProvider";
import { useCompare } from "@/components/compare/CompareProvider";
import { useSavedItems } from "@/components/profile/SavedItemsProvider";
import type { ShoppingContextRequest } from "@/types/assistant";

type UseShoppingContextOptions = {
  viewingProductSlug?: string | null;
};

export function useShoppingContext(options: UseShoppingContextOptions = {}) {
  const { user } = useAuth();
  const { cart } = useCart();
  const { items: compareItems } = useCompare();
  const { savedProductIds } = useSavedItems();

  const context = useMemo<ShoppingContextRequest>(
    () => ({
      cartProductIds: user ? cart.items.map((item) => item.productId) : [],
      savedProductIds: user ? [...savedProductIds] : [],
      compareProductIds: compareItems.map((item) => item.id),
      viewingProductSlug: options.viewingProductSlug ?? null,
    }),
    [user, cart.items, savedProductIds, compareItems, options.viewingProductSlug],
  );

  const summary = useMemo(
    () => ({
      cartCount: context.cartProductIds?.length ?? 0,
      savedCount: context.savedProductIds?.length ?? 0,
      compareCount: context.compareProductIds?.length ?? 0,
      viewingSlug: context.viewingProductSlug ?? null,
    }),
    [context],
  );

  return { context, summary, isAuthenticated: Boolean(user) };
}
