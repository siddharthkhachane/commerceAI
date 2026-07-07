"use client";

import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { CheckoutPageContent } from "@/components/cart/CheckoutPageContent";

export default function CheckoutPage() {
  return (
    <ProtectedRoute>
      <CheckoutPageContent />
    </ProtectedRoute>
  );
}
