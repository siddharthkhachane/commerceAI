"use client";

import Image from "next/image";
import { useCallback, useEffect, useMemo, useState } from "react";

import { ProductForm } from "@/components/admin/catalog/ProductForm";
import { AdminPageHeader } from "@/components/admin/AdminShared";
import {
  bulkUpdateAdminProducts,
  createAdminProduct,
  deleteAdminProduct,
  getAdminCatalogProducts,
  getAdminCategories,
  updateAdminProduct,
} from "@/lib/admin-catalog";
import { formatPrice } from "@/lib/format";
import type { AdminProduct, AdminProductQuery } from "@/types/admin-catalog";
import type { Category } from "@/types/catalog";

export function AdminProductsContent() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [search, setSearch] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [active, setActive] = useState<"all" | "active" | "inactive">("all");
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [sort, setSort] = useState<AdminProductQuery["sort"]>("newest");
  const [editing, setEditing] = useState<AdminProduct | null>(null);
  const [creating, setCreating] = useState(false);
  const [bulkStockDelta, setBulkStockDelta] = useState("10");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const query = useMemo<AdminProductQuery>(
    () => ({
      search: search || undefined,
      categoryId: categoryId ? Number(categoryId) : undefined,
      active,
      lowStockOnly,
      sort,
      page: 0,
      size: 50,
    }),
    [search, categoryId, active, lowStockOnly, sort],
  );

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [categoryData, productData] = await Promise.all([
        getAdminCategories(),
        getAdminCatalogProducts(query),
      ]);
      setCategories(categoryData);
      setProducts(productData.content);
      setSelectedIds((current) => current.filter((id) => productData.content.some((p) => p.id === id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load products.");
    } finally {
      setLoading(false);
    }
  }, [query]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  function toggleSelected(id: number) {
    setSelectedIds((current) =>
      current.includes(id) ? current.filter((value) => value !== id) : [...current, id],
    );
  }

  async function handleBulkStockIncrease() {
    if (selectedIds.length === 0) return;
    await bulkUpdateAdminProducts({
      productIds: selectedIds,
      stockDelta: Number(bulkStockDelta),
    });
    setSelectedIds([]);
    await loadData();
  }

  async function handleBulkDeactivate() {
    if (selectedIds.length === 0) return;
    await bulkUpdateAdminProducts({
      productIds: selectedIds,
      isActive: false,
    });
    setSelectedIds([]);
    await loadData();
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <AdminPageHeader
          title="Product management"
          description="Create, edit, search, and bulk-manage products with images, pricing, and inventory."
        />
        <button
          type="button"
          onClick={() => {
            setCreating(true);
            setEditing(null);
          }}
          className="h-11 rounded-xl bg-foreground px-4 text-sm font-medium text-background"
        >
          New product
        </button>
      </div>

      <div className="grid gap-3 rounded-2xl border border-border bg-card p-4 md:grid-cols-2 xl:grid-cols-5">
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search products..."
          className="h-11 rounded-xl border border-input bg-background px-4 text-sm outline-none"
        />
        <select
          value={categoryId}
          onChange={(event) => setCategoryId(event.target.value)}
          className="h-11 rounded-xl border border-input bg-background px-4 text-sm"
        >
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <select
          value={active}
          onChange={(event) => setActive(event.target.value as "all" | "active" | "inactive")}
          className="h-11 rounded-xl border border-input bg-background px-4 text-sm"
        >
          <option value="all">All statuses</option>
          <option value="active">Active only</option>
          <option value="inactive">Inactive only</option>
        </select>
        <select
          value={sort}
          onChange={(event) => setSort(event.target.value as AdminProductQuery["sort"])}
          className="h-11 rounded-xl border border-input bg-background px-4 text-sm"
        >
          <option value="newest">Newest</option>
          <option value="name-asc">Name A-Z</option>
          <option value="price-asc">Price low-high</option>
          <option value="price-desc">Price high-low</option>
          <option value="stock-asc">Stock low-high</option>
          <option value="stock-desc">Stock high-low</option>
        </select>
        <label className="flex h-11 items-center gap-2 rounded-xl border border-input bg-background px-4 text-sm">
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(event) => setLowStockOnly(event.target.checked)}
          />
          Low stock only
        </label>
      </div>

      {selectedIds.length > 0 ? (
        <div className="flex flex-wrap items-center gap-3 rounded-2xl border border-border bg-muted/40 p-4">
          <p className="text-sm font-medium text-foreground">{selectedIds.length} selected</p>
          <input
            value={bulkStockDelta}
            onChange={(event) => setBulkStockDelta(event.target.value)}
            className="h-10 w-24 rounded-xl border border-input bg-background px-3 text-sm"
          />
          <button
            type="button"
            onClick={handleBulkStockIncrease}
            className="rounded-xl bg-foreground px-4 py-2 text-sm font-medium text-background"
          >
            Bulk add stock
          </button>
          <button
            type="button"
            onClick={handleBulkDeactivate}
            className="rounded-xl border border-border px-4 py-2 text-sm font-medium"
          >
            Bulk deactivate
          </button>
        </div>
      ) : null}

      {creating ? (
        <ProductForm
          categories={categories}
          onCancel={() => setCreating(false)}
          onSubmit={async (values) => {
            await createAdminProduct(values);
            setCreating(false);
            await loadData();
          }}
        />
      ) : null}

      {editing ? (
        <ProductForm
          categories={categories}
          initial={editing}
          onCancel={() => setEditing(null)}
          onSubmit={async (values) => {
            await updateAdminProduct(editing.id, values);
            setEditing(null);
            await loadData();
          }}
        />
      ) : null}

      {error ? (
        <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading products...</p>
      ) : (
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
                <th className="px-4 py-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-b border-border/70 last:border-0">
                  <td className="px-4 py-4">
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(product.id)}
                      onChange={() => toggleSelected(product.id)}
                    />
                  </td>
                  <td className="px-4 py-4">
                    <div className="flex items-center gap-3">
                      <div className="relative h-12 w-10 overflow-hidden rounded-lg bg-muted">
                        <Image src={product.imageUrl} alt={product.name} fill className="object-cover" />
                      </div>
                      <div>
                        <p className="font-medium text-foreground">{product.name}</p>
                        <p className="text-xs text-muted-foreground">{product.slug}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-4">{product.category.name}</td>
                  <td className="px-4 py-4 font-medium">{formatPrice(product.price)}</td>
                  <td className="px-4 py-4">{product.stockQuantity}</td>
                  <td className="px-4 py-4">
                    <span className="rounded-full bg-muted px-2.5 py-1 text-xs font-medium">
                      {product.isActive ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="px-4 py-4">
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => {
                          setEditing(product);
                          setCreating(false);
                        }}
                        className="text-sm font-medium hover:underline"
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        onClick={async () => {
                          await deleteAdminProduct(product.id);
                          await loadData();
                        }}
                        className="text-sm font-medium text-rose-600 hover:underline"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
