import Image from "next/image";
import Link from "next/link";

import { formatPrice } from "@/lib/format";
import type { ProductDetail } from "@/types/catalog";

type ProductDetailViewProps = {
  product: ProductDetail;
};

export function ProductDetailView({ product }: ProductDetailViewProps) {
  const inStock = product.stockQuantity > 0;

  return (
    <div className="mx-auto max-w-7xl px-6 py-12 md:py-16">
      <div className="mb-8 text-sm text-zinc-500">
        <Link href="/products" className="hover:text-zinc-950">
          Shop
        </Link>
        <span className="mx-2">/</span>
        <Link
          href={`/categories/${product.category.slug}`}
          className="hover:text-zinc-950"
        >
          {product.category.name}
        </Link>
      </div>

      <div className="grid gap-10 lg:grid-cols-2 lg:gap-16">
        <div className="overflow-hidden rounded-3xl bg-zinc-100">
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

        <div className="flex flex-col justify-center">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
            {product.category.name}
          </p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight text-zinc-950">
            {product.name}
          </h1>
          <p className="mt-4 text-2xl text-zinc-800">{formatPrice(product.price)}</p>
          <p className="mt-6 text-base leading-7 text-zinc-600">{product.description}</p>

          <div className="mt-8 flex items-center gap-3">
            <span
              className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ring-1 ${
                inStock
                  ? "bg-emerald-50 text-emerald-700 ring-emerald-200"
                  : "bg-red-50 text-red-700 ring-red-200"
              }`}
            >
              {inStock ? `${product.stockQuantity} in stock` : "Out of stock"}
            </span>
          </div>

          <button
            type="button"
            disabled={!inStock}
            className="mt-8 w-full rounded-full bg-zinc-950 px-6 py-3.5 text-sm font-medium text-white transition hover:bg-zinc-800 disabled:cursor-not-allowed disabled:bg-zinc-300 md:w-auto"
          >
            {inStock ? "Add to cart" : "Unavailable"}
          </button>
          <p className="mt-3 text-sm text-zinc-500">
            Cart checkout arrives in a future phase.
          </p>
        </div>
      </div>
    </div>
  );
}
