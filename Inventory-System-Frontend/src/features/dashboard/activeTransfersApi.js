import { api } from "../../api/client";

export const getActiveTransfersImpact = async ({ branchId } = {}) => {
  const response = await api.get("/api/metrics/active-transfers-impact", {
    params: branchId ? { branchId } : undefined,
  });
  return response.data ?? [];
};


