import { ProductGridSkeleton } from "@/components/ui/Skeleton";

export default function ShopLoading() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 md:py-16">
      <div className="mb-10 space-y-4">
        <div className="h-4 w-20 animate-shimmer rounded-lg" />
        <div className="h-10 w-72 max-w-full animate-shimmer rounded-xl" />
        <div className="h-5 w-96 max-w-full animate-shimmer rounded-lg" />
      </div>
      <ProductGridSkeleton count={8} />
    </div>
  );
}
