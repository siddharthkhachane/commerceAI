import Link from "next/link";

type ShopHeroProps = {
  eyebrow?: string;
  title: string;
  description: string;
  ctaLabel?: string;
  ctaHref?: string;
};

export function ShopHero({
  eyebrow = "CommerceAI",
  title,
  description,
  ctaLabel = "Shop the collection",
  ctaHref = "/products",
}: ShopHeroProps) {
  return (
    <section className="relative overflow-hidden bg-zinc-950 text-white">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.12),transparent_40%)]" />
      <div className="relative mx-auto max-w-7xl px-6 py-20 md:py-28">
        <p className="text-sm font-medium uppercase tracking-[0.24em] text-zinc-400">
          {eyebrow}
        </p>
        <h1 className="mt-4 max-w-3xl text-4xl font-semibold tracking-tight md:text-6xl">
          {title}
        </h1>
        <p className="mt-6 max-w-2xl text-lg leading-8 text-zinc-300">
          {description}
        </p>
        <Link
          href={ctaHref}
          className="mt-10 inline-flex rounded-full bg-white px-6 py-3 text-sm font-medium text-zinc-950 transition hover:bg-zinc-100"
        >
          {ctaLabel}
        </Link>
      </div>
    </section>
  );
}
