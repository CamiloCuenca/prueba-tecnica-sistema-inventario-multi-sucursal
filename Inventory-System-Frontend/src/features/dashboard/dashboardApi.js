import { api } from "../../api/client";

export const getInventoryBehaviorMetrics = async () => {
  const response = await api.get("/api/metrics/inventory-behavior");
  return response.data;
}
