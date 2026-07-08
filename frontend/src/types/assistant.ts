import type { ProductSummary } from "@/types/catalog";
import type { PageResponse } from "@/types/catalog";

export type ShoppingAssistantRequest = {
  message: string;
  budget?: number;
};

export type ShoppingAssistantRecommendation = {
  product: ProductSummary;
  reason: string;
};

export type ShoppingAssistantResponse = {
  intent: string;
  budget: number | null;
  recommendations: ShoppingAssistantRecommendation[];
};

export type ProductSearchAssistantRequest = {
  query: string;
  page?: number;
  size?: number;
  sort?: "newest" | "price-asc" | "price-desc" | "name-asc" | "name-desc";
};

export type ProductSearchFilters = {
  normalizedQuery: string;
  occasion: string | null;
  categorySlugs: string[];
  keywords: string[];
  maxPrice: number | null;
};

export type ProductSearchAssistantResponse = {
  filters: ProductSearchFilters;
  products: PageResponse<ProductSummary>;
};
