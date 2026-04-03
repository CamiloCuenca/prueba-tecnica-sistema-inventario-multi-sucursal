import { api } from "../../api/client";

// items: [{ productId, quantity, price, discount }], discountTotal: number
export const postSale = async ({ branchId, items, discountTotal = 0 }) => {
  if (!branchId || !Array.isArray(items) || items.length === 0) {
    throw new Error("branchId e items son requeridos");
  }
  const response = await api.post(`/api/sales`, {
    branchId,
    items,
    discountTotal,
  });
  return response.data;
};

