"use client";

import { useCompare } from "@/components/compare/CompareProvider";
import type { ProductSummary } from "@/types/catalog";

type AddToCompareButtonProps = {
  product: ProductSummary;
  className?: string;
  compact?: boolean;
};

export function AddToCompareButton({
  product,
  className = "",
  compact = false,
}: AddToCompareButtonProps) {
  const { addItem, removeItem, isSelected, canAddMore } = useCompare();
  const selected = isSelected(product.id);

  function handleClick(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();

    if (selected) {
      removeItem(product.id);
      return;
    }

    if (canAddMore) {
      addItem(product);
    }
  }

  const label = selected
    ? "Remove from compare"
    : canAddMore
      ? "Add to compare"
      : "Compare list full";

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={!selected && !canAddMore}
      aria-label={label}
      aria-pressed={selected}
      className={`inline-flex items-center justify-center rounded-full border border-border bg-card text-sm font-medium text-foreground transition hover:bg-card-hover disabled:cursor-not-allowed disabled:opacity-50 ${
        compact ? "h-9 w-9" : "px-4 py-2.5"
      } ${selected ? "border-foreground/30 bg-foreground/5" : ""} ${className}`}
    >
      {compact ? (
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
          <path
            d="M8 7h12M8 12h12M8 17h8M4 7h.01M4 12h.01M4 17h.01"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </svg>
      ) : (
        label
      )}
    </button>
  );
}
