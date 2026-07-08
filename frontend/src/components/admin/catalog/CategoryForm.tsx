"use client";

import Image from "next/image";
import { FormEvent, useState } from "react";

import { AdminField, AdminPanel, AdminTextArea } from "@/components/admin/catalog/AdminFormFields";
import type { Category } from "@/types/catalog";

type CategoryFormProps = {
  initial?: Category | null;
  onSubmit: (values: { name: string; description?: string; imageUrl?: string }) => Promise<void>;
  onCancel: () => void;
};

export function CategoryForm({ initial, onSubmit, onCancel }: CategoryFormProps) {
  const [name, setName] = useState(initial?.name ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [imageUrl, setImageUrl] = useState(
    initial?.imageUrl ?? "https://picsum.photos/seed/category/1200/800",
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);

    try {
      await onSubmit({
        name,
        description: description || undefined,
        imageUrl: imageUrl || undefined,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not save category.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <AdminPanel title={initial ? "Edit category" : "Create category"}>
      <form onSubmit={handleSubmit} className="grid gap-4 lg:grid-cols-2">
        <div className="space-y-4">
          <AdminField label="Name" value={name} onChange={setName} required />
          <AdminTextArea label="Description" value={description} onChange={setDescription} />
          <AdminField
            label="Image URL"
            value={imageUrl}
            onChange={setImageUrl}
            type="url"
            placeholder="https://picsum.photos/seed/category/1200/800"
          />
        </div>

        <div className="overflow-hidden rounded-2xl border border-border bg-muted">
          <div className="relative aspect-[3/2]">
            {imageUrl ? (
              <Image src={imageUrl} alt={name || "Category preview"} fill className="object-cover" />
            ) : null}
          </div>
        </div>

        {error ? <p className="lg:col-span-2 text-sm text-rose-600">{error}</p> : null}

        <div className="flex gap-2 lg:col-span-2">
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-foreground px-4 py-2.5 text-sm font-medium text-background disabled:opacity-60"
          >
            {saving ? "Saving..." : initial ? "Update category" : "Create category"}
          </button>
          <button type="button" onClick={onCancel} className="rounded-xl border border-border px-4 py-2.5 text-sm font-medium">
            Cancel
          </button>
        </div>
      </form>
    </AdminPanel>
  );
}
