"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";

import { useAuth } from "@/components/auth/AuthProvider";

const navItems = [
  { href: "/admin", label: "Dashboard" },
  { href: "/admin/assistant", label: "AI Assistant" },
  { href: "/admin/orders", label: "Orders" },
  { href: "/admin/products", label: "Products" },
  { href: "/admin/categories", label: "Categories" },
  { href: "/admin/inventory", label: "Inventory" },
];

export function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();

  function handleLogout() {
    logout();
    router.push("/admin/login");
  }

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="mx-auto flex min-h-screen max-w-7xl">
        <aside className="hidden w-64 shrink-0 border-r border-border bg-card p-6 lg:block">
          <div className="mb-10">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
              CommerceAI
            </p>
            <h1 className="mt-2 text-2xl font-semibold text-foreground">Admin</h1>
          </div>

          <nav className="space-y-1">
            {navItems.map((item) => {
              const active = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`block rounded-xl px-4 py-2.5 text-sm font-medium transition ${
                    active
                      ? "bg-foreground text-background"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>

          <div className="mt-10 border-t border-border pt-6">
            <p className="text-sm font-medium text-foreground">{user?.fullName}</p>
            <p className="text-xs text-muted-foreground">{user?.email}</p>
            <div className="mt-4 flex flex-col gap-2">
              <Link
                href="/"
                className="inline-flex rounded-xl border border-border bg-background px-4 py-2.5 text-center text-sm font-medium text-foreground transition hover:bg-muted"
              >
                Back to storefront
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                className="text-left text-sm text-muted-foreground transition hover:text-foreground"
              >
                Sign out
              </button>
            </div>
          </div>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="border-b border-border bg-card px-4 py-4 sm:px-6 lg:hidden">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.16em] text-muted-foreground">Admin</p>
                <p className="text-sm font-medium text-foreground">{user?.fullName}</p>
              </div>
              <div className="flex items-center gap-2">
                <Link
                  href="/"
                  className="rounded-full border border-border px-3 py-1.5 text-xs font-medium text-foreground transition hover:bg-muted"
                >
                  Storefront
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-full border border-border px-3 py-1.5 text-xs font-medium"
                >
                  Sign out
                </button>
              </div>
            </div>
            <div className="mt-4 flex gap-2 overflow-x-auto">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-medium ${
                    pathname === item.href
                      ? "bg-foreground text-background"
                      : "bg-muted text-muted-foreground"
                  }`}
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </header>

          <main className="flex-1 px-4 py-6 sm:px-6 sm:py-8">{children}</main>
        </div>
      </div>
    </div>
  );
}
