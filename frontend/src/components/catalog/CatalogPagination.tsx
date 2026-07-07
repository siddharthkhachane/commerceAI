import Link from "next/link";

type CatalogPaginationProps = {
  page: number;
  totalPages: number;
  basePath: string;
  searchParams?: Record<string, string | undefined>;
};

export function CatalogPagination({
  page,
  totalPages,
  basePath,
  searchParams = {},
}: CatalogPaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  function buildHref(targetPage: number) {
    const params = new URLSearchParams();
    Object.entries(searchParams).forEach(([key, value]) => {
      if (value) params.set(key, value);
    });
    params.set("page", String(targetPage));
    return `${basePath}?${params.toString()}`;
  }

  return (
    <div className="mt-12 flex items-center justify-center gap-4">
      <Link
        href={buildHref(Math.max(0, page - 1))}
        aria-disabled={page <= 0}
        className={`rounded-full border px-4 py-2 text-sm font-medium ${
          page <= 0
            ? "pointer-events-none border-zinc-200 text-zinc-300"
            : "border-zinc-300 text-zinc-700 hover:bg-zinc-50"
        }`}
      >
        Previous
      </Link>
      <span className="text-sm text-zinc-600">
        Page {page + 1} of {totalPages}
      </span>
      <Link
        href={buildHref(Math.min(totalPages - 1, page + 1))}
        aria-disabled={page >= totalPages - 1}
        className={`rounded-full border px-4 py-2 text-sm font-medium ${
          page >= totalPages - 1
            ? "pointer-events-none border-zinc-200 text-zinc-300"
            : "border-zinc-300 text-zinc-700 hover:bg-zinc-50"
        }`}
      >
        Next
      </Link>
    </div>
  );
}
