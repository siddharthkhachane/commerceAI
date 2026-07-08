import type { Category, PageResponse } from "@/types/catalog";

export type AdminProduct = {
  id: number;
  name: string;
  slug: string;
  description: string;
  price: number;
  imageUrl: string;
  stockQuantity: number;
  isActive: boolean;
  category: Category;
};

export type AdminProductQuery = {
  search?: string;
  categoryId?: number;
  lowStockOnly?: boolean;
  active?: "all" | "active" | "inactive";
  page?: number;
  size?: number;
  sort?: "newest" | "price-asc" | "price-desc" | "name-asc" | "name-desc" | "stock-asc" | "stock-desc";
};

export type CreateProductInput = {
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  categoryId: number;
  stockQuantity: number;
  isActive: boolean;
};

export type UpdateProductInput = CreateProductInput;

export type CreateCategoryInput = {
  name: string;
  description?: string;
  imageUrl?: string;
};

export type UpdateCategoryInput = CreateCategoryInput;

export type BulkUpdateProductsInput = {
  productIds: number[];
  stockQuantity?: number;
  stockDelta?: number;
  price?: number;
  isActive?: boolean;
};

export type BulkUpdateProductsResponse = {
  updatedCount: number;
  products: AdminProduct[];
};

export type AdminProductsResponse = PageResponse<AdminProduct>;
