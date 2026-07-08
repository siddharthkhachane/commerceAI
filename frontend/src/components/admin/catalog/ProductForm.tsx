"use client";

import Image from "next/image";
import { FormEvent, useEffect, useState } from "react";

import { AdminField, AdminPanel, AdminSelect, AdminTextArea } from "@/components/admin/catalog/AdminFormFields";
import type { AdminProduct } from "@/types/admin-catalog";
import type { Category } from "@/types/catalog";

type ProductFormProps = {
  categories: Category[];
  initial?: AdminProduct | null;
  onSubmit: (values: {
    name: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
    stockQuantity: number;
    isActive: boolean;
  }) => Promise<void>;
  onCancel: () => void;
};

export function ProductForm({ categories, initial, onSubmit, onCancel }: ProductFormProps) {
  const [name, setName] = useState(initial?.name ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [price, setPrice] = useState(String(initial?.price ?? ""));
  const [imageUrl, setImageUrl] = useState(
    initial?.imageUrl ?? "https://picsum.photos/seed/product/900/1200",
  );
  const [categoryId, setCategoryId] = useState(String(initial?.category.id ?? categories[0]?.id ?? ""));
  const [stockQuantity, setStockQuantity] = useState(String(initial?.stockQuantity ?? "0"));
  const [isActive, setIsActive] = useState(initial?.isActive ?? true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!initial && categories[0] && !categoryId) {
      setCategoryId(String(categories[0].id));
    }
  }, [categories, categoryId, initial]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);

    try {
      await onSubmit({
        name,
        description,
        price: Number(price),
        imageUrl,
        categoryId: Number(categoryId),
        stockQuantity: Number(stockQuantity),
        isActive,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not save product.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <AdminPanel title={initial ? "Edit product" : "Create product"}>
      <form onSubmit={handleSubmit} className="grid gap-4 lg:grid-cols-2">
        <div className="space-y-4">
          <AdminField label="Name" value={name} onChange={setName} required />
          <AdminTextArea label="Description" value={description} onChange={setDescription} />
          <AdminField label="Price" value={price} onChange={setPrice} type="number" required />
          <AdminField
            label="Image URL"
            value={imageUrl}
            onChange={setImageUrl}
            type="url"
            placeholder="https://picsum.photos/seed/product/900/1200"
            required
          />
        </div>

        <div className="space-y-4">
          <AdminSelect
            label="Category"
            value={categoryId}
            onChange={setCategoryId}
            options={categories.map((category) => ({
              value: String(category.id),
              label: category.name,
            }))}
          />
          <AdminField label="Stock quantity" value={stockQuantity} onChange={setStockQuantity} type="number" />
          <label className="flex items-center gap-3 text-sm text-foreground">
            <input
              type="checkbox"
              checked={isActive}
              onChange={(event) => setIsActive(event.target.checked)}
              className="h-4 w-4 rounded border-border"
            />
            Active on storefront
          </label>

          <div className="overflow-hidden rounded-2xl border border-border bg-muted">
            <div className="relative aspect-[4/5] max-h-72">
              {imageUrl ? (
                <Image src={imageUrl} alt={name || "Product preview"} fill className="object-cover" />
              ) : null}
            </div>
          </div>
        </div>

        {error ? <p className="lg:col-span-2 text-sm text-rose-600">{error}</p> : null}

        <div className="flex gap-2 lg:col-span-2">
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-foreground px-4 py-2.5 text-sm font-medium text-background disabled:opacity-60"
          >
            {saving ? "Saving..." : initial ? "Update product" : "Create product"}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="rounded-xl border border-border px-4 py-2.5 text-sm font-medium"
          >
            Cancel
          </button>
        </div>
      </form>
    </AdminPanel>
  );
}
