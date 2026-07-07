export type CartItem = {
  productId: number;
  productName: string;
  productSlug: string;
  imageUrl: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  stockQuantity: number;
};

export type Cart = {
  items: CartItem[];
  itemCount: number;
  subtotal: number;
};

export type AddCartItemRequest = {
  productId: number;
  quantity: number;
};

export type UpdateCartItemRequest = {
  quantity: number;
};

export type CheckoutRequest = {
  shippingName: string;
  shippingEmail: string;
  shippingAddress: string;
  shippingCity: string;
  shippingState: string;
  shippingPostalCode: string;
};

export type OrderItem = {
  productId: number;
  productName: string;
  productSlug: string;
  imageUrl: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type Order = {
  id: number;
  status: "CONFIRMED" | "CANCELLED";
  totalAmount: number;
  shippingName: string;
  shippingEmail: string;
  shippingAddress: string;
  shippingCity: string;
  shippingState: string;
  shippingPostalCode: string;
  createdAt: string;
  items: OrderItem[];
};
