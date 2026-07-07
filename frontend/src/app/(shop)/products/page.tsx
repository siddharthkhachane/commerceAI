import { CategoryPills } from "@/components/catalog/CategoryPills";
import { CatalogPagination } from "@/components/catalog/CatalogPagination";
import { ProductGrid } from "@/components/catalog/ProductGrid";
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
    <div className="mx-auto max-w-7xl px-6 py-12 md:py-16">
      <div className="mb-10 space-y-4">
        <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
          Shop
        </p>
        <h1 className="text-4xl font-semibold tracking-tight text-zinc-950">
          All products
        </h1>
        <p className="max-w-2xl text-sm leading-7 text-zinc-600">
          Explore our full collection of premium essentials across every category.
        </p>
      </div>

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
