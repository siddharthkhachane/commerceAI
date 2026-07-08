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
import * as profileApi from "@/lib/profile";

type SavedItemsContextValue = {
  savedProductIds: Set<number>;
  isLoading: boolean;
  isSaved: (productId: number) => boolean;
  saveProduct: (productId: number) => Promise<void>;
  removeProduct: (productId: number) => Promise<void>;
  toggleSaved: (productId: number) => Promise<void>;
  refresh: () => Promise<void>;
};

const SavedItemsContext = createContext<SavedItemsContextValue | null>(null);

export function SavedItemsProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const [savedProductIds, setSavedProductIds] = useState<Set<number>>(new Set());
  const [isLoading, setIsLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!user) {
      setSavedProductIds(new Set());
      return;
    }

    setIsLoading(true);
    try {
      const items = await profileApi.getSavedItems();
      setSavedProductIds(new Set(items.map((item) => item.product.id)));
    } catch {
      setSavedProductIds(new Set());
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const saveProduct = useCallback(async (productId: number) => {
    await profileApi.saveItem(productId);
    setSavedProductIds((current) => new Set([...current, productId]));
  }, []);

  const removeProduct = useCallback(async (productId: number) => {
    await profileApi.removeSavedItem(productId);
    setSavedProductIds((current) => {
      const next = new Set(current);
      next.delete(productId);
      return next;
    });
  }, []);

  const toggleSaved = useCallback(
    async (productId: number) => {
      if (savedProductIds.has(productId)) {
        await removeProduct(productId);
      } else {
        await saveProduct(productId);
      }
    },
    [removeProduct, saveProduct, savedProductIds],
  );

  const isSaved = useCallback(
    (productId: number) => savedProductIds.has(productId),
    [savedProductIds],
  );

  const value = useMemo(
    () => ({
      savedProductIds,
      isLoading,
      isSaved,
      saveProduct,
      removeProduct,
      toggleSaved,
      refresh,
    }),
    [savedProductIds, isLoading, isSaved, saveProduct, removeProduct, toggleSaved, refresh],
  );

  return <SavedItemsContext.Provider value={value}>{children}</SavedItemsContext.Provider>;
}

export function useSavedItems() {
  const context = useContext(SavedItemsContext);
  if (!context) {
    throw new Error("useSavedItems must be used within SavedItemsProvider");
  }
  return context;
}
