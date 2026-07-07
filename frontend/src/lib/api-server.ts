const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function fetchApiServer<T>(
  path: string,
  init?: RequestInit & { revalidate?: number },
): Promise<T> {
  const { revalidate = 60, ...requestInit } = init ?? {};
  const response = await fetch(`${API_URL}${path}`, {
    ...requestInit,
    headers: {
      "Content-Type": "application/json",
      ...(requestInit.headers as Record<string, string> | undefined),
    },
    next: { revalidate },
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}
