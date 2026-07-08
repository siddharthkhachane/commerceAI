"use client";

import Image from "next/image";
import { useCallback, useEffect, useState } from "react";

import { CategoryForm } from "@/components/admin/catalog/CategoryForm";
import { AdminPageHeader } from "@/components/admin/AdminShared";
import {
  createAdminCategory,
  deleteAdminCategory,
  getAdminCategories,
  updateAdminCategory,
} from "@/lib/admin-catalog";
import type { Category } from "@/types/catalog";

export function AdminCategoriesContent() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [editing, setEditing] = useState<Category | null>(null);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setError(null);
    try {
      setCategories(await getAdminCategories());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load categories.");
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <AdminPageHeader
          title="Category management"
          description="Organize the catalog with category names, descriptions, and cover images."
        />
        <button
          type="button"
          onClick={() => {
            setCreating(true);
            setEditing(null);
          }}
          className="h-11 rounded-xl bg-foreground px-4 text-sm font-medium text-background"
        >
          New category
        </button>
      </div>

      {creating ? (
        <CategoryForm
          onCancel={() => setCreating(false)}
          onSubmit={async (values) => {
            await createAdminCategory(values);
            setCreating(false);
            await loadData();
          }}
        />
      ) : null}

      {editing ? (
        <CategoryForm
          initial={editing}
          onCancel={() => setEditing(null)}
          onSubmit={async (values) => {
            await updateAdminCategory(editing.id, values);
            setEditing(null);
            await loadData();
          }}
        />
      ) : null}

      {error ? (
        <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>
      ) : null}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {categories.map((category) => (
          <article key={category.id} className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
            <div className="relative aspect-[3/2] bg-muted">
              {category.imageUrl ? (
                <Image src={category.imageUrl} alt={category.name} fill className="object-cover" />
              ) : null}
            </div>
            <div className="space-y-2 p-4">
              <h3 className="text-lg font-semibold text-foreground">{category.name}</h3>
              <p className="text-xs uppercase tracking-[0.14em] text-muted-foreground">{category.slug}</p>
              <p className="text-sm text-muted-foreground">{category.description}</p>
              <p className="text-xs text-muted-foreground">{category.productCount ?? 0} products</p>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setEditing(category);
                    setCreating(false);
                  }}
                  className="text-sm font-medium hover:underline"
                >
                  Edit
                </button>
                <button
                  type="button"
                  onClick={async () => {
                    await deleteAdminCategory(category.id);
                    await loadData();
                  }}
                  className="text-sm font-medium text-rose-600 hover:underline"
                >
                  Delete
                </button>
              </div>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
