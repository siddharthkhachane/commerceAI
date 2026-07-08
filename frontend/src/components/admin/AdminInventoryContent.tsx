"use client";

import { useCallback, useEffect, useState } from "react";

import { AdminPageHeader } from "@/components/admin/AdminShared";
import { bulkUpdateAdminProducts, getAdminCatalogProducts } from "@/lib/admin-catalog";
import { formatPrice } from "@/lib/format";
import type { AdminProduct } from "@/types/admin-catalog";

export function AdminInventoryContent() {
  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [search, setSearch] = useState("");
  const [lowStockOnly, setLowStockOnly] = useState(true);
  const [setStockValue, setSetStockValue] = useState("25");
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setError(null);
    try {
      const response = await getAdminCatalogProducts({
        search: search || undefined,
        lowStockOnly,
        sort: "stock-asc",
        page: 0,
        size: 100,
      });
      setProducts(response.content);
      setSelectedIds((current) => current.filter((id) => response.content.some((p) => p.id === id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load inventory.");
    }
  }, [search, lowStockOnly]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  function toggleSelected(id: number) {
    setSelectedIds((current) =>
      current.includes(id) ? current.filter((value) => value !== id) : [...current, id],
    );
  }

  async function handleBulkSetStock() {
    if (selectedIds.length === 0) return;
    await bulkUpdateAdminProducts({
      productIds: selectedIds,
      stockQuantity: Number(setStockValue),
    });
    setSelectedIds([]);
    await loadData();
  }

  return (
    <div className="space-y-6">
      <AdminPageHeader
        title="Inventory management"
        description="Monitor stock levels, filter low inventory, and bulk update quantities."
      />

      <div className="grid gap-3 rounded-2xl border border-border bg-card p-4 md:grid-cols-3">
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search inventory..."
          className="h-11 rounded-xl border border-input bg-background px-4 text-sm outline-none"
        />
        <label className="flex h-11 items-center gap-2 rounded-xl border border-input bg-background px-4 text-sm">
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(event) => setLowStockOnly(event.target.checked)}
          />
          Show low stock only
        </label>
        <button
          type="button"
          onClick={loadData}
          className="h-11 rounded-xl border border-border px-4 text-sm font-medium"
        >
          Refresh
        </button>
      </div>

      {selectedIds.length > 0 ? (
        <div className="flex flex-wrap items-center gap-3 rounded-2xl border border-border bg-muted/40 p-4">
          <p className="text-sm font-medium text-foreground">{selectedIds.length} selected</p>
          <input
            value={setStockValue}
            onChange={(event) => setSetStockValue(event.target.value)}
            className="h-10 w-24 rounded-xl border border-input bg-background px-3 text-sm"
          />
          <button
            type="button"
            onClick={handleBulkSetStock}
            className="rounded-xl bg-foreground px-4 py-2 text-sm font-medium text-background"
          >
            Bulk set stock
          </button>
        </div>
      ) : null}

      {error ? (
        <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>
      ) : null}

      <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
        <table className="min-w-full text-left text-sm">
          <thead className="border-b border-border bg-muted/40 text-muted-foreground">
            <tr>
              <th className="px-4 py-3 font-medium">Select</th>
              <th className="px-4 py-3 font-medium">Product</th>
              <th className="px-4 py-3 font-medium">Category</th>
              <th className="px-4 py-3 font-medium">Price</th>
              <th className="px-4 py-3 font-medium">Stock</th>
              <th className="px-4 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => {
              const isLowStock = product.stockQuantity < 15;
              return (
                <tr key={product.id} className="border-b border-border/70 last:border-0">
                  <td className="px-4 py-4">
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(product.id)}
                      onChange={() => toggleSelected(product.id)}
                    />
                  </td>
                  <td className="px-4 py-4 font-medium text-foreground">{product.name}</td>
                  <td className="px-4 py-4">{product.category.name}</td>
                  <td className="px-4 py-4">{formatPrice(product.price)}</td>
                  <td className="px-4 py-4">{product.stockQuantity}</td>
                  <td className="px-4 py-4">
                    <span
                      className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                        isLowStock
                          ? "bg-amber-500/10 text-amber-700 dark:text-amber-300"
                          : "bg-emerald-500/10 text-emerald-700 dark:text-emerald-300"
                      }`}
                    >
                      {isLowStock ? "Low stock" : "Healthy"}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
