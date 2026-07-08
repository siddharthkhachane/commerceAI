import { fetchApi } from "@/lib/api";
import type {
  SavedProduct,
  UpdateUserProfileRequest,
  UserProfile,
} from "@/types/profile";

export function getProfile(): Promise<UserProfile> {
  return fetchApi<UserProfile>("/api/profile");
}

export function updateProfile(data: UpdateUserProfileRequest): Promise<UserProfile> {
  return fetchApi<UserProfile>("/api/profile", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export function getSavedItems(): Promise<SavedProduct[]> {
  return fetchApi<SavedProduct[]>("/api/profile/saved-items");
}

export function saveItem(productId: number): Promise<SavedProduct> {
  return fetchApi<SavedProduct>(`/api/profile/saved-items/${productId}`, {
    method: "POST",
  });
}

export function removeSavedItem(productId: number): Promise<void> {
  return fetchApi<void>(`/api/profile/saved-items/${productId}`, {
    method: "DELETE",
  });
}
