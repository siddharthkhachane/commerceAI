export type Category = {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  imageUrl: string | null;
  productCount?: number | null;
};

export type ProductSummary = {
  id: number;
  name: string;
  slug: string;
  price: number;
  imageUrl: string;
  category: Category;
};

export type ProductDetail = ProductSummary & {
  description: string;
  stockQuantity: number;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ProductQuery = {
  category?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: "newest" | "price-asc" | "price-desc" | "name-asc" | "name-desc";
};
