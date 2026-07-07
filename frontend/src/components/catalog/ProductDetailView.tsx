import Image from "next/image";
import Link from "next/link";

import { AddToCartButton } from "@/components/cart/AddToCartButton";
import { formatPrice } from "@/lib/format";
import type { ProductDetail } from "@/types/catalog";

type ProductDetailViewProps = {
  product: ProductDetail;
};

export function ProductDetailView({ product }: ProductDetailViewProps) {
  const inStock = product.stockQuantity > 0;

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 md:py-16">
      <div className="mb-8 animate-fade-in text-sm text-muted-foreground">
        <Link href="/products" className="transition hover:text-foreground">
          Shop
        </Link>
        <span className="mx-2">/</span>
        <Link
          href={`/categories/${product.category.slug}`}
          className="transition hover:text-foreground"
        >
          {product.category.name}
        </Link>
      </div>

      <div className="grid gap-10 lg:grid-cols-2 lg:gap-16">
        <div className="animate-scale-in overflow-hidden rounded-3xl border border-border bg-muted shadow-sm">
          <div className="relative aspect-[4/5]">
            <Image
              src={product.imageUrl}
              alt={product.name}
              fill
              priority
              sizes="(max-width: 1024px) 100vw, 50vw"
              className="object-cover"
            />
          </div>
        </div>

        <div className="flex animate-fade-in-up flex-col justify-center">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
            {product.category.name}
          </p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight text-foreground sm:text-5xl">
            {product.name}
          </h1>
          <p className="mt-4 text-2xl font-medium text-foreground">{formatPrice(product.price)}</p>
          <p className="mt-6 text-base leading-7 text-muted-foreground">{product.description}</p>

          <div className="mt-8 flex items-center gap-3">
            <span
              className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ring-1 ${
                inStock
                  ? "bg-emerald-500/10 text-emerald-700 ring-emerald-500/20 dark:text-emerald-300"
                  : "bg-red-500/10 text-red-700 ring-red-500/20 dark:text-red-300"
              }`}
            >
              {inStock ? `${product.stockQuantity} in stock` : "Out of stock"}
            </span>
          </div>

          <AddToCartButton product={product} />
        </div>
      </div>
    </div>
  );
}
