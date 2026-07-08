import { fetchApi } from "@/lib/api";
import type { RecommendationResponse } from "@/types/recommendations";

export function getSimilarProducts(slug: string): Promise<RecommendationResponse> {
  return fetchApi<RecommendationResponse>(`/api/recommendations/similar/${slug}`);
}

export function getRelatedProducts(productIds: number[]): Promise<RecommendationResponse> {
  const params = productIds.map((id) => `productIds=${id}`).join("&");
  return fetchApi<RecommendationResponse>(`/api/recommendations/related?${params}`);
}

export function getForYouRecommendations(): Promise<RecommendationResponse> {
  return fetchApi<RecommendationResponse>("/api/recommendations/for-you");
}
