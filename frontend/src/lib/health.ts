import { fetchApi } from "@/lib/api";
import type { HealthResponse } from "@/types";

export function getHealth(): Promise<HealthResponse> {
  return fetchApi<HealthResponse>("/api/health");
}
