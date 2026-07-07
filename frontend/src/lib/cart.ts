import { fetchApi } from "@/lib/api";
import type {
  AddCartItemRequest,
  Cart,
  UpdateCartItemRequest,
} from "@/types/cart";

export function getCart(): Promise<Cart> {
  return fetchApi<Cart>("/api/cart");
}

export function addCartItem(data: AddCartItemRequest): Promise<Cart> {
  return fetchApi<Cart>("/api/cart/items", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function updateCartItem(
  productId: number,
  data: UpdateCartItemRequest,
): Promise<Cart> {
  return fetchApi<Cart>(`/api/cart/items/${productId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export function removeCartItem(productId: number): Promise<Cart> {
  return fetchApi<Cart>(`/api/cart/items/${productId}`, {
    method: "DELETE",
  });
}

export function clearCart(): Promise<void> {
  return fetchApi<void>("/api/cart", {
    method: "DELETE",
  });
}
