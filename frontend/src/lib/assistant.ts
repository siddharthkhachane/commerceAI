import { fetchApi } from "@/lib/api";
import { fetchApiServer } from "@/lib/api-server";
import type {
  AssistantChatRequest,
  AssistantChatResponse,
  ConversationSummary,
  ProductCompareAssistantRequest,
  ProductCompareAssistantResponse,
  ProductSearchAssistantRequest,
  ProductSearchAssistantResponse,
  ShoppingAssistantRequest,
  ShoppingAssistantResponse,
} from "@/types/assistant";

export function sendAssistantChat(
  payload: AssistantChatRequest,
): Promise<AssistantChatResponse> {
  return fetchApi<AssistantChatResponse>("/api/assistant/chat", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function listAssistantConversations(): Promise<ConversationSummary[]> {
  return fetchApi<ConversationSummary[]>("/api/assistant/conversations");
}

export function getAssistantConversation(
  conversationId: number,
  sessionId?: string,
): Promise<{
  id: number;
  title: string;
  sessionId: string;
  messages: Array<{
    id: number;
    role: "USER" | "ASSISTANT" | "TOOL";
    content: string;
    toolName: string | null;
    createdAt: string;
  }>;
}> {
  const query = sessionId ? `?sessionId=${encodeURIComponent(sessionId)}` : "";
  return fetchApi(`/api/assistant/conversations/${conversationId}${query}`);
}

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

export function compareProducts(
  payload: ProductCompareAssistantRequest,
): Promise<ProductCompareAssistantResponse> {
  return fetchApi<ProductCompareAssistantResponse>("/api/assistant/compare", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
