"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

import { useAuth } from "@/components/auth/AuthProvider";
import { useCart } from "@/components/cart/CartProvider";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { SearchBar } from "@/components/ui/SearchBar";
import type { Category } from "@/types/catalog";

type SiteHeaderProps = {
  categories?: Category[];
};

export function SiteHeader({ categories = [] }: SiteHeaderProps) {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const { cart } = useCart();
  const [mobileOpen, setMobileOpen] = useState(false);
  const isAuthPage = pathname === "/login" || pathname === "/register";

  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileOpen]);

  return (
    <>
      <header className="sticky top-0 z-50 border-b border-border/80 bg-background/85 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3 sm:px-6 sm:py-4">
          <div className="flex min-w-0 items-center gap-3">
            {!isAuthPage && (
              <button
                type="button"
                className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-border bg-card text-foreground transition hover:bg-card-hover md:hidden"
                onClick={() => setMobileOpen(true)}
                aria-label="Open menu"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
                  <path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </button>
            )}
            <Link
              href="/"
              className="shrink-0 text-lg font-semibold tracking-tight text-foreground transition hover:opacity-80"
            >
              CommerceAI
            </Link>
          </div>

          {!isAuthPage && (
            <div className="hidden flex-1 justify-center px-4 md:flex">
              <SearchBar className="w-full max-w-lg" />
            </div>
          )}

          <div className="ml-auto flex items-center gap-2 sm:gap-3">
            <ThemeToggle />

            {!isAuthPage && (
              <Link
                href={user ? "/cart" : "/login?from=/cart"}
                className="relative inline-flex h-10 w-10 items-center justify-center rounded-full border border-border bg-card text-foreground transition hover:bg-card-hover"
                aria-label="Cart"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
                  <path
                    d="M6 6h15l-1.5 9h-12L6 6Z"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeLinejoin="round"
                  />
                  <path
                    d="M6 6 5 3H2"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                  />
                  <circle cx="9" cy="20" r="1" fill="currentColor" />
                  <circle cx="18" cy="20" r="1" fill="currentColor" />
                </svg>
                {user && cart.itemCount > 0 && (
                  <span className="absolute -right-1 -top-1 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1 text-[10px] font-semibold text-accent-foreground animate-scale-in">
                    {cart.itemCount}
                  </span>
                )}
              </Link>
            )}

            {user ? (
              <>
                <Link
                  href={user.role === "ADMIN" ? "/admin" : "/dashboard"}
                  className="hidden rounded-full border border-border px-4 py-2 text-sm font-medium text-foreground transition hover:bg-card-hover sm:inline-flex"
                >
                  Account
                </Link>
                <button
                  type="button"
                  onClick={logout}
                  className="hidden rounded-full border border-border px-4 py-2 text-sm font-medium text-foreground transition hover:bg-card-hover sm:inline-flex"
                >
                  Sign out
                </button>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="hidden text-sm font-medium text-muted-foreground transition hover:text-foreground sm:inline"
                >
                  Sign in
                </Link>
                <Link
                  href="/register"
                  className="rounded-full bg-accent px-4 py-2 text-sm font-medium text-accent-foreground transition hover:opacity-90"
                >
                  Join
                </Link>
              </>
            )}
          </div>
        </div>

        {!isAuthPage && (
          <div className="border-t border-border/70 px-4 py-3 md:hidden sm:px-6">
            <SearchBar />
          </div>
        )}

        {!isAuthPage && (
          <nav className="hidden border-t border-border/70 md:block">
            <div className="mx-auto flex max-w-7xl items-center gap-6 overflow-x-auto px-6 py-3 text-sm">
              <Link
                href="/products"
                className="shrink-0 font-medium text-muted-foreground transition hover:text-foreground"
              >
                Shop All
              </Link>
              {categories.map((category) => (
                <Link
                  key={category.id}
                  href={`/categories/${category.slug}`}
                  className="shrink-0 font-medium text-muted-foreground transition hover:text-foreground"
                >
                  {category.name}
                </Link>
              ))}
            </div>
          </nav>
        )}
      </header>

      {mobileOpen && !isAuthPage && (
        <div className="fixed inset-0 z-[60] md:hidden">
          <button
            type="button"
            className="absolute inset-0 bg-black/50 backdrop-blur-sm animate-fade-in"
            onClick={() => setMobileOpen(false)}
            aria-label="Close menu"
          />
          <div className="absolute inset-y-0 left-0 w-[min(88vw,320px)] border-r border-border bg-background p-6 shadow-2xl animate-fade-in-up">
            <div className="mb-8 flex items-center justify-between">
              <p className="text-lg font-semibold text-foreground">Menu</p>
              <button
                type="button"
                onClick={() => setMobileOpen(false)}
                className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-border"
                aria-label="Close menu"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
                  <path d="M6 6l12 12M18 6 6 18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </button>
            </div>
            <div className="space-y-1">
              <MobileLink href="/products" onNavigate={() => setMobileOpen(false)}>
                Shop All
              </MobileLink>
              {categories.map((category) => (
                <MobileLink
                  key={category.id}
                  href={`/categories/${category.slug}`}
                  onNavigate={() => setMobileOpen(false)}
                >
                  {category.name}
                </MobileLink>
              ))}
              <MobileLink href="/orders" onNavigate={() => setMobileOpen(false)}>
                Orders
              </MobileLink>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

function MobileLink({
  href,
  children,
  onNavigate,
}: {
  href: string;
  children: React.ReactNode;
  onNavigate: () => void;
}) {
  return (
    <Link
      href={href}
      onClick={onNavigate}
      className="block rounded-xl px-3 py-3 text-base font-medium text-foreground transition hover:bg-card-hover"
    >
      {children}
    </Link>
  );
}
