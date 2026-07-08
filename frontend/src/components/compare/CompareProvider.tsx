"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import type { ProductSummary } from "@/types/catalog";

const STORAGE_KEY = "compare-products";
const MAX_COMPARE_ITEMS = 4;

type CompareContextValue = {
  items: ProductSummary[];
  addItem: (product: ProductSummary) => void;
  removeItem: (productId: number) => void;
  clear: () => void;
  isSelected: (productId: number) => boolean;
  canAddMore: boolean;
};

const CompareContext = createContext<CompareContextValue | null>(null);

function readStoredItems(): ProductSummary[] {
  if (typeof window === "undefined") return [];
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as ProductSummary[];
    return Array.isArray(parsed) ? parsed.slice(0, MAX_COMPARE_ITEMS) : [];
  } catch {
    return [];
  }
}

export function CompareProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<ProductSummary[]>([]);

  useEffect(() => {
    setItems(readStoredItems());
  }, []);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items]);

  const addItem = useCallback((product: ProductSummary) => {
    setItems((current) => {
      if (current.some((item) => item.id === product.id)) return current;
      if (current.length >= MAX_COMPARE_ITEMS) return current;
      return [...current, product];
    });
  }, []);

  const removeItem = useCallback((productId: number) => {
    setItems((current) => current.filter((item) => item.id !== productId));
  }, []);

  const clear = useCallback(() => {
    setItems([]);
  }, []);

  const isSelected = useCallback(
    (productId: number) => items.some((item) => item.id === productId),
    [items],
  );

  const value = useMemo(
    () => ({
      items,
      addItem,
      removeItem,
      clear,
      isSelected,
      canAddMore: items.length < MAX_COMPARE_ITEMS,
    }),
    [items, addItem, removeItem, clear, isSelected],
  );

  return <CompareContext.Provider value={value}>{children}</CompareContext.Provider>;
}

export function useCompare() {
  const context = useContext(CompareContext);
  if (!context) {
    throw new Error("useCompare must be used within CompareProvider");
  }
  return context;
}
