import { PageHeaderSkeleton, ProductGridSkeleton } from "@/components/ui/Skeleton";

export default function ProductsLoading() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 md:py-16">
      <PageHeaderSkeleton />
      <div className="mb-8 h-10 w-full max-w-md animate-shimmer rounded-full" />
      <div className="mb-8 flex gap-2">
        {Array.from({ length: 5 }).map((_, index) => (
          <div key={index} className="h-9 w-20 animate-shimmer rounded-full" />
        ))}
      </div>
      <ProductGridSkeleton count={12} />
    </div>
  );
}
