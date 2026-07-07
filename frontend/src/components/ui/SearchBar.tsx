"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

type SearchBarProps = {
  placeholder?: string;
  className?: string;
  defaultValue?: string;
};

export function SearchBar({
  placeholder = "Search products…",
  className = "",
  defaultValue = "",
}: SearchBarProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(defaultValue);

  useEffect(() => {
    setQuery(searchParams.get("search") ?? defaultValue);
  }, [searchParams, defaultValue]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = query.trim();
    if (trimmed) {
      router.push(`/products?search=${encodeURIComponent(trimmed)}`);
    } else {
      router.push("/products");
    }
  }

  return (
    <form onSubmit={handleSubmit} className={`relative ${className}`}>
      <svg
        className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
        viewBox="0 0 24 24"
        fill="none"
        aria-hidden
      >
        <path
          d="M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15Z"
          stroke="currentColor"
          strokeWidth="1.5"
        />
        <path
          d="M16 16l4.5 4.5"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinecap="round"
        />
      </svg>
      <input
        type="search"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder={placeholder}
        className="w-full rounded-full border border-border bg-card py-2.5 pl-11 pr-4 text-sm text-foreground outline-none transition placeholder:text-muted-foreground focus:border-foreground/30 focus:ring-2 focus:ring-ring/60"
      />
    </form>
  );
}
