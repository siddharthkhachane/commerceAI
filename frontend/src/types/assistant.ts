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

export type ProductCompareAssistantRequest = {
  productIds: number[];
  question?: string;
};

export type ProductComparisonInsight = {
  product: ProductSummary;
  material: string;
  qualityScore: number;
  priceScore: number;
  styleScore: number;
  pros: string[];
  cons: string[];
};

export type ProductCompareAssistantResponse = {
  question: string;
  comparisons: ProductComparisonInsight[];
  recommendedProductId: number;
  recommendation: string;
};

export type ShoppingContextRequest = {
  cartProductIds?: number[];
  savedProductIds?: number[];
  compareProductIds?: number[];
  viewingProductSlug?: string | null;
};

export type ShoppingContextSummary = {
  cartProductCount: number;
  savedProductCount: number;
  compareProductCount: number;
  viewingProductSlug: string | null;
  stylePreference: string | null;
  preferredCategorySlug: string | null;
  budgetPreference: number | null;
};

export type ToolCallResult = {
  toolName: string;
  summary: string;
};

export type ChatRecommendation = {
  product: ProductSummary;
  reason: string;
  toolName: string;
};

export type AssistantChatRequest = {
  conversationId?: number | null;
  message: string;
  sessionId?: string;
  budget?: number;
  context?: ShoppingContextRequest;
};

export type AssistantChatResponse = {
  conversationId: number;
  sessionId: string;
  reply: string;
  toolCalls: ToolCallResult[];
  recommendations: ChatRecommendation[];
  shoppingContext: ShoppingContextSummary;
  usedConversationMemory: boolean;
  avoidedRepeatRecommendations: number;
};

export type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  toolCalls?: ToolCallResult[];
  recommendations?: ChatRecommendation[];
};

export type ConversationSummary = {
  id: number;
  title: string;
  updatedAt: string;
  messageCount: number;
};
