import Image from "next/image";
import Link from "next/link";

import { formatPrice } from "@/lib/format";
import type { ProductSummary } from "@/types/catalog";

type ProductCardProps = {
  product: ProductSummary;
  index?: number;
};

export function ProductCard({ product, index = 0 }: ProductCardProps) {
  return (
    <Link
      href={`/products/${product.slug}`}
      className="group block animate-fade-in-up"
      style={{ animationDelay: `${Math.min(index * 0.05, 0.35)}s` }}
    >
      <div className="relative overflow-hidden rounded-2xl border border-border/60 bg-card shadow-sm transition duration-300 group-hover:-translate-y-1 group-hover:border-border group-hover:shadow-lg">
        <div className="relative aspect-[4/5] overflow-hidden bg-muted">
          <Image
            src={product.imageUrl}
            alt={product.name}
            fill
            sizes="(max-width: 768px) 50vw, (max-width: 1200px) 33vw, 25vw"
            className="object-cover transition duration-700 group-hover:scale-105"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/25 via-transparent to-transparent opacity-0 transition duration-300 group-hover:opacity-100" />
          <div className="absolute bottom-4 left-4 right-4 translate-y-2 opacity-0 transition duration-300 group-hover:translate-y-0 group-hover:opacity-100">
            <span className="inline-flex rounded-full bg-white/95 px-3 py-1.5 text-xs font-medium text-zinc-900 shadow-sm backdrop-blur dark:bg-zinc-900/90 dark:text-zinc-100">
              View product
            </span>
          </div>
        </div>
      </div>
      <div className="mt-4 space-y-1.5 px-0.5">
        <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-muted-foreground">
          {product.category.name}
        </p>
        <h3 className="line-clamp-2 text-base font-medium leading-snug text-foreground transition group-hover:text-muted-foreground">
          {product.name}
        </h3>
        <p className="text-sm font-medium text-foreground/90">{formatPrice(product.price)}</p>
      </div>
    </Link>
  );
}
