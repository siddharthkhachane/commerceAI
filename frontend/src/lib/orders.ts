import { fetchApi } from "@/lib/api";
import type { CheckoutRequest, Order } from "@/types/cart";

export function checkout(data: CheckoutRequest): Promise<Order> {
  return fetchApi<Order>("/api/orders/checkout", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function getOrders(): Promise<Order[]> {
  return fetchApi<Order[]>("/api/orders");
}

export function getOrder(orderId: number): Promise<Order> {
  return fetchApi<Order>(`/api/orders/${orderId}`);
}
