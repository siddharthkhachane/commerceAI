import Link from "next/link";

type AuthLayoutProps = {
  title: string;
  subtitle: string;
  children: React.ReactNode;
  footer: React.ReactNode;
};

export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="flex flex-1 items-center justify-center bg-background px-4 py-16 sm:px-6">
      <div className="w-full max-w-md animate-fade-in-up space-y-8">
        <div className="space-y-2 text-center">
          <Link
            href="/"
            className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground transition hover:text-foreground"
          >
            CommerceAI
          </Link>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            {title}
          </h1>
          <p className="text-sm text-muted-foreground">{subtitle}</p>
        </div>

        <div className="rounded-2xl border border-border bg-card p-8 shadow-sm">
          {children}
        </div>

        <div className="text-center text-sm text-muted-foreground">{footer}</div>
      </div>
    </div>
  );
}
