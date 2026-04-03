import { api } from "../../../api/client";

export const getBranchPerformanceMetrics = async () => {
  const response = await api.get("/api/metrics/branch-performance-comparison");
  return response.data?.content ?? [];
};
