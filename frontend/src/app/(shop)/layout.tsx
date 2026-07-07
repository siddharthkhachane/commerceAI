import { SiteFooter } from "@/components/layout/SiteFooter";
import { SiteHeader } from "@/components/layout/SiteHeader";
import { getCategories } from "@/lib/catalog";

export const dynamic = "force-dynamic";

export default async function ShopLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  let categories: Awaited<ReturnType<typeof getCategories>> = [];

  try {
    categories = await getCategories();
  } catch {
    categories = [];
  }

  return (
    <>
      <SiteHeader categories={categories} />
      <main className="flex-1">{children}</main>
      <SiteFooter />
    </>
  );
}
