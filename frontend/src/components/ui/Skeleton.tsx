type SkeletonProps = {
  className?: string;
};

export function Skeleton({ className = "" }: SkeletonProps) {
  return <div className={`animate-shimmer rounded-xl ${className}`} />;
}

export function ProductCardSkeleton() {
  return (
    <div className="space-y-4">
      <Skeleton className="aspect-[4/5] w-full rounded-2xl" />
      <Skeleton className="h-3 w-20" />
      <Skeleton className="h-5 w-3/4" />
      <Skeleton className="h-4 w-16" />
    </div>
  );
}

export function ProductGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div className="grid grid-cols-2 gap-x-5 gap-y-10 md:grid-cols-3 lg:grid-cols-4">
      {Array.from({ length: count }).map((_, index) => (
        <ProductCardSkeleton key={index} />
      ))}
    </div>
  );
}

export function ProductDetailSkeleton() {
  return (
    <div className="mx-auto max-w-7xl px-6 py-12 md:py-16">
      <Skeleton className="mb-8 h-4 w-48" />
      <div className="grid gap-10 lg:grid-cols-2 lg:gap-16">
        <Skeleton className="aspect-[4/5] w-full rounded-3xl" />
        <div className="space-y-4">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-10 w-3/4" />
          <Skeleton className="h-8 w-28" />
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-12 w-40 rounded-full" />
        </div>
      </div>
    </div>
  );
}

export function PageHeaderSkeleton() {
  return (
    <div className="mb-10 space-y-4">
      <Skeleton className="h-4 w-16" />
      <Skeleton className="h-10 w-64" />
      <Skeleton className="h-5 w-96 max-w-full" />
    </div>
  );
}
