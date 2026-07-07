import Link from "next/link";

import type { Category } from "@/types/catalog";

type CategoryPillsProps = {
  categories: Category[];
  activeSlug?: string;
};

export function CategoryPills({ categories, activeSlug }: CategoryPillsProps) {
  return (
    <div className="flex flex-wrap gap-2">
      <Pill href="/products" active={!activeSlug}>
        All
      </Pill>
      {categories.map((category) => (
        <Pill
          key={category.id}
          href={`/categories/${category.slug}`}
          active={activeSlug === category.slug}
        >
          {category.name}
        </Pill>
      ))}
    </div>
  );
}

function Pill({
  href,
  active,
  children,
}: {
  href: string;
  active: boolean;
  children: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      className={`rounded-full px-4 py-2 text-sm font-medium transition ${
        active
          ? "bg-accent text-accent-foreground shadow-sm"
          : "border border-border bg-card text-muted-foreground hover:border-foreground/20 hover:text-foreground"
      }`}
    >
      {children}
    </Link>
  );
}
