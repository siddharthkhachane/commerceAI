"use client";

import { useRouter, useSearchParams } from "next/navigation";

const sortOptions = [
  { value: "newest", label: "Newest" },
  { value: "price-asc", label: "Price: Low to High" },
  { value: "price-desc", label: "Price: High to Low" },
  { value: "name-asc", label: "Name: A–Z" },
  { value: "name-desc", label: "Name: Z–A" },
] as const;

type SortSelectProps = {
  className?: string;
};

export function SortSelect({ className = "" }: SortSelectProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const currentSort = searchParams.get("sort") ?? "newest";

  function handleChange(value: string) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("sort", value);
    params.delete("page");
    router.push(`/products?${params.toString()}`);
  }

  return (
    <select
      value={currentSort}
      onChange={(event) => handleChange(event.target.value)}
      className={`rounded-full border border-border bg-card px-4 py-2.5 text-sm text-foreground outline-none transition focus:ring-2 focus:ring-ring/60 ${className}`}
      aria-label="Sort products"
    >
      {sortOptions.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}
