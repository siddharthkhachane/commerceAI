import type { Metadata } from "next";
import { notFound } from "next/navigation";

import { ProductDetailView } from "@/components/catalog/ProductDetailView";
import { getProduct } from "@/lib/catalog";

type ProductPageProps = {
  params: Promise<{ slug: string }>;
};

export async function generateMetadata({
  params,
}: ProductPageProps): Promise<Metadata> {
  try {
    const { slug } = await params;
    const product = await getProduct(slug);
    return {
      title: `${product.name} | CommerceAI`,
      description: product.description,
    };
  } catch {
    return { title: "Product | CommerceAI" };
  }
}

export default async function ProductPage({ params }: ProductPageProps) {
  const { slug } = await params;

  try {
    const product = await getProduct(slug);
    return <ProductDetailView product={product} />;
  } catch {
    notFound();
  }
}
