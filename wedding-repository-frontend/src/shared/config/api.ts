const DEFAULT_BACKEND_API_BASE_URL = "http://localhost:8082";

export function getBackendApiBaseUrl() {
  const configuredBaseUrl =
    process.env.BACKEND_API_BASE_URL?.trim() || process.env.NEXT_PUBLIC_BACKEND_API_BASE_URL?.trim();

  return (configuredBaseUrl || DEFAULT_BACKEND_API_BASE_URL).replace(/\/+$/, "");
}
