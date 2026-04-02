import { api } from "../../api/client";

export const getProductPrices = async (productId) => {
  if (!productId) throw new Error("productId es requerido");
  const response = await api.get(`/api/products/${productId}/prices`);
  return response.data; // [{ priceType, price }]
};
