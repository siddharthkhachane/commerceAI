import type { User } from "@/types";

export type UserPreferences = {
  stylePreference: string | null;
  preferredCategorySlug: string | null;
  budgetPreference: number | null;
  emailNotifications: boolean;
};

export type UserProfile = {
  id: number;
  email: string;
  fullName: string;
  role: User["role"];
  preferences: UserPreferences;
  orderCount: number;
  savedItemCount: number;
};

export type UpdateUserProfileRequest = {
  fullName: string;
  stylePreference?: string | null;
  preferredCategorySlug?: string | null;
  budgetPreference?: number | null;
  emailNotifications: boolean;
};

export type SavedProduct = {
  product: import("@/types/catalog").ProductSummary;
  savedAt: string;
};
