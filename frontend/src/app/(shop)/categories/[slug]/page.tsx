import type { Metadata } from "next";
import { notFound } from "next/navigation";

import { CategoryPills } from "@/components/catalog/CategoryPills";
import { CatalogPagination } from "@/components/catalog/CatalogPagination";
import { ProductGrid } from "@/components/catalog/ProductGrid";
import { getCategories, getCategory, getProducts } from "@/lib/catalog";

type CategoryPageProps = {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{ page?: string }>;
};

export async function generateMetadata({
  params,
}: CategoryPageProps): Promise<Metadata> {
  try {
    const { slug } = await params;
    const category = await getCategory(slug);
    return {
      title: `${category.name} | CommerceAI`,
      description: category.description ?? undefined,
    };
  } catch {
    return { title: "Category | CommerceAI" };
  }
}

export default async function CategoryPage({
  params,
  searchParams,
}: CategoryPageProps) {
  const { slug } = await params;
  const query = await searchParams;
  const page = Number(query.page ?? "0");

  try {
    const [categories, category, products] = await Promise.all([
      getCategories(),
      getCategory(slug),
      getProducts({
        category: slug,
        page: Number.isNaN(page) ? 0 : page,
        size: 24,
      }),
    ]);

    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 md:py-16">
        <div className="mb-10 animate-fade-in-up space-y-4">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
            Category
          </p>
          <h1 className="text-4xl font-semibold tracking-tight text-foreground sm:text-5xl">
            {category.name}
          </h1>
          {category.description && (
            <p className="max-w-2xl text-sm leading-7 text-muted-foreground">
              {category.description}
            </p>
          )}
        </div>

        <div className="mb-8">
          <CategoryPills categories={categories} activeSlug={slug} />
        </div>

        <ProductGrid products={products.content} />

        <CatalogPagination
          page={products.page}
          totalPages={products.totalPages}
          basePath={`/categories/${slug}`}
        />
      </div>
    );
  } catch {
    notFound();
  }
}
