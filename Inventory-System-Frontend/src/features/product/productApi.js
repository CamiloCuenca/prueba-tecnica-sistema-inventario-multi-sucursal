// Eliminar producto (DELETE /api/products/{id})
export const deleteProduct = async (id) => {
  const response = await api.delete(`/api/products/${id}`);
  return response.data;
};

// Editar producto (PUT /api/products/{id})
export const updateProduct = async (id, body) => {
  const response = await api.put(`/api/products/${id}`, body);
  return response.data;
};
// Crear producto (POST /api/products)
export const createProduct = async (body) => {
  const response = await api.post('/api/products', body);
  return response.data;
};
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

