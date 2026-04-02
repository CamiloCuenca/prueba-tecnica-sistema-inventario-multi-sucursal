import { api } from '../../api/client';
export const getProduct = async ({ branchId, productId }) => {
  const response = await api.get(`/api/branches/${branchId}/inventory/${productId}`);
  return response.data;
};