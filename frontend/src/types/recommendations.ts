import type { ProductSummary } from "@/types/catalog";

export type ProductRecommendation = {
  product: ProductSummary;
  reason: string;
};

export type RecommendationResponse = {
  type: "similar" | "related" | "for-you";
  recommendations: ProductRecommendation[];
};
