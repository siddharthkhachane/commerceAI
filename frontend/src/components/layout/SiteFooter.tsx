import Link from "next/link";

import { ThemeToggle } from "@/components/theme/ThemeToggle";

const shopLinks = [
  { href: "/products", label: "All products" },
  { href: "/cart", label: "Cart" },
  { href: "/orders", label: "Orders" },
];

const companyLinks = [
  { href: "/login", label: "Account" },
  { href: "/register", label: "Create account" },
];

export function SiteFooter() {
  return (
    <footer className="mt-auto border-t border-border bg-muted/50">
      <div className="mx-auto max-w-7xl px-4 py-14 sm:px-6">
        <div className="grid gap-10 md:grid-cols-2 lg:grid-cols-4">
          <div className="lg:col-span-2">
            <p className="text-xl font-semibold tracking-tight text-foreground">CommerceAI</p>
            <p className="mt-3 max-w-md text-sm leading-7 text-muted-foreground">
              Premium essentials with thoughtful design. Quality pieces for everyday life,
              curated with the calm confidence of a modern retailer.
            </p>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
              Shop
            </p>
            <ul className="mt-4 space-y-3">
              {shopLinks.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-sm text-foreground/80 transition hover:text-foreground"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
              Account
            </p>
            <ul className="mt-4 space-y-3">
              {companyLinks.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-sm text-foreground/80 transition hover:text-foreground"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="mt-12 flex flex-col items-start justify-between gap-4 border-t border-border pt-8 sm:flex-row sm:items-center">
          <p className="text-xs text-muted-foreground">
            © {new Date().getFullYear()} CommerceAI. All rights reserved.
          </p>
          <ThemeToggle />
        </div>
      </div>
    </footer>
  );
}
