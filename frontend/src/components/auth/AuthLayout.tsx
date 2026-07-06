import Link from "next/link";

type AuthLayoutProps = {
  title: string;
  subtitle: string;
  children: React.ReactNode;
  footer: React.ReactNode;
};

export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="flex flex-1 items-center justify-center px-6 py-16">
      <div className="w-full max-w-md space-y-8">
        <div className="space-y-2 text-center">
          <Link href="/" className="text-sm font-medium uppercase tracking-[0.2em] text-zinc-500">
            CommerceAI
          </Link>
          <h1 className="text-3xl font-semibold tracking-tight text-zinc-950">
            {title}
          </h1>
          <p className="text-sm text-zinc-600">{subtitle}</p>
        </div>

        <div className="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
          {children}
        </div>

        <div className="text-center text-sm text-zinc-600">{footer}</div>
      </div>
    </div>
  );
}
