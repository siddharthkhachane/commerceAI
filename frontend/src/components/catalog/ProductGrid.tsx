import type { ProductSummary } from "@/types/catalog";

import { ProductCard } from "./ProductCard";

type ProductGridProps = {
  products: ProductSummary[];
};

export function ProductGrid({ products }: ProductGridProps) {
  if (products.length === 0) {
    return (
      <div className="rounded-2xl border border-dashed border-zinc-300 px-6 py-16 text-center">
        <p className="text-sm text-zinc-600">No products found.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 gap-x-5 gap-y-10 md:grid-cols-3 lg:grid-cols-4">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}
