import type { PageResponse, ProductDetail } from "@/types/catalog";

export type AdminMetrics = {
  totalRevenue: number;
  totalOrders: number;
  confirmedOrders: number;
  totalProducts: number;
  activeProducts: number;
  lowStockProducts: number;
  totalCustomers: number;
  inventoryUnits: number;
};

export type TimeSeriesPoint = {
  date: string;
  value: number;
};

export type OrdersTimeSeriesPoint = {
  date: string;
  count: number;
};

export type TopProductStat = {
  productId: number;
  productName: string;
  unitsSold: number;
  revenue: number;
};

export type CategoryInventoryStat = {
  categoryName: string;
  productCount: number;
  inventoryUnits: number;
};

export type AdminAnalytics = {
  revenueSeries: TimeSeriesPoint[];
  ordersSeries: OrdersTimeSeriesPoint[];
  topProducts: TopProductStat[];
  categoryBreakdown: CategoryInventoryStat[];
};

export type AdminOrderSummary = {
  id: number;
  customerName: string;
  customerEmail: string;
  status: "CONFIRMED" | "CANCELLED";
  totalAmount: number;
  itemCount: number;
  createdAt: string;
};

export type AdminInventoryItem = {
  product: ProductDetail;
  isLowStock: boolean;
};

export type AdminInventoryResponse = {
  lowStockThreshold: number;
  items: PageResponse<AdminInventoryItem>;
};

export type BusinessInsight = {
  title: string;
  detail: string;
  severity: "info" | "warning" | "success";
};

export type BusinessSnapshot = {
  currentPeriodRevenue: number;
  previousPeriodRevenue: number;
  revenueChangePercent: number;
  currentPeriodOrders: number;
  previousPeriodOrders: number;
  ordersChangePercent: number;
  lowStockProducts: number;
  activeProducts: number;
  totalProducts: number;
  topProductName: string | null;
  topProductRevenue: number | null;
};

export type BusinessAssistantResponse = {
  question: string;
  summary: string;
  insights: BusinessInsight[];
  snapshot: BusinessSnapshot;
  recommendations: string[];
};
