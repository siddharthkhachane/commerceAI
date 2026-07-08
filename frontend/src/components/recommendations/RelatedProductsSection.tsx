"use client";

import { useCallback } from "react";

import { RecommendationRail } from "@/components/recommendations/RecommendationRail";
import { getRelatedProducts } from "@/lib/recommendations";

type RelatedProductsSectionProps = {
  productIds: number[];
};

export function RelatedProductsSection({ productIds }: RelatedProductsSectionProps) {
  const idsKey = productIds.join(",");
  const load = useCallback(
    () => getRelatedProducts(productIds),
    [idsKey, productIds],
  );

  if (productIds.length === 0) {
    return null;
  }

  return (
    <RecommendationRail
      title="Customers also bought"
      description="Based on your purchase, these related products are a great next pick."
      load={load}
    />
  );
}
