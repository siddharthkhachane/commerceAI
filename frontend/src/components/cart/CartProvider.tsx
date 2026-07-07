"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import * as cartApi from "@/lib/cart";
import type { Cart } from "@/types/cart";

const emptyCart: Cart = { items: [], itemCount: 0, subtotal: 0 };

type CartContextValue = {
  cart: Cart;
  isLoading: boolean;
  refreshCart: () => Promise<void>;
  addItem: (productId: number, quantity?: number) => Promise<void>;
  updateItem: (productId: number, quantity: number) => Promise<void>;
  removeItem: (productId: number) => Promise<void>;
};

const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const [cart, setCart] = useState<Cart>(emptyCart);
  const [isLoading, setIsLoading] = useState(false);

  const refreshCart = useCallback(async () => {
    if (!user) {
      setCart(emptyCart);
      return;
    }

    setIsLoading(true);
    try {
      const nextCart = await cartApi.getCart();
      setCart(nextCart);
    } catch {
      setCart(emptyCart);
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const addItem = useCallback(
    async (productId: number, quantity = 1) => {
      const nextCart = await cartApi.addCartItem({ productId, quantity });
      setCart(nextCart);
    },
    [],
  );

  const updateItem = useCallback(async (productId: number, quantity: number) => {
    const nextCart = await cartApi.updateCartItem(productId, { quantity });
    setCart(nextCart);
  }, []);

  const removeItem = useCallback(async (productId: number) => {
    const nextCart = await cartApi.removeCartItem(productId);
    setCart(nextCart);
  }, []);

  const value = useMemo(
    () => ({ cart, isLoading, refreshCart, addItem, updateItem, removeItem }),
    [cart, isLoading, refreshCart, addItem, updateItem, removeItem],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within CartProvider");
  }
  return context;
}
