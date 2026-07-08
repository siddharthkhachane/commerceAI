import { fetchApi } from "@/lib/api";
import type { Category, ProductDetail } from "@/types/catalog";
import type {
  AdminProduct,
  AdminProductQuery,
  AdminProductsResponse,
  BulkUpdateProductsInput,
  BulkUpdateProductsResponse,
  CreateCategoryInput,
  CreateProductInput,
  UpdateCategoryInput,
  UpdateProductInput,
} from "@/types/admin-catalog";

function buildProductQuery(query: AdminProductQuery = {}): string {
  const params = new URLSearchParams();
  if (query.search) params.set("search", query.search);
  if (query.categoryId !== undefined) params.set("categoryId", String(query.categoryId));
  if (query.lowStockOnly) params.set("lowStockOnly", "true");
  if (query.active && query.active !== "all") params.set("active", query.active);
  if (query.page !== undefined) params.set("page", String(query.page));
  if (query.size !== undefined) params.set("size", String(query.size));
  if (query.sort) params.set("sort", query.sort);
  const queryString = params.toString();
  return queryString ? `?${queryString}` : "";
}

export function getAdminCategories(): Promise<Category[]> {
  return fetchApi<Category[]>("/api/admin/categories");
}

export function getAdminCatalogProducts(query: AdminProductQuery = {}): Promise<AdminProductsResponse> {
  return fetchApi<AdminProductsResponse>(`/api/admin/products${buildProductQuery(query)}`);
}

export function createAdminProduct(input: CreateProductInput): Promise<ProductDetail> {
  return fetchApi<ProductDetail>("/api/admin/products", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateAdminProduct(id: number, input: UpdateProductInput): Promise<ProductDetail> {
  return fetchApi<ProductDetail>(`/api/admin/products/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteAdminProduct(id: number): Promise<void> {
  return fetchApi<void>(`/api/admin/products/${id}`, { method: "DELETE" });
}

export function bulkUpdateAdminProducts(
  input: BulkUpdateProductsInput,
): Promise<BulkUpdateProductsResponse> {
  return fetchApi<BulkUpdateProductsResponse>("/api/admin/products/bulk-update", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function createAdminCategory(input: CreateCategoryInput): Promise<Category> {
  return fetchApi<Category>("/api/admin/categories", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateAdminCategory(id: number, input: UpdateCategoryInput): Promise<Category> {
  return fetchApi<Category>(`/api/admin/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteAdminCategory(id: number): Promise<void> {
  return fetchApi<void>(`/api/admin/categories/${id}`, { method: "DELETE" });
}
