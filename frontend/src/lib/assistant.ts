import { fetchApi } from "@/lib/api";
import { fetchApiServer } from "@/lib/api-server";
import type {
  ProductSearchAssistantRequest,
  ProductSearchAssistantResponse,
  ShoppingAssistantRequest,
  ShoppingAssistantResponse,
} from "@/types/assistant";

export function getShoppingRecommendations(
  payload: ShoppingAssistantRequest,
): Promise<ShoppingAssistantResponse> {
  return fetchApi<ShoppingAssistantResponse>("/api/assistant/shopping", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function searchProductsWithAssistant(
  payload: ProductSearchAssistantRequest,
): Promise<ProductSearchAssistantResponse> {
  return fetchApiServer<ProductSearchAssistantResponse>("/api/assistant/product-search", {
    method: "POST",
    body: JSON.stringify(payload),
    revalidate: 0,
  });
}
