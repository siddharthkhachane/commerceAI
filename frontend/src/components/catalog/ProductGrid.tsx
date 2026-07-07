import type { ProductSummary } from "@/types/catalog";

import { ProductCard } from "./ProductCard";

type ProductGridProps = {
  products: ProductSummary[];
};

export function ProductGrid({ products }: ProductGridProps) {
  if (products.length === 0) {
    return (
      <div className="rounded-3xl border border-dashed border-border bg-card px-6 py-20 text-center animate-fade-in">
        <p className="text-base font-medium text-foreground">No products found</p>
        <p className="mt-2 text-sm text-muted-foreground">
          Try adjusting your search or browse all categories.
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 gap-x-4 gap-y-10 sm:gap-x-5 md:grid-cols-3 lg:grid-cols-4">
      {products.map((product, index) => (
        <ProductCard key={product.id} product={product} index={index} />
      ))}
    </div>
  );
}
