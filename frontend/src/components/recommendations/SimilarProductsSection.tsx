"use client";

import { useCallback } from "react";

import { RecommendationRail } from "@/components/recommendations/RecommendationRail";
import { getSimilarProducts } from "@/lib/recommendations";

type SimilarProductsSectionProps = {
  slug: string;
  inStock: boolean;
};

export function SimilarProductsSection({ slug, inStock }: SimilarProductsSectionProps) {
  const load = useCallback(() => getSimilarProducts(slug), [slug]);

  if (inStock) {
    return null;
  }

  return (
    <RecommendationRail
      title="Similar alternatives in stock"
      description="This item is unavailable right now. Here are close matches you can order today."
      load={load}
    />
  );
}
