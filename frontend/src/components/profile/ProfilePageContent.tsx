"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { useSavedItems } from "@/components/profile/SavedItemsProvider";
import { formatPrice } from "@/lib/format";
import { getCategoriesClient } from "@/lib/catalog";
import * as ordersApi from "@/lib/orders";
import * as profileApi from "@/lib/profile";
import type { Order } from "@/types/cart";
import type { Category } from "@/types/catalog";
import type { SavedProduct, UserProfile } from "@/types/profile";

type ProfileTab = "overview" | "preferences" | "orders" | "saved";

const tabs: { id: ProfileTab; label: string }[] = [
  { id: "overview", label: "Overview" },
  { id: "preferences", label: "Preferences" },
  { id: "orders", label: "Order History" },
  { id: "saved", label: "Favorites" },
];

export function ProfilePageContent() {
  const { user, refreshUser } = useAuth();
  const { refresh: refreshSavedItems } = useSavedItems();
  const [activeTab, setActiveTab] = useState<ProfileTab>("overview");
  const [categories, setCategories] = useState<Category[]>([]);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [savedItems, setSavedItems] = useState<SavedProduct[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [preferencesForm, setPreferencesForm] = useState({
    fullName: "",
    stylePreference: "",
    preferredCategorySlug: "",
    budgetPreference: "",
    emailNotifications: true,
  });

  useEffect(() => {
    getCategoriesClient().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    Promise.all([profileApi.getProfile(), ordersApi.getOrders(), profileApi.getSavedItems()])
      .then(([profileData, orderData, savedData]) => {
        setProfile(profileData);
        setOrders(orderData);
        setSavedItems(savedData);
        setPreferencesForm({
          fullName: profileData.fullName,
          stylePreference: profileData.preferences.stylePreference ?? "",
          preferredCategorySlug: profileData.preferences.preferredCategorySlug ?? "",
          budgetPreference:
            profileData.preferences.budgetPreference !== null
              ? String(profileData.preferences.budgetPreference)
              : "",
          emailNotifications: profileData.preferences.emailNotifications,
        });
      })
      .catch(() => setError("Could not load your profile."));
  }, []);

  async function handlePreferencesSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);

    try {
      const updated = await profileApi.updateProfile({
        fullName: preferencesForm.fullName.trim(),
        stylePreference: preferencesForm.stylePreference.trim() || null,
        preferredCategorySlug: preferencesForm.preferredCategorySlug || null,
        budgetPreference: preferencesForm.budgetPreference
          ? Number(preferencesForm.budgetPreference)
          : null,
        emailNotifications: preferencesForm.emailNotifications,
      });
      setProfile(updated);
      await refreshUser();
    } catch {
      setError("Could not save preferences.");
    } finally {
      setSaving(false);
    }
  }

  async function handleRemoveSaved(productId: number) {
    await profileApi.removeSavedItem(productId);
    setSavedItems((current) => current.filter((item) => item.product.id !== productId));
    await refreshSavedItems();
    if (profile) {
      setProfile({ ...profile, savedItemCount: Math.max(0, profile.savedItemCount - 1) });
    }
  }

  if (!user) {
    return null;
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap gap-2">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            className={`rounded-full px-4 py-2 text-sm font-medium transition ${
              activeTab === tab.id
                ? "bg-foreground text-background"
                : "border border-border bg-card text-foreground hover:bg-card-hover"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {error && <p className="text-sm text-red-600">{error}</p>}

      {activeTab === "overview" && profile && (
        <section className="grid gap-4 sm:grid-cols-3">
          <StatCard label="Orders" value={String(profile.orderCount)} />
          <StatCard label="Saved items" value={String(profile.savedItemCount)} />
          <StatCard label="Account status" value="Active" />
          <div className="sm:col-span-3 rounded-2xl border border-border bg-card p-6">
            <h2 className="text-lg font-semibold text-foreground">Account</h2>
            <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
              <div>
                <dt className="text-muted-foreground">Name</dt>
                <dd className="mt-1 font-medium text-foreground">{profile.fullName}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Email</dt>
                <dd className="mt-1 font-medium text-foreground">{profile.email}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Role</dt>
                <dd className="mt-1 font-medium text-foreground">{profile.role}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Shopping style</dt>
                <dd className="mt-1 font-medium text-foreground">
                  {profile.preferences.stylePreference ?? "Not set"}
                </dd>
              </div>
            </dl>
          </div>
        </section>
      )}

      {activeTab === "preferences" && (
        <section className="rounded-2xl border border-border bg-card p-6">
          <h2 className="text-lg font-semibold text-foreground">Shopping preferences</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Personalize recommendations and assistant suggestions.
          </p>
          <form onSubmit={handlePreferencesSubmit} className="mt-6 space-y-5">
            <Field label="Display name">
              <input
                value={preferencesForm.fullName}
                onChange={(event) =>
                  setPreferencesForm((current) => ({ ...current, fullName: event.target.value }))
                }
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm text-foreground"
                required
              />
            </Field>
            <Field label="Style preference">
              <input
                value={preferencesForm.stylePreference}
                onChange={(event) =>
                  setPreferencesForm((current) => ({
                    ...current,
                    stylePreference: event.target.value,
                  }))
                }
                placeholder="e.g. minimal, classic, sporty"
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm text-foreground"
              />
            </Field>
            <Field label="Preferred category">
              <select
                value={preferencesForm.preferredCategorySlug}
                onChange={(event) =>
                  setPreferencesForm((current) => ({
                    ...current,
                    preferredCategorySlug: event.target.value,
                  }))
                }
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm text-foreground"
              >
                <option value="">No preference</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.slug}>
                    {category.name}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Typical budget (USD)">
              <input
                type="number"
                min="0"
                step="0.01"
                value={preferencesForm.budgetPreference}
                onChange={(event) =>
                  setPreferencesForm((current) => ({
                    ...current,
                    budgetPreference: event.target.value,
                  }))
                }
                placeholder="150"
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm text-foreground"
              />
            </Field>
            <label className="flex items-center gap-3 text-sm text-foreground">
              <input
                type="checkbox"
                checked={preferencesForm.emailNotifications}
                onChange={(event) =>
                  setPreferencesForm((current) => ({
                    ...current,
                    emailNotifications: event.target.checked,
                  }))
                }
                className="h-4 w-4 rounded border-border"
              />
              Email me about order updates and promotions
            </label>
            <button
              type="submit"
              disabled={saving}
              className="rounded-full bg-accent px-5 py-2.5 text-sm font-medium text-accent-foreground transition hover:opacity-90 disabled:opacity-60"
            >
              {saving ? "Saving…" : "Save preferences"}
            </button>
          </form>
        </section>
      )}

      {activeTab === "orders" && (
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-foreground">Order history</h2>
            <Link href="/orders" className="text-sm font-medium text-muted-foreground hover:text-foreground">
              View all
            </Link>
          </div>
          {orders.length === 0 ? (
            <p className="text-sm text-muted-foreground">No orders yet.</p>
          ) : (
            orders.slice(0, 5).map((order) => (
              <Link
                key={order.id}
                href={`/orders/${order.id}`}
                className="block rounded-2xl border border-border bg-card p-5 transition hover:bg-card-hover"
              >
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-medium text-foreground">Order #{order.id}</p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      {new Date(order.createdAt).toLocaleString()}
                    </p>
                  </div>
                  <p className="text-sm font-medium text-foreground">
                    {formatPrice(order.totalAmount)}
                  </p>
                </div>
              </Link>
            ))
          )}
        </section>
      )}

      {activeTab === "saved" && (
        <section className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold text-foreground">Favorites & saved items</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Products you have saved for later.
            </p>
          </div>
          {savedItems.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              No saved items yet. Tap the heart on any product to save it.
            </p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {savedItems.map((item) => (
                <article
                  key={item.product.id}
                  className="flex gap-4 rounded-2xl border border-border bg-card p-4"
                >
                  <Link href={`/products/${item.product.slug}`} className="relative h-24 w-20 shrink-0 overflow-hidden rounded-xl bg-muted">
                    <Image
                      src={item.product.imageUrl}
                      alt={item.product.name}
                      fill
                      sizes="80px"
                      className="object-cover"
                    />
                  </Link>
                  <div className="min-w-0 flex-1">
                    <Link
                      href={`/products/${item.product.slug}`}
                      className="line-clamp-2 text-sm font-medium text-foreground hover:underline"
                    >
                      {item.product.name}
                    </Link>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {formatPrice(item.product.price)}
                    </p>
                    <button
                      type="button"
                      onClick={() => void handleRemoveSaved(item.product.id)}
                      className="mt-3 text-xs font-medium text-rose-600 transition hover:text-rose-500"
                    >
                      Remove
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      )}
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-border bg-card p-5">
      <p className="text-xs font-medium uppercase tracking-[0.2em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-2xl font-semibold text-foreground">{value}</p>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block space-y-2 text-sm">
      <span className="font-medium text-foreground">{label}</span>
      {children}
    </label>
  );
}
