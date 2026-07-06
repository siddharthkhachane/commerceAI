import { fetchApi } from "@/lib/api";
import type { AuthResponse, LoginRequest, RegisterRequest, User } from "@/types";

export function register(data: RegisterRequest): Promise<AuthResponse> {
  return fetchApi<AuthResponse>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function login(data: LoginRequest): Promise<AuthResponse> {
  return fetchApi<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function getCurrentUser(): Promise<User> {
  return fetchApi<User>("/api/auth/me");
}
