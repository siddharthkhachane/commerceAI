"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useAuth } from "@/components/auth/AuthProvider";
import { useSavedItems } from "@/components/profile/SavedItemsProvider";
import type { ProductSummary } from "@/types/catalog";

type SaveProductButtonProps = {
  product: ProductSummary;
  className?: string;
  compact?: boolean;
};

export function SaveProductButton({
  product,
  className = "",
  compact = false,
}: SaveProductButtonProps) {
  const router = useRouter();
  const { user } = useAuth();
  const { isSaved, toggleSaved } = useSavedItems();
  const saved = isSaved(product.id);

  async function handleClick(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();

    if (!user) {
      router.push(`/login?from=/products/${product.slug}`);
      return;
    }

    await toggleSaved(product.id);
  }

  const label = saved ? "Remove from saved items" : "Save item";

  return (
    <button
      type="button"
      onClick={handleClick}
      aria-label={label}
      aria-pressed={saved}
      className={`inline-flex items-center justify-center rounded-full border border-border bg-card text-sm font-medium text-foreground transition hover:bg-card-hover ${
        compact ? "h-9 w-9" : "px-4 py-2.5"
      } ${saved ? "border-rose-500/30 bg-rose-500/10 text-rose-600 dark:text-rose-300" : ""} ${className}`}
    >
      {compact ? (
        <svg width="16" height="16" viewBox="0 0 24 24" fill={saved ? "currentColor" : "none"} aria-hidden>
          <path
            d="M12 20.5 4.5 12.9a4.9 4.9 0 0 1 0-6.9 4.9 4.9 0 0 1 6.9 0L12 6.6l.6-.6a4.9 4.9 0 0 1 6.9 0 4.9 4.9 0 0 1 0 6.9L12 20.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
        </svg>
      ) : (
        label
      )}
    </button>
  );
}

export function SaveProductLink({ product }: { product: ProductSummary }) {
  const { user } = useAuth();

  if (!user) {
    return (
      <Link
        href={`/login?from=/products/${product.slug}`}
        className="text-sm font-medium text-muted-foreground transition hover:text-foreground"
      >
        Sign in to save
      </Link>
    );
  }

  return <SaveProductButton product={product} />;
}
