"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { useAuth } from "@/components/auth/AuthProvider";
import type { Category } from "@/types/catalog";

type SiteHeaderProps = {
  categories?: Category[];
};

export function SiteHeader({ categories = [] }: SiteHeaderProps) {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const isAuthPage = pathname === "/login" || pathname === "/register";

  return (
    <header className="sticky top-0 z-50 border-b border-zinc-200/80 bg-white/90 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-6 px-6 py-4">
        <div className="flex items-center gap-8">
          <Link href="/" className="text-lg font-semibold tracking-tight text-zinc-950">
            CommerceAI
          </Link>
          {!isAuthPage && (
            <nav className="hidden items-center gap-6 md:flex">
              <Link
                href="/products"
                className="text-sm font-medium text-zinc-600 transition hover:text-zinc-950"
              >
                Shop All
              </Link>
              {categories.slice(0, 6).map((category) => (
                <Link
                  key={category.id}
                  href={`/categories/${category.slug}`}
                  className="text-sm font-medium text-zinc-600 transition hover:text-zinc-950"
                >
                  {category.name}
                </Link>
              ))}
            </nav>
          )}
        </div>

        <div className="flex items-center gap-3">
          {user ? (
            <>
              <Link
                href={user.role === "ADMIN" ? "/admin" : "/dashboard"}
                className="hidden text-sm font-medium text-zinc-600 transition hover:text-zinc-950 sm:inline"
              >
                Account
              </Link>
              <button
                type="button"
                onClick={logout}
                className="rounded-full border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 transition hover:bg-zinc-50"
              >
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="text-sm font-medium text-zinc-600 transition hover:text-zinc-950"
              >
                Sign in
              </Link>
              <Link
                href="/register"
                className="rounded-full bg-zinc-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-zinc-800"
              >
                Join
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
