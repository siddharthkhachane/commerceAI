import Link from "next/link";

export function SiteFooter() {
  return (
    <footer className="border-t border-zinc-200 bg-zinc-50">
      <div className="mx-auto flex max-w-7xl flex-col gap-6 px-6 py-12 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-lg font-semibold tracking-tight text-zinc-950">CommerceAI</p>
          <p className="mt-2 max-w-md text-sm leading-6 text-zinc-600">
            Premium essentials with thoughtful design. Quality pieces for everyday life.
          </p>
        </div>
        <div className="flex gap-6 text-sm text-zinc-600">
          <Link href="/products" className="transition hover:text-zinc-950">
            Shop
          </Link>
          <Link href="/login" className="transition hover:text-zinc-950">
            Account
          </Link>
        </div>
      </div>
    </footer>
  );
}
