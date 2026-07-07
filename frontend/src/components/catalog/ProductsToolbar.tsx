"use client";

import { Suspense } from "react";

import { SortSelect } from "@/components/catalog/SortSelect";
import { SearchBar } from "@/components/ui/SearchBar";

type ProductsToolbarProps = {
  initialSearch?: string;
};

export function ProductsToolbar({ initialSearch = "" }: ProductsToolbarProps) {
  return (
    <div className="mb-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <Suspense fallback={<div className="h-10 w-full max-w-md animate-shimmer rounded-full" />}>
        <SearchBar className="w-full max-w-md" defaultValue={initialSearch} />
      </Suspense>
      <Suspense fallback={null}>
        <SortSelect className="w-full sm:w-auto" />
      </Suspense>
    </div>
  );
}
