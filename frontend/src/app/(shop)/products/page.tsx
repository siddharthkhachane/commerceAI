import { CategoryPills } from "@/components/catalog/CategoryPills";
import { CatalogPagination } from "@/components/catalog/CatalogPagination";
import { ProductGrid } from "@/components/catalog/ProductGrid";
import { ProductsToolbar } from "@/components/catalog/ProductsToolbar";
import { getCategories, getProducts } from "@/lib/catalog";

type ProductsPageProps = {
  searchParams: Promise<{
    page?: string;
    search?: string;
    sort?: string;
  }>;
};

export default async function ProductsPage({ searchParams }: ProductsPageProps) {
  const params = await searchParams;
  const page = Number(params.page ?? "0");
  const sort = (params.sort as "price-asc" | "price-desc" | "name-asc" | "name-desc" | "newest") ?? "newest";

  const [categories, products] = await Promise.all([
    getCategories(),
    getProducts({
      page: Number.isNaN(page) ? 0 : page,
      size: 24,
      search: params.search,
      sort,
    }),
  ]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 md:py-16">
      <div className="mb-10 animate-fade-in-up space-y-4">
        <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
          Shop
        </p>
        <h1 className="text-4xl font-semibold tracking-tight text-foreground sm:text-5xl">
          {params.search ? `Results for “${params.search}”` : "All products"}
        </h1>
        <p className="max-w-2xl text-sm leading-7 text-muted-foreground">
          Explore our full collection of premium essentials across every category.
        </p>
      </div>

      <ProductsToolbar initialSearch={params.search} />

      <div className="mb-8">
        <CategoryPills categories={categories} />
      </div>

      <ProductGrid products={products.content} />

      <CatalogPagination
        page={products.page}
        totalPages={products.totalPages}
        basePath="/products"
        searchParams={{
          search: params.search,
          sort,
        }}
      />
    </div>
  );
}
