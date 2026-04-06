// Obtener producto por id (GET /api/products/{id})
export const getById = async (id) => {
  const response = await api.get(`/api/products/${id}`);
  return response.data;
};
// Listar productos (get paginado)
export const listGet = async (body = { page: 0, size: 20, sort: [] }) => {
  const response = await api.get('/api/products', { params: body });
  return response.data;
};
import { api } from '../../api/client';
export const getProduct = async ({ branchId, productId }) => {
  const response = await api.get(`/api/branches/${branchId}/inventory/${productId}`);
  return response.data;
};

