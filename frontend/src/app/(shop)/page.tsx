import Link from "next/link";

import { CategoryGrid } from "@/components/catalog/CategoryGrid";
import { ProductGrid } from "@/components/catalog/ProductGrid";
import { ShopHero } from "@/components/catalog/ShopHero";
import { getCategories, getProducts } from "@/lib/catalog";

export default async function HomePage() {
  let categories: Awaited<ReturnType<typeof getCategories>> = [];
  let featured: Awaited<ReturnType<typeof getProducts>> = {
    content: [],
    page: 0,
    size: 8,
    totalElements: 0,
    totalPages: 0,
  };

  try {
    const [categoryData, productData] = await Promise.all([
      getCategories(),
      getProducts({ page: 0, size: 8, sort: "newest" }),
    ]);
    categories = categoryData;
    featured = productData;
  } catch {
    // Backend unavailable during build or local startup.
  }

  return (
    <>
      <ShopHero
        title="Quality essentials, thoughtfully made."
        description="Discover premium pieces across apparel, home, and lifestyle — curated with the calm confidence of a modern retailer."
      />

      <section className="mx-auto max-w-7xl px-6 py-16 md:py-20">
        <div className="mb-8 flex items-end justify-between gap-4">
          <div>
            <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
              Categories
            </p>
            <h2 className="mt-2 text-3xl font-semibold tracking-tight text-zinc-950">
              Shop by collection
            </h2>
          </div>
          <Link
            href="/products"
            className="hidden text-sm font-medium text-zinc-700 hover:text-zinc-950 md:inline"
          >
            View all
          </Link>
        </div>
        <CategoryGrid categories={categories} />
      </section>

      <section className="bg-zinc-50">
        <div className="mx-auto max-w-7xl px-6 py-16 md:py-20">
          <div className="mb-8 flex items-end justify-between gap-4">
            <div>
              <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
                New arrivals
              </p>
              <h2 className="mt-2 text-3xl font-semibold tracking-tight text-zinc-950">
                Featured products
              </h2>
            </div>
            <Link
              href="/products"
              className="text-sm font-medium text-zinc-700 hover:text-zinc-950"
            >
              Shop all
            </Link>
          </div>
          <ProductGrid products={featured.content} />
        </div>
      </section>
    </>
  );
}
