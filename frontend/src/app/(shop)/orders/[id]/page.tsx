"use client";

import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { OrderConfirmation } from "@/components/cart/OrderConfirmation";
import { use } from "react";

type OrderPageProps = {
  params: Promise<{ id: string }>;
};

export default function OrderPage({ params }: OrderPageProps) {
  const { id } = use(params);
  const orderId = Number(id);

  return (
    <ProtectedRoute>
      <OrderConfirmation orderId={orderId} />
    </ProtectedRoute>
  );
}
