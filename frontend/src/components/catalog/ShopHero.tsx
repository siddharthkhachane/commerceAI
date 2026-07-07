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
    <section className="relative overflow-hidden bg-hero text-white">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.14),transparent_42%)]" />
      <div className="absolute inset-0 bg-[linear-gradient(to_bottom,transparent,rgba(0,0,0,0.35))]" />
      <div className="relative mx-auto max-w-7xl px-4 py-20 sm:px-6 md:py-28">
        <p className="animate-fade-in-up text-sm font-medium uppercase tracking-[0.28em] text-white/55">
          {eyebrow}
        </p>
        <h1 className="animate-fade-in-up mt-4 max-w-3xl text-4xl font-semibold tracking-tight sm:text-5xl md:text-6xl md:leading-[1.05]">
          {title}
        </h1>
        <p
          className="animate-fade-in-up mt-6 max-w-2xl text-base leading-8 text-white/75 sm:text-lg"
          style={{ animationDelay: "0.08s" }}
        >
          {description}
        </p>
        <Link
          href={ctaHref}
          className="animate-fade-in-up mt-10 inline-flex rounded-full bg-white px-6 py-3.5 text-sm font-medium text-zinc-950 transition hover:scale-[1.02] hover:bg-zinc-100 active:scale-[0.98]"
          style={{ animationDelay: "0.14s" }}
        >
          {ctaLabel}
        </Link>
      </div>
    </section>
  );
}
