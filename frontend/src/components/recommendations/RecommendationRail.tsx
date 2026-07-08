"use client";

import Link from "next/link";
import Image from "next/image";
import { useEffect, useState } from "react";

import { formatPrice } from "@/lib/format";
import type { ProductRecommendation } from "@/types/recommendations";

type RecommendationRailProps = {
  title: string;
  description?: string;
  load: () => Promise<{ recommendations: ProductRecommendation[] }>;
};

export function RecommendationRail({ title, description, load }: RecommendationRailProps) {
  const [items, setItems] = useState<ProductRecommendation[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    load()
      .then((response) => setItems(response.recommendations))
      .catch(() => setError("Could not load recommendations."));
  }, [load]);

  if (error || items.length === 0) {
    return null;
  }

  return (
    <section className="mt-16 border-t border-border pt-12">
      <div className="mb-8">
        <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
          Recommended for you
        </p>
        <h2 className="mt-2 text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
          {title}
        </h2>
        {description ? (
          <p className="mt-2 max-w-2xl text-sm text-muted-foreground">{description}</p>
        ) : null}
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {items.map((item) => (
          <Link
            key={item.product.id}
            href={`/products/${item.product.slug}`}
            className="rounded-2xl border border-border bg-card p-4 transition hover:-translate-y-0.5 hover:shadow-sm"
          >
            <div className="relative mb-4 aspect-[4/5] overflow-hidden rounded-xl bg-muted">
              <Image
                src={item.product.imageUrl}
                alt={item.product.name}
                fill
                sizes="(max-width: 1024px) 50vw, 25vw"
                className="object-cover"
              />
            </div>
            <p className="text-xs uppercase tracking-[0.14em] text-muted-foreground">
              {item.product.category.name}
            </p>
            <h3 className="mt-1 line-clamp-2 text-sm font-semibold text-foreground">
              {item.product.name}
            </h3>
            <p className="mt-1 text-sm font-medium">{formatPrice(item.product.price)}</p>
            <p className="mt-3 text-xs leading-5 text-muted-foreground">{item.reason}</p>
          </Link>
        ))}
      </div>
    </section>
  );
}
