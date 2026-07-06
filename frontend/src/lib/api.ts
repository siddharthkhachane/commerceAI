import type { ApiError } from "@/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiRequestError extends Error {
  constructor(
    message: string,
    public status: number,
    public errors?: Record<string, string>,
  ) {
    super(message);
    this.name = "ApiRequestError";
  }
}

function getAuthToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return localStorage.getItem("auth-token");
}

export async function fetchApi<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init?.headers as Record<string, string> | undefined),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    const errorBody = (await response.json().catch(() => null)) as ApiError | null;
    throw new ApiRequestError(
      errorBody?.message ?? `API request failed: ${response.status}`,
      response.status,
      errorBody?.errors,
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
