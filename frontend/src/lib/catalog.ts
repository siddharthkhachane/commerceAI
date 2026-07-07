import { fetchApi } from "@/lib/api";
import { fetchApiServer } from "@/lib/api-server";
import type { Category, PageResponse, ProductDetail, ProductQuery, ProductSummary } from "@/types/catalog";

function buildProductQuery(query: ProductQuery = {}): string {
  const params = new URLSearchParams();
  if (query.category) params.set("category", query.category);
  if (query.search) params.set("search", query.search);
  if (query.page !== undefined) params.set("page", String(query.page));
  if (query.size !== undefined) params.set("size", String(query.size));
  if (query.sort) params.set("sort", query.sort);
  const queryString = params.toString();
  return queryString ? `?${queryString}` : "";
}

export function getCategories(): Promise<Category[]> {
  return fetchApiServer<Category[]>("/api/categories");
}

export function getCategory(slug: string): Promise<Category> {
  return fetchApiServer<Category>(`/api/categories/${slug}`);
}

export function getProducts(query: ProductQuery = {}): Promise<PageResponse<ProductSummary>> {
  return fetchApiServer<PageResponse<ProductSummary>>(
    `/api/products${buildProductQuery(query)}`,
  );
}

export function getProduct(slug: string): Promise<ProductDetail> {
  return fetchApiServer<ProductDetail>(`/api/products/${slug}`);
}

export function searchProductsClient(
  query: ProductQuery,
): Promise<PageResponse<ProductSummary>> {
  return fetchApi<PageResponse<ProductSummary>>(`/api/products${buildProductQuery(query)}`);
}
