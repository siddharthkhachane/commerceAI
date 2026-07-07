import Image from "next/image";
import Link from "next/link";

import type { Category } from "@/types/catalog";

type CategoryGridProps = {
  categories: Category[];
};

export function CategoryGrid({ categories }: CategoryGridProps) {
  return (
    <div className="stagger-children grid gap-4 sm:gap-5 md:grid-cols-2 lg:grid-cols-4">
      {categories.map((category) => (
        <Link
          key={category.id}
          href={`/categories/${category.slug}`}
          className="group relative overflow-hidden rounded-3xl border border-border/60 bg-card shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-lg"
        >
          <div className="relative aspect-[5/4] bg-muted">
            {category.imageUrl && (
              <Image
                src={category.imageUrl}
                alt={category.name}
                fill
                sizes="(max-width: 768px) 100vw, 25vw"
                className="object-cover transition duration-700 group-hover:scale-105"
              />
            )}
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent" />
            <div className="absolute inset-x-0 bottom-0 p-5 text-white">
              <p className="text-lg font-medium">{category.name}</p>
              {category.productCount != null && (
                <p className="mt-1 text-sm text-white/75">
                  {category.productCount} products
                </p>
              )}
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
}
