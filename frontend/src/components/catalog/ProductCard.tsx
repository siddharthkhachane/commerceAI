import Image from "next/image";
import Link from "next/link";

import { formatPrice } from "@/lib/format";
import type { ProductSummary } from "@/types/catalog";

type ProductCardProps = {
  product: ProductSummary;
};

export function ProductCard({ product }: ProductCardProps) {
  return (
    <Link href={`/products/${product.slug}`} className="group block">
      <div className="overflow-hidden rounded-2xl bg-zinc-100">
        <div className="relative aspect-[4/5] overflow-hidden">
          <Image
            src={product.imageUrl}
            alt={product.name}
            fill
            sizes="(max-width: 768px) 50vw, (max-width: 1200px) 33vw, 25vw"
            className="object-cover transition duration-500 group-hover:scale-105"
          />
        </div>
      </div>
      <div className="mt-4 space-y-1">
        <p className="text-xs font-medium uppercase tracking-[0.18em] text-zinc-500">
          {product.category.name}
        </p>
        <h3 className="text-base font-medium text-zinc-950 transition group-hover:text-zinc-600">
          {product.name}
        </h3>
        <p className="text-sm text-zinc-700">{formatPrice(product.price)}</p>
      </div>
    </Link>
  );
}
