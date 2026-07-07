import Link from "next/link";

import type { Category } from "@/types/catalog";

type CategoryPillsProps = {
  categories: Category[];
  activeSlug?: string;
};

export function CategoryPills({ categories, activeSlug }: CategoryPillsProps) {
  return (
    <div className="flex flex-wrap gap-2">
      <Link
        href="/products"
        className={`rounded-full px-4 py-2 text-sm font-medium transition ${
          !activeSlug
            ? "bg-zinc-950 text-white"
            : "bg-zinc-100 text-zinc-700 hover:bg-zinc-200"
        }`}
      >
        All
      </Link>
      {categories.map((category) => (
        <Link
          key={category.id}
          href={`/categories/${category.slug}`}
          className={`rounded-full px-4 py-2 text-sm font-medium transition ${
            activeSlug === category.slug
              ? "bg-zinc-950 text-white"
              : "bg-zinc-100 text-zinc-700 hover:bg-zinc-200"
          }`}
        >
          {category.name}
        </Link>
      ))}
    </div>
  );
}
