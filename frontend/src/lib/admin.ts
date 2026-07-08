import { fetchApi } from "@/lib/api";
import type {
  AdminAnalytics,
  AdminInventoryResponse,
  AdminMetrics,
  AdminOrderSummary,
  BusinessAssistantResponse,
} from "@/types/admin";
import type { PageResponse } from "@/types/catalog";

export function getAdminMetrics(): Promise<AdminMetrics> {
  return fetchApi<AdminMetrics>("/api/admin/dashboard");
}

export function getAdminAnalytics(days = 30): Promise<AdminAnalytics> {
  return fetchApi<AdminAnalytics>(`/api/admin/analytics?days=${days}`);
}

export function getAdminOrders(page = 0, size = 20): Promise<PageResponse<AdminOrderSummary>> {
  return fetchApi<PageResponse<AdminOrderSummary>>(
    `/api/admin/orders?page=${page}&size=${size}`,
  );
}

export function getAdminInventory(page = 0, size = 20): Promise<AdminInventoryResponse> {
  return fetchApi<AdminInventoryResponse>(
    `/api/admin/inventory?page=${page}&size=${size}`,
  );
}

// Product listing for admin dashboard overview uses catalog management API.
export { getAdminCatalogProducts as getAdminProducts } from "@/lib/admin-catalog";

export function askBusinessAssistant(question: string): Promise<BusinessAssistantResponse> {
  return fetchApi<BusinessAssistantResponse>("/api/admin/assistant/chat", {
    method: "POST",
    body: JSON.stringify({ question }),
  });
}
